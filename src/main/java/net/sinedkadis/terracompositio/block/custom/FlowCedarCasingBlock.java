package net.sinedkadis.terracompositio.block.custom;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.sinedkadis.terracompositio.block.entity.FlowCedarCasingBlockEntity;
import net.sinedkadis.terracompositio.registries.ModBlockEntities;
import net.sinedkadis.terracompositio.registries.ModBlockStateProperties;
import net.sinedkadis.terracompositio.registries.ModBlocks;
import net.sinedkadis.terracompositio.registries.ModItems;
import net.sinedkadis.terracompositio.util.FunctionSide;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.sinedkadis.terracompositio.block.custom.ModIOBaseEntityBlock.createTickerHelper;
import static net.sinedkadis.terracompositio.util.TCUtil.handleInWorldBlockCraft;

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
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(AXIS, pContext.getClickedFace().getAxis());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(AXIS, INPUT_BUS, OUTPUT_BUS, FUNCTION_SIDE, INPUT_BUS_CONNECTION, OUTPUT_BUS_CONNECTION, INFUSED);
    }

    @Override
    @ParametersAreNotNullByDefault
    public @NotNull InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack item = pPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        if (item.is(ModItems.INPUT_BUS.get()) && !hasInputBus(pState)) {
            return handleInWorldBlockCraft(pState, pState.setValue(INPUT_BUS, true), pLevel, pPos, item, 1);
        }
        if (item.is(ModItems.OUTPUT_BUS.get()) && !hasOutputBus(pState)) {
            return handleInWorldBlockCraft(pState, pState.setValue(OUTPUT_BUS, true), pLevel, pPos, item, 1);
        }
        BlockPos blockPos = pPos.relative(FunctionSide.getDirectionByFunctionSide(pState));
        Item item1 = ModItems.INFUSED_IRON_ROD.get();
        BlockState blockState1 = pLevel.getBlockState(blockPos);
        if (item.is(item1) && hasInputBus(pState) && !hasInputBusConnection(pState) && blockState1.hasProperty(ModBlockStateProperties.UP_CONNECTION)){
            pLevel.setBlock(pPos,pState.setValue(INPUT_BUS_CONNECTION,true),3);
            return handleInWorldBlockCraft(blockState1, blockState1.setValue(ModBlockStateProperties.UP_CONNECTION, true), pLevel, blockPos, item, 1);
        }
        if (item.is(item1) && hasOutputBus(pState) && !hasOutputBusConnection(pState) && blockState1.hasProperty(ModBlockStateProperties.DOWN_CONNECTION)){
            pLevel.setBlock(pPos,pState.setValue(OUTPUT_BUS_CONNECTION,true),3);
            return handleInWorldBlockCraft(blockState1, blockState1.setValue(ModBlockStateProperties.DOWN_CONNECTION, true), pLevel, blockPos, item, 1);
        }
        if (isPortAttached(pLevel,pState,pPos) && pPos != pHit.getBlockPos() && pHand != InteractionHand.OFF_HAND){
            FlowCedarCasingBlockEntity blockEntity = (FlowCedarCasingBlockEntity) pLevel.getBlockEntity(pPos);
            assert blockEntity != null;
            ItemStack inputSlot = blockEntity.getFirstSlot();
            if (inputSlot.isEmpty()) {
                if (!item.isEmpty()) {
                    blockEntity.insertItemStack(0, item);
                    item.shrink(1);
                    pLevel.playSound(pPlayer, pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS);
                    return InteractionResult.SUCCESS;
                }
            } else if (!inputSlot.isEmpty()) {
                if (!pPlayer.addItem(inputSlot)) {
                    pPlayer.drop(inputSlot, false);
                }
                blockEntity.setSlotEmpty(0);
                pLevel.playSound(pPlayer, pPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS);
                return InteractionResult.SUCCESS;
            }
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public void onRemove(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pIsMoving) {
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        if (hasInputBus(pState) && !hasInputBus(pNewState)){
            pLevel.addFreshEntity(new ItemEntity(pLevel, pPos.getX(),pPos.getY(),pPos.getZ(), new ItemStack(ModItems.INPUT_BUS.get())));
        }
        if (hasInputBusConnection(pState) && !hasInputBusConnection(pNewState)){
            pLevel.addFreshEntity(new ItemEntity(pLevel, pPos.getX(),pPos.getY(),pPos.getZ(), new ItemStack(ModItems.INFUSED_IRON_ROD.get())));
        }
        if (hasOutputBus(pState) && !hasOutputBus(pNewState)){
            pLevel.addFreshEntity(new ItemEntity(pLevel, pPos.getX(),pPos.getY(),pPos.getZ(), new ItemStack(ModItems.OUTPUT_BUS.get())));
        }
        if (hasOutputBusConnection(pState) && !hasOutputBusConnection(pNewState)){
            pLevel.addFreshEntity(new ItemEntity(pLevel, pPos.getX(),pPos.getY(),pPos.getZ(), new ItemStack(ModItems.INFUSED_IRON_ROD.get())));
        }
        if (pState.getBlock() != pNewState.getBlock()) {
            pLevel.addFreshEntity(new ItemEntity(pLevel, pPos.getX(), pPos.getY(), pPos.getZ(), new ItemStack(ModItems.GOLD_ROD.get(), 4)));
            Direction direction = FunctionSide.getDirectionByFunctionSide(pState);
            if (direction != Direction.DOWN) {
                BlockPos blockPos = pPos.relative(direction);
                BlockState blockState = pLevel.getBlockState(blockPos);
                if (blockState.is(ModBlocks.MATTER_INFUSER_PORT.get())
                        || blockState.is(ModBlocks.MATTER_INFUSER_IO.get())) {
                    pLevel.destroyBlock(blockPos, true);
                }
            }
            FlowCedarCasingBlockEntity blockEntity = (FlowCedarCasingBlockEntity) pLevel.getBlockEntity(pPos);
            if (blockEntity != null) {
                blockEntity.drops();
            }
        }
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
            return level.getBlockState(pos.relative(facing)).is(ModBlocks.MATTER_INFUSER_PORT.get());
        }
        return false;
    }

    public static boolean isUnitAttached(Level level, BlockState blockState, BlockPos pos) {
        Direction facing = FunctionSide.getDirectionByFunctionSide(blockState);
        if (facing != Direction.DOWN){
            return level.getBlockState(pos.relative(facing)).is(ModBlocks.MATTER_INFUSER_IO.get());
        }
        return false;
    }

    @Nullable
    @Override
    @ParametersAreNotNullByDefault
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ModBlockEntities.FLOW_CEDAR_CASING_BE.get().create(blockPos,blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }
        return createTickerHelper(pBlockEntityType, ModBlockEntities.FLOW_CEDAR_CASING_BE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1,pPos,pState1));
    }

    static {
        INPUT_BUS = ModBlockStateProperties.INPUT_BUS;
        OUTPUT_BUS = ModBlockStateProperties.OUTPUT_BUS;
        FUNCTION_SIDE = ModBlockStateProperties.FUNCTION_SIDE;
        INPUT_BUS_CONNECTION = ModBlockStateProperties.INPUT_BUS_CONNECTION;
        OUTPUT_BUS_CONNECTION = ModBlockStateProperties.OUTPUT_BUS_CONNECTION;
    }
}
