package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;
import net.sinedkadis.terracompositio.api.networks.ecf.IECFHandler;
import net.sinedkadis.terracompositio.api.registries.TCCapabilities;
import net.sinedkadis.terracompositio.block.behaviours.ECFHandlerBehaviour;
import net.sinedkadis.terracompositio.block.behaviours.ItemStateHolderBehaviour;
import net.sinedkadis.terracompositio.block.custom.MatterInfuserBaseEntityBlock;
import net.sinedkadis.terracompositio.config.TCInnerConfig;
import net.sinedkadis.terracompositio.recipe.MatterInfusionRecipe;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCItems;
import net.sinedkadis.terracompositio.util.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.util.behaviors.blockentity.IBEECFBehaviour;
import net.sinedkadis.terracompositio.util.helpers.ParticleHelperInternal;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

import static net.sinedkadis.terracompositio.api.registries.TCBlockStateProperties.INFUSED;
import static net.sinedkadis.terracompositio.block.entity.FlowCedarCasingBlockEntity.DOWN_CONNECTION_SLOT;
import static net.sinedkadis.terracompositio.block.entity.FlowCedarCasingBlockEntity.UP_CONNECTION_SLOT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MatterInfuserUnitBlockEntity extends MatterInfuserBaseBlockEntity{


    protected float catalystDecayRate;
    private boolean isAssembled;

    public MatterInfuserUnitBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.MATTER_INFUSER_IO_BE.get(), pos, state);
    }

    @Override
    public void addBEBehaviours(List<IBEBehaviour> list) {
        list.add(new ECFHandlerBehaviour(this)
                .range(10)
                .priority(TCInnerConfig.DEFAULT_CONSUMER_PRIORITY));
        list.add(new ItemStateHolderBehaviour(this) {

            @Override
            public int getLimitInSlot(int slot) {
                return 2;
            }

            @Override
            public boolean allowInsert(int pSlot, ItemStack pStack, @Nullable Direction pDirection, boolean manual) {
                boolean enough = pStack.getCount() >= 2;
                boolean isRod = pStack.is(TCItems.INFUSED_IRON_ROD.get());
                boolean slotIsEmpty = itemHandler.getStackInSlot(pSlot).isEmpty();
                Direction left = getBlockEntity().getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise();
                if (level == null) return false;
                BlockState leftState = level.getBlockState(getBlockPos().relative(left));
                boolean leftIsMI = leftState.getBlock() instanceof MatterInfuserBaseEntityBlock;
                return manual && enough && isRod && slotIsEmpty && leftIsMI;
            }

            @Override
            public InteractionResult onUse(Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
                TCBlockEntity blockEntity = getBlockEntity();
                Level level = blockEntity.getLevel();
                if (level != null) {
                    BlockPos blockPos = blockEntity.getBlockPos();
                    Direction left = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise();
                    if (level.getBlockState(blockPos.relative(left)).getBlock() instanceof MatterInfuserBaseEntityBlock)
                        return super.onUse(pPlayer, pHand, pHit);
                }
                return InteractionResult.PASS;
            }
        });

    }

    int timer = 0;
    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        super.tick(pLevel, pPos, pState);
        if (timer <= 0) {
            timer = 20;
            isAssembled = assembleValid();
            if (progress>0)
                ParticleHelperInternal.spawnParticlesIn(pLevel,
                        pPos.relative(pState.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite()),
                        ((int) Math.ceil(tickECFCost * 20)));

        }
        timer--;
        if (hasRecipe() && enoughECF() && isAssembled) {
            increaseCraftingProgress();
            consumeECF();
            setChanged(pLevel, pPos, pState);
//            if (!pLevel.isClientSide){
//                ((ServerLevel) pLevel).sendParticles(ModParticles.FLOW_STILL_PARTICLE.get(),pPos.getX()+0.5D,pPos.getY()+0.5D,pPos.getZ()+0.5D,3,0,-0.1D,0,0.1D);
//            }
            if(hasProgressFinished()){
                craftItem();
                resetProgress();
            }
        }else if(!hasRecipe()) {
            resetProgress();
        }
    }

    private boolean assembleValid() {
        if (level == null) return false;

        FlowCedarCasingBlockEntity casingBE = getCasingBE();

        if (casingBE == null) {
            return false;
        }
        IItemHandler casingItemHandler = casingBE.getCapability(TCCapabilities.ITEM_STATE_HOLDER).orElse(((IItemHandlerModifiable) EmptyHandler.INSTANCE));
        if (casingItemHandler.getStackInSlot(UP_CONNECTION_SLOT).isEmpty()
                || casingItemHandler.getStackInSlot(DOWN_CONNECTION_SLOT).isEmpty())
            return false;

        for (int i = 0; i < 9; i++) {
            Direction dir = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise();
            BlockPos currentPos = worldPosition.relative(dir, i);
            BlockEntity blockEntity = level.getBlockEntity(currentPos);
            if (blockEntity instanceof MatterInfuserPortBlockEntity) break;
            if (blockEntity instanceof MatterInfuserUnitBlockEntity unitBlockEntity) {
                IItemHandler unitItemHandler = unitBlockEntity.getCapability(TCCapabilities.ITEM_STATE_HOLDER).orElse(((IItemHandlerModifiable) EmptyHandler.INSTANCE));
                if (unitItemHandler.getStackInSlot(0).isEmpty()) return false;
                continue;
            }
            return false;
        }
        return true;
    }

    protected void craftItem() {
        Optional<MatterInfusionRecipe> recipe = getCurrentRecipe();
        MatterInfuserPortBlockEntity portBE = this.getPortBE();
        FlowCedarCasingBlockEntity casingBE = this.getCasingBE();
        if (recipe.isPresent()
                && this.level != null
                && portBE != null
                && casingBE != null) {
            ItemStack result = recipe.get().getResultItem(null);
            int takeCount = recipe.get().getIngredients().get(1).getItems()[0].getCount();

            IItemHandlerModifiable itemHandler = casingBE.getItemHandler();

            ItemStack copy = itemHandler.getStackInSlot(0).copy();
            copy.shrink(takeCount);
            itemHandler.setStackInSlot(0, copy);
            ItemStack resultCopy = result.copy();
            resultCopy.setCount(resultCopy.getCount() + itemHandler.getStackInSlot(1).getCount());
            itemHandler.setStackInSlot(1, resultCopy);
            if (level != null) {
                BlockState blockState = getBlockState();
                level.sendBlockUpdated(worldPosition, blockState, blockState, 3);
                if (this.level.getRandom().nextInt(100) < catalystDecayRate) {
                    portBE.extractItemStackViaSetter(0, 1);
                }
            }
        }
    }

    protected boolean hasRecipe() {
        Optional<MatterInfusionRecipe> recipe = getCurrentRecipe();
        if (recipe.isEmpty()){
            return false;
        }
        MatterInfusionRecipe matterInfusionRecipe = recipe.get();
        ItemStack result = matterInfusionRecipe.getResultItem(null);
        boolean outputTest = enoughSpaceInOutput(result.getCount()) && sameItemInOutput(result.getItem());
        FlowCedarCasingBlockEntity casingBE = this.getCasingBE();
        boolean infusedTest = false;
        if (casingBE != null) {
            infusedTest = casingBE.getBlockState().getValue(INFUSED);
        }
        if (outputTest && infusedTest) {
            maxProgress = matterInfusionRecipe.getTicks();
            tickECFCost = matterInfusionRecipe.getECFTick();
            catalystDecayRate = matterInfusionRecipe.getCatalystDecayRate();
        }
        return outputTest && infusedTest;
    }

    @Override
    protected IItemHandlerModifiable getItemHandler() {
        FlowCedarCasingBlockEntity casingBE = getCasingBE();
        if (casingBE != null)
            return casingBE.getItemHandler();
        throw new RuntimeException("Item handler not present: " + this);
    }

    protected IECFHandler getEcfContainer() {
        IBEECFBehaviour cfeBehaviour = getECFBehaviour();
        if (cfeBehaviour != null) {
            return cfeBehaviour.getMainHandler();
        }
        throw new RuntimeException("CFE handler not present: " + this);
    }

    public Optional<MatterInfusionRecipe> getCurrentRecipe() {
        ItemStack catalyst = this.getCatalyst();
        ItemStack inputSlot = this.getInputSlot();
        if (catalyst.isEmpty() || inputSlot.isEmpty())
            return Optional.empty();
        SimpleContainer inventory;inventory = new SimpleContainer(catalyst, inputSlot);

        if (this.level != null) {
            return this.level.getRecipeManager().getRecipeFor(MatterInfusionRecipe.Type.INSTANCE, inventory, level);
        }
        return Optional.empty();
    }

    @Override
    protected int getECF() {
        return getEcfContainer().getECF();
    }

    public ItemStack getCatalyst() {
        MatterInfuserPortBlockEntity port = this.getPortBE();
        return port != null ? port.getInputSlot() : ItemStack.EMPTY;
    }

    private @Nullable MatterInfuserPortBlockEntity getPortBE() {
        for (int i = 1; i <= 8; i++){
            if (level != null
                    && level.getBlockEntity(worldPosition.relative(
                            this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise(), i))
                    instanceof MatterInfuserPortBlockEntity blockEntity) {
                if (blockEntity.getInputSlot().isEmpty())
                    continue;
                return blockEntity;
            }
        }
        return null;
    }

    protected boolean sameItemInOutput(Item item) {
        ItemStack outputSlot = this.getItemInSlot(FlowCedarCasingBlockEntity.OUTPUT_INVENTORY_SLOT);
        boolean b = outputSlot.isEmpty() || outputSlot.is(item);
        checkCraftException(b, CraftException.NO_SPACE);
        return b;
    }

    protected boolean enoughSpaceInOutput(int count) {
        ItemStack outputSlot = this.getItemInSlot(FlowCedarCasingBlockEntity.OUTPUT_INVENTORY_SLOT);
        boolean b = outputSlot.getCount() + count <= outputSlot.getMaxStackSize();
        checkCraftException(b, CraftException.NO_SPACE);
        return b;
    }
}
