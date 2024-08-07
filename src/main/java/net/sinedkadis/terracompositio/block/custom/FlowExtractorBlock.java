package net.sinedkadis.terracompositio.block.custom;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.SoundAction;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.entity.FlowExtractorBlockEntity;
import net.sinedkadis.terracompositio.block.entity.ModBlockEntities;
import net.sinedkadis.terracompositio.block.entity.ModBlockEntity;
import net.sinedkadis.terracompositio.fluid.ModFluids;
import net.sinedkadis.terracompositio.item.ModItems;
import net.sinedkadis.terracompositio.util.IBE;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class FlowExtractorBlock extends BaseEntityBlock implements IBE<FlowExtractorBlockEntity> {
    public FlowExtractorBlock(Properties properties) {
        super(properties);
    }
    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }
    private static final String KEY_FILLED = TerraCompositio.makeDescriptionId("block", "tank.filled");
    private static final String KEY_DRAINED = TerraCompositio.makeDescriptionId("block", "tank.drained");

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Shapes.join(
                Shapes.join(
                Block.box(0,2,0,2,16,16),
                Block.box(2,2,14,16,16,16),
                BooleanOp.OR),
                Shapes.join(
                        Shapes.join(
                                Block.box(14,2,2,16,16,14),
                                Block.box(2,2,0,16,16,2),
                                BooleanOp.OR),
                        Block.box(2,2,2,14,4,14),
                        BooleanOp.OR),
                BooleanOp.OR);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return ModBlockEntities.FLOW_EXTRACTOR_BE.get().create(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }
        return createTickerHelper(pBlockEntityType, ModBlockEntities.FLOW_EXTRACTOR_BE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1,pPos,pState1));
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(handIn);

        return onBlockEntityUse(worldIn, pos, be -> {
            if (!heldItem.isEmpty()) {
                if (tryEmptyItemIntoBE(worldIn, player, handIn, heldItem, be))
                    return InteractionResult.SUCCESS;
                if (tryFillItemFromBE(worldIn, player, handIn, heldItem, be))
                    return InteractionResult.SUCCESS;

                if (canItemBeEmptied(worldIn, heldItem)
                        || canItemBeFilled(worldIn, heldItem))
                    return InteractionResult.SUCCESS;
                if (heldItem.getItem()
                        .equals(Items.SPONGE)
                        && !be.getCapability(ForgeCapabilities.FLUID_HANDLER)
                        .map(iFluidHandler -> iFluidHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE))
                        .orElse(FluidStack.EMPTY)
                        .isEmpty()) {
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.PASS;
            }
            return InteractionResult.SUCCESS;
        });
    }

    @Override
    public Class<FlowExtractorBlockEntity> getBlockEntityClass() {
        return FlowExtractorBlockEntity.class;
    }

    private static void playEmptySound(Level world, BlockPos pos, Player player, FluidStack transferred) {
        world.playSound(null, pos, getEmptySound(transferred), SoundSource.BLOCKS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable(KEY_FILLED, transferred.getAmount(), transferred.getDisplayName()), true);
    }

    private static void playFillSound(Level world, BlockPos pos, Player player, FluidStack transferred) {
        world.playSound(null, pos, getFillSound(transferred), SoundSource.BLOCKS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable(KEY_DRAINED, transferred.getAmount(), transferred.getDisplayName()), true);
    }
    public static SoundEvent getEmptySound(FluidStack fluid) {
        return getSound(fluid, SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY);
    }
    /** Gets the given sound from the fluid */
    public static SoundEvent getSound(FluidStack fluid, SoundAction action, SoundEvent fallback) {
        SoundEvent event = fluid.getFluid().getFluidType().getSound(fluid, action);
        if (event == null) {
            return fallback;
        }
        return event;
    }
    /** Gets the fill sound for a fluid */
    public static SoundEvent getFillSound(FluidStack fluid) {
        return getSound(fluid, SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL);
    }

    public static boolean tryEmptyItemIntoBE(Level worldIn, Player player, InteractionHand handIn, ItemStack heldItem,
                                             ModBlockEntity be) {
        if (!canItemBeEmptied(worldIn, heldItem))
            return false;

        Pair<FluidStack, ItemStack> emptyingResult = emptyItem(worldIn, heldItem, true);
        LazyOptional<IFluidHandler> capability = be.getCapability(ForgeCapabilities.FLUID_HANDLER);
        IFluidHandler tank = capability.orElse(null);
        FluidStack fluidStack = emptyingResult.getFirst();

        if (tank == null || fluidStack.getAmount() != tank.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE))
            return false;
        if (worldIn.isClientSide)
            return true;

        ItemStack copyOfHeld = heldItem.copy();
        emptyingResult = emptyItem(worldIn, copyOfHeld, false);
        tank.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);

        if (!player.isCreative()) {
            if (copyOfHeld.isEmpty())
                player.setItemInHand(handIn, emptyingResult.getSecond());
            else {
                player.setItemInHand(handIn, copyOfHeld);
                player.getInventory()
                        .placeItemBackInInventory(emptyingResult.getSecond());
            }
        }
        return true;
    }
    public static boolean canItemBeEmptied(Level world, ItemStack stack) {
        if (stack.getItem() instanceof PotionItem)
            return true;
        if (stack.is(ModItems.FLOW_BOTTLE.get()))
            return true;

        LazyOptional<IFluidHandlerItem> capability =
                stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
        IFluidHandlerItem tank = capability.orElse(null);
        if (tank == null)
            return false;
        for (int i = 0; i < tank.getTanks(); i++) {
            if (tank.getFluidInTank(i)
                    .getAmount() > 0)
                return true;
        }
        return false;
    }
    public static Pair<FluidStack, ItemStack> emptyItem(Level world, ItemStack stack, boolean simulate) {
        FluidStack resultingFluid = FluidStack.EMPTY;
        ItemStack resultingItem = ItemStack.EMPTY;

        ItemStack split = stack.copy();
        split.setCount(1);
        LazyOptional<IFluidHandlerItem> capability =
                split.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
        IFluidHandlerItem tank = capability.orElse(null);
        if (tank == null) {
            if (split.is(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER).getItem())){
                resultingFluid = new FluidStack(Fluids.WATER,250);
                resultingItem = new ItemStack(Items.GLASS_BOTTLE);
                if (!simulate)
                    stack.shrink(1);
            }
            if (split.is(ModItems.FLOW_BOTTLE.get())){
                resultingFluid = new FluidStack(ModFluids.FLOW_FLUID.source.get(),250);
                resultingItem = new ItemStack(Items.GLASS_BOTTLE);
                if (!simulate)
                    stack.shrink(1);
            }
            return Pair.of(resultingFluid, resultingItem);
        }
        resultingFluid = tank.drain(1000, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
        resultingItem = tank.getContainer()
                .copy();
        if (!simulate)
            stack.shrink(1);

        return Pair.of(resultingFluid, resultingItem);
    }

    public static boolean tryFillItemFromBE(Level world, Player player, InteractionHand handIn, ItemStack heldItem,
                                            ModBlockEntity be) {
        if (!canItemBeFilled(world, heldItem))
            return false;

        LazyOptional<IFluidHandler> capability = be.getCapability(ForgeCapabilities.FLUID_HANDLER);
        IFluidHandler tank = capability.orElse(null);

        for (int i = 0; i < tank.getTanks(); i++) {
            FluidStack fluid = tank.getFluidInTank(i);
            if (fluid.isEmpty())
                continue;
            int requiredAmountForItem = getRequiredAmountForItem(world, heldItem, fluid.copy());
            if (requiredAmountForItem == -1)
                continue;
            if (requiredAmountForItem > fluid.getAmount())
                continue;

            if (world.isClientSide)
                return true;

            if (player.isCreative())
                heldItem = heldItem.copy();
            ItemStack out = fillItem(world, requiredAmountForItem, heldItem, fluid.copy());

            FluidStack copy = fluid.copy();
            copy.setAmount(requiredAmountForItem);
            tank.drain(copy, IFluidHandler.FluidAction.EXECUTE);

            if (!player.isCreative())
                player.getInventory()
                        .placeItemBackInInventory(out);
            be.sendUpdate();
            return true;
        }

        return false;
    }
    public static boolean canItemBeFilled(Level world, ItemStack stack) {
        if (stack.getItem() == Items.GLASS_BOTTLE)
            return true;
        if (stack.getItem() == Items.MILK_BUCKET)
            return false;

        LazyOptional<IFluidHandlerItem> capability =
                stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
        IFluidHandlerItem tank = capability.orElse(null);
        if (tank == null)
            return false;
        if (!isFluidHandlerValid(stack, tank))
            return false;
        for (int i = 0; i < tank.getTanks(); i++) {
            if (tank.getFluidInTank(i)
                    .getAmount() < tank.getTankCapacity(i))
                return true;
        }
        return false;
    }
    public static boolean isFluidHandlerValid(ItemStack stack, IFluidHandlerItem fluidHandler) {
        // Not instanceof in case a correct subclass is made
        if (fluidHandler.getClass() == FluidBucketWrapper.class) {
            Item item = stack.getItem();
            // Forge does not patch the FluidBucketWrapper onto subclasses of BucketItem
            if (item.getClass() != BucketItem.class && !(item instanceof MilkBucketItem)) {
                return false;
            }
        }
        return true;
    }
    public static int getRequiredAmountForItem(Level world, ItemStack stack, FluidStack availableFluid) {
        if (stack.getItem() == Items.GLASS_BOTTLE && canFillGlassBottleInternally(availableFluid))
            return 250;
        if (stack.getItem() == Items.BUCKET && canFillBucketInternally(availableFluid))
            return 1000;

        LazyOptional<IFluidHandlerItem> capability =
                stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
        IFluidHandlerItem tank = capability.orElse(null);
        if (tank instanceof FluidBucketWrapper) {
            Item filledBucket = availableFluid.getFluid()
                    .getBucket();
            if (filledBucket == Items.AIR)
                return -1;
            if (!((FluidBucketWrapper) tank).getFluid()
                    .isEmpty())
                return -1;
            return 1000;
        }

        int filled = tank.fill(availableFluid, IFluidHandler.FluidAction.SIMULATE);
        return filled == 0 ? -1 : filled;
    }
    private static boolean canFillGlassBottleInternally(FluidStack availableFluid) {
        Fluid fluid = availableFluid.getFluid();
        if (fluid.isSame(Fluids.WATER))
            return true;
        if (fluid.isSame(ModFluids.FLOW_FLUID.source.get()))
            return true;
        return false;
    }
    private static boolean canFillBucketInternally(FluidStack availableFluid) {
        return false;
    }
    public static ItemStack fillItem(Level world, int requiredAmount, ItemStack stack, FluidStack availableFluid) {
        FluidStack toFill = availableFluid.copy();
        toFill.setAmount(requiredAmount);
        availableFluid.shrink(requiredAmount);

        if (stack.getItem() == Items.GLASS_BOTTLE && canFillGlassBottleInternally(toFill)) {
            ItemStack fillBottle;
            Fluid fluid = toFill.getFluid();
            if (isWater(fluid))
                fillBottle = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
            else if (fluid.isSame(ModFluids.FLOW_FLUID.source.get()))
                fillBottle = new ItemStack(ModItems.FLOW_BOTTLE.get());
            else
                return new ItemStack(Items.GLASS_BOTTLE);
            stack.shrink(1);
            return fillBottle;
        }

        ItemStack split = stack.copy();
        split.setCount(1);
        LazyOptional<IFluidHandlerItem> capability =
                split.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
        IFluidHandlerItem tank = capability.orElse(null);
        tank.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
        ItemStack container = tank.getContainer()
                .copy();
        stack.shrink(1);
        return container;
    }
    public static boolean isWater(Fluid fluid) {
        return convertToStill(fluid) == Fluids.WATER;
    }

    public static Fluid convertToStill(Fluid fluid) {
        if (fluid == Fluids.FLOWING_WATER)
            return Fluids.WATER;
        if (fluid == Fluids.FLOWING_LAVA)
            return Fluids.LAVA;
        if (fluid instanceof ForgeFlowingFluid)
            return ((ForgeFlowingFluid) fluid).getSource();
        return fluid;
    }

}
