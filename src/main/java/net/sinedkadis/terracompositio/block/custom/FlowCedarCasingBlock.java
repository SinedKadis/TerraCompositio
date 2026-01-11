package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.ItemStackHandler;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEItemBehaviour;
import net.sinedkadis.terracompositio.block.entity.FlowCedarCasingBlockEntity;
import net.sinedkadis.terracompositio.item.custom.WrenchAxeItem;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCBlockStateProperties;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.registries.TCItems;
import net.sinedkadis.terracompositio.util.FunctionSide;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

import static net.sinedkadis.terracompositio.block.custom.TCBaseEntityBlock.createTickerHelper;
import static net.sinedkadis.terracompositio.util.TCUtil.handleInWorldBlockCraft;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FlowCedarCasingBlock extends FlowCedarLikeBlock implements EntityBlock {
    protected static final BooleanProperty INPUT_BUS;
    protected static final BooleanProperty OUTPUT_BUS;
    protected static final EnumProperty<FunctionSide> FUNCTION_SIDE;
    protected static final BooleanProperty INPUT_BUS_CONNECTION;
    protected static final BooleanProperty OUTPUT_BUS_CONNECTION;

    public FlowCedarCasingBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(defaultBlockState()
                .setValue(INPUT_BUS,false)
                .setValue(OUTPUT_BUS,false)
                .setValue(FUNCTION_SIDE,FunctionSide.NONE)
                .setValue(INPUT_BUS_CONNECTION,false)
                .setValue(OUTPUT_BUS_CONNECTION,false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(AXIS, INPUT_BUS, OUTPUT_BUS, FUNCTION_SIDE, INPUT_BUS_CONNECTION, OUTPUT_BUS_CONNECTION, INFUSED,WAXED);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public @NotNull InteractionResult use(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
        ItemStack item = pPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack item2 = pPlayer.getItemInHand(InteractionHand.OFF_HAND);
        if (pState.getValue(AXIS).isVertical() && item2.getItem() instanceof WrenchAxeItem){
            if (item.is(TCItems.INFUSED_IRON_ROD.get()) && item.getCount() >= 8){
                if (WrenchAxeItem.getMode(item2).equals(WrenchAxeItem.WrenchMode.WRENCH)) {
                    return handleInWorldBlockCraft(pState,
                            TCBlocks.FLOW_CEDAR_TANK.get().defaultBlockState()
                                    .setValue(FlowCedarTankBlock.STAGE, pState.getValue(INFUSED) ? 0 : 1),
                            pLevel, pPos, item, 8);
                }
            }
        }
        if (item.is(TCItems.INPUT_BUS.get()) && !hasInputBus(pState)) {
            return handleInWorldBlockCraft(pState, pState.setValue(INPUT_BUS, true), pLevel, pPos, item, 1);
        }
        if (item.is(TCItems.OUTPUT_BUS.get()) && !hasOutputBus(pState)) {
            return handleInWorldBlockCraft(pState, pState.setValue(OUTPUT_BUS, true), pLevel, pPos, item, 1);
        }
        BlockPos blockPos = pPos.relative(FunctionSide.getDirectionByFunctionSide(pState));
        Item item1 = TCItems.INFUSED_IRON_ROD.get();
        BlockState blockState1 = pLevel.getBlockState(blockPos);
        if (item.is(item1) && hasInputBus(pState) && !hasInputBusConnection(pState) && blockState1.hasProperty(TCBlockStateProperties.UP_CONNECTION)){
            pLevel.setBlock(pPos,pState.setValue(INPUT_BUS_CONNECTION,true),3);
            return handleInWorldBlockCraft(blockState1, blockState1.setValue(TCBlockStateProperties.UP_CONNECTION, true), pLevel, blockPos, item, 1);
        }
        if (item.is(item1) && hasOutputBus(pState) && !hasOutputBusConnection(pState) && blockState1.hasProperty(TCBlockStateProperties.DOWN_CONNECTION)){
            pLevel.setBlock(pPos,pState.setValue(OUTPUT_BUS_CONNECTION,true),3);
            return handleInWorldBlockCraft(blockState1, blockState1.setValue(TCBlockStateProperties.DOWN_CONNECTION, true), pLevel, blockPos, item, 1);
        }
        if (isPortAttached(pLevel,pState,pPos) && pPos != pHit.getBlockPos() && pHand != InteractionHand.OFF_HAND){
            FlowCedarCasingBlockEntity blockEntity = (FlowCedarCasingBlockEntity) pLevel.getBlockEntity(pPos);
            assert blockEntity != null;
            ItemStackHandler itemHandler = blockEntity.getItemBehaviour().get().getItemHandler();
            ItemStack inputSlot = itemHandler.getStackInSlot(0);
            if (inputSlot.isEmpty()) {
                if (!item.isEmpty()) {
                    itemHandler.insertItem(0, item,false);
                    item.shrink(1);
                    pLevel.playSound(pPlayer, pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS);
                    return InteractionResult.SUCCESS;
                }
            } else if (!inputSlot.isEmpty()) {
                if (!pPlayer.addItem(inputSlot)) {
                    pPlayer.drop(inputSlot, false);
                }
                itemHandler.setStackInSlot(0,ItemStack.EMPTY);
                pLevel.playSound(pPlayer, pPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS);
                return InteractionResult.SUCCESS;
            }
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public void onRemove(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pIsMoving) {
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        if (pState.getBlock() != pNewState.getBlock()
                && pNewState.getBlock() != Blocks.STRUCTURE_VOID){
            Direction direction = FunctionSide.getDirectionByFunctionSide(pState);
            if (direction != Direction.DOWN) {
                BlockPos blockPos = pPos.relative(direction);
                BlockState blockState = pLevel.getBlockState(blockPos);
                if (blockState.is(TCBlocks.MATTER_INFUSER_PORT.get())
                        || blockState.is(TCBlocks.MATTER_INFUSER_IO.get())) {
                    pLevel.destroyBlock(blockPos, true);
                }
            }
            FlowCedarCasingBlockEntity blockEntity = (FlowCedarCasingBlockEntity) pLevel.getBlockEntity(pPos);
            if (blockEntity != null) {
                Optional<IBEItemBehaviour> itemBehaviour = blockEntity.getItemBehaviour();
                itemBehaviour.ifPresent(IBEItemBehaviour::drops);
            }
        } else if (pNewState.getBlock() == Blocks.STRUCTURE_VOID){
            FlowCedarCasingBlockEntity blockEntity = (FlowCedarCasingBlockEntity) pLevel.getBlockEntity(pPos);
            if (blockEntity != null) {
                Optional<IBEItemBehaviour> itemBehaviour = blockEntity.getItemBehaviour();
                itemBehaviour.ifPresent(IBEItemBehaviour::drops);
            }
        }
    }
    @SuppressWarnings("deprecation")
    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState pState, LootParams.@NotNull Builder pParams) {
        List<ItemStack> drops = super.getDrops(pState,pParams);
        if (hasInputBus(pState)){
            drops.add(new ItemStack(TCItems.INPUT_BUS.get()));
        }
        if (hasOutputBus(pState)){
            drops.add(new ItemStack(TCItems.OUTPUT_BUS.get()));
        }
        drops.add(new ItemStack(TCItems.GOLD_ROD.get(), 4));
        return drops;
    }

    public static boolean hasInputBus(BlockState blockState){
        if (blockState.hasProperty(INPUT_BUS))
            return blockState.getValue(INPUT_BUS);
        return false;
    }
    public static boolean hasOutputBus(BlockState blockState) {
        if (blockState.hasProperty(OUTPUT_BUS))
            return blockState.getValue(OUTPUT_BUS);
        return false;
    }
    public static boolean hasInputBusConnection(BlockState blockState){
        if (blockState.hasProperty(INPUT_BUS_CONNECTION))
            return blockState.getValue(INPUT_BUS_CONNECTION);
        return false;
    }
    public static boolean hasOutputBusConnection(BlockState blockState){
        if (blockState.hasProperty(OUTPUT_BUS_CONNECTION))
            return blockState.getValue(OUTPUT_BUS_CONNECTION);
        return false;
    }

    public static boolean isPortAttached(Level level, BlockState blockState, BlockPos pos) {
        Direction facing = FunctionSide.getDirectionByFunctionSide(blockState);
        if (facing != Direction.DOWN){
            return level.getBlockState(pos.relative(facing)).is(TCBlocks.MATTER_INFUSER_PORT.get());
        }
        return false;
    }

    public static boolean isUnitAttached(Level level, BlockState blockState, BlockPos pos) {
        Direction facing = FunctionSide.getDirectionByFunctionSide(blockState);
        if (facing != Direction.DOWN){
            return level.getBlockState(pos.relative(facing)).is(TCBlocks.MATTER_INFUSER_IO.get());
        }
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return TCBlockEntities.FLOW_CEDAR_CASING_BE.get().create(blockPos,blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }
        return createTickerHelper(pBlockEntityType, TCBlockEntities.FLOW_CEDAR_CASING_BE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1,pPos,pState1));
    }

    static {
        INPUT_BUS = TCBlockStateProperties.INPUT_BUS;
        OUTPUT_BUS = TCBlockStateProperties.OUTPUT_BUS;
        FUNCTION_SIDE = TCBlockStateProperties.FUNCTION_SIDE;
        INPUT_BUS_CONNECTION = TCBlockStateProperties.INPUT_BUS_CONNECTION;
        OUTPUT_BUS_CONNECTION = TCBlockStateProperties.OUTPUT_BUS_CONNECTION;
    }
}
