package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBECFEBehaviour;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEItemBehaviour;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.behaviours.CFEHandlerBehaviour;
import net.sinedkadis.terracompositio.block.behaviours.ManySlotItemHandlerBehaviour;
import net.sinedkadis.terracompositio.block.custom.MatterInfuserBaseEntityBlock;
import net.sinedkadis.terracompositio.recipe.MatterInfusionRecipe;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCItems;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

import static net.sinedkadis.terracompositio.registries.TCBlockStateProperties.INFUSED;

@ParametersAreNonnullByDefault
public class MatterInfuserIOBlockEntity extends MatterInfuserBaseBlockEntity{


    protected float catalystDecayRate;

    public MatterInfuserIOBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.MATTER_INFUSER_IO_BE.get(), pos, state);
    }

    @Override
    public void addBEBehaviours(List<IBEBehaviour> list) {
        list.add(new CFEHandlerBehaviour(this){
            @Override
            public Vec3 particleTargetOffset() {
                return switch (getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)){
                    case SOUTH -> new Vec3(8.0 / 16, 8.0 / 16, 0.5 / 16);
                    case NORTH -> new Vec3(8.0 / 16, 8.0 / 16, 15.5 / 16);
                    case EAST -> new Vec3(0.5 / 16, 8.0 / 16, 8.0 / 16);
                    case WEST -> new Vec3(15.5 / 16, 8.0 / 16, 8.0 / 16);
                    default -> super.particleTargetOffset();
                };
            }
        }.range(10));
        list.add(new ManySlotItemHandlerBehaviour(this) {
            @Override
            public boolean allowExtract(int pSlot, ItemStack pStack, @Nullable Direction pDirection, boolean manual) {
                return false;
            }

            @Override
            public boolean allowInsert(int pSlot, ItemStack pStack, @Nullable Direction pDirection, boolean manual) {
                boolean enough = pStack.getCount() >= 2;
                boolean isRod = pStack.is(TCItems.INFUSED_IRON_ROD.get());
                return manual && enough && isRod;
            }

            @Override
            public int getLimitInSlot(int slot) {
                return 2;
            }

            @Override
            public @NotNull InteractionResult onUse(Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
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

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        super.tick(pLevel, pPos, pState);
        if(hasRecipe() && enoughCFE()){
            increaseCraftingProgress();
            consumeCFE();
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

    protected ItemStack craftItem() {
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
            IBEItemBehaviour itemBehaviour = casingBE.getItemBehaviour();

            if (itemBehaviour instanceof ManySlotItemHandlerBehaviour itemHandlerBehaviour) {
                itemHandlerBehaviour.ignoreRestrictions = true;
                itemHandler.extractItem(0, takeCount, false);
                itemHandler.insertItem(1, result, false);
                itemHandlerBehaviour.ignoreRestrictions = false;
            }

            if (this.level.getRandom().nextInt(100) < catalystDecayRate){
                portBE.extractItemStack(0,1);
            }
            return result;
        }
        return ItemStack.EMPTY;
    }

    public int ticksLeft(){
        return maxProgress-progress;
    }

    protected boolean enoughCFE() {
        return this.getCfeContainer().getCFE() >= Math.ceil(tickCFECost);
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
            tickCFECost = matterInfusionRecipe.getCFETick();
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

    protected ICFEHandler getCfeContainer() {
        IBECFEBehaviour cfeBehaviour = getCFEBehaviour();
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
        ItemStack outputSlot = this.getItemInSlot(1);
        return outputSlot.isEmpty() || outputSlot.is(item);
    }

    protected boolean enoughSpaceInOutput(int count) {
        ItemStack outputSlot = this.getItemInSlot(1);
        return outputSlot.getCount() + count <= outputSlot.getMaxStackSize();
    }
}
