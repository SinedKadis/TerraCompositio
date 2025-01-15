package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.network.NetworkHooks;
import net.sinedkadis.terracompositio.block.ModBlockStateProperties;
import net.sinedkadis.terracompositio.block.ModBlocks;
import net.sinedkadis.terracompositio.block.entity.FlowInfuserBlockEntity;
import net.sinedkadis.terracompositio.block.entity.FlowPortBlockEntity;
import net.sinedkadis.terracompositio.block.entity.ModCFEBlockEntity;
import net.sinedkadis.terracompositio.block.entity.ModBlockEntities;
import net.sinedkadis.terracompositio.util.IBE;
import net.sinedkadis.terracompositio.util.ModGameRules;
import net.sinedkadis.terracompositio.util.ModTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static net.sinedkadis.terracompositio.block.custom.FlowCedarLikeBlock.getNearBlocks;

public class FlowInfuserBlock extends BaseEntityBlock implements IBE<FlowInfuserBlockEntity> {
    public static BooleanProperty INFUSED;

    public FlowInfuserBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue(INFUSED, false));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(INFUSED);
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return !state.getValue(INFUSED);
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(INFUSED) ? 0 : 5;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(INFUSED) ? 0 : 5;
    }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        Map<BlockState,BlockState> stripPair = new HashMap<>();

        stripPair.put(
                ModBlocks.FLOW_INFUSER.get().defaultBlockState(),
                ModBlocks.STRIPPED_FLOW_CEDAR_LOG.get().defaultBlockState());

        if(context.getItemInHand().getItem() instanceof AxeItem){
            if (stripPair.containsKey(state))
                return stripPair.get(state)
                        .setValue(INFUSED,state.getValue(INFUSED));
        }
        return super.getToolModifiedState(state, context,toolAction,simulate);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        if (pState.getBlock() != pNewState.getBlock()
                && pState.getValue(INFUSED) != pNewState.getValue(INFUSED)){
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof FlowInfuserBlockEntity){
                ((FlowInfuserBlockEntity) blockEntity).drops();
            }
        }
        if (pNewState.is(Blocks.AIR)) {
            if (pState.getValue(INFUSED)&&!pLevel.getGameRules().getBoolean(ModGameRules.DISABLE_FLOW_LEAKING)) {
                BlockPos fpos = pPos.relative(Direction.UP,1);
                BlockPos bpos = pPos.relative(Direction.DOWN,-1);
                getNearBlocks(fpos).stream()
                        .filter(pos -> pos != pPos)
                        .filter(pos -> pLevel.getBlockState(pos).is(ModTags.Blocks.FLOW_LEAKABLE))
                        .forEach(pos -> pLevel.setBlockAndUpdate(pos,pLevel.getBlockState(pos).setValue(INFUSED,false)));
                getNearBlocks(bpos).stream()
                        .filter(pos -> pos != pPos)
                        .filter(pos -> pLevel.getBlockState(pos).is(ModTags.Blocks.FLOW_LEAKABLE))
                        .forEach(pos -> pLevel.setBlockAndUpdate(pos,pLevel.getBlockState(pos).setValue(INFUSED,false)));
            }
        }
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        //if (!pLevel.isClientSide()){
        BlockEntity entity = pLevel.getBlockEntity(pPos);
        if(entity instanceof FlowInfuserBlockEntity && pState.getValue(INFUSED)){
            ItemStack itemstack = pPlayer.getItemInHand(pHand);
            ItemStack outputSlot = ModBlockEntities.FLOW_INFUSER_BE.get().getBlockEntity(pLevel,pPos).getOutputSlot();
            ItemStack inputSlot = ModBlockEntities.FLOW_INFUSER_BE.get().getBlockEntity(pLevel,pPos).getInputSlot();
            if (outputSlot.isEmpty() && inputSlot.isEmpty()){
                if(!itemstack.isEmpty()) {
                    ModBlockEntities.FLOW_INFUSER_BE.get().getBlockEntity(pLevel,pPos).addItemInSlot(0,itemstack,1);
                    pLevel.playSound(pPlayer,pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS);
                }
            }else if (!outputSlot.isEmpty()){
                if (!pPlayer.addItem(outputSlot)) {
                    pPlayer.drop(outputSlot, false);
                }
                ModBlockEntities.FLOW_INFUSER_BE.get().getBlockEntity(pLevel,pPos).setSlotEmpty(1);
                pLevel.playSound(pPlayer,pPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS);
            } else if (!inputSlot.isEmpty()){
                if (!pPlayer.addItem(inputSlot)) {
                    pPlayer.drop(inputSlot, false);
                }
                ModBlockEntities.FLOW_INFUSER_BE.get().getBlockEntity(pLevel,pPos).setSlotEmpty(0);
                pLevel.playSound(pPlayer,pPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS);
            }
        }
        //}
        return pState.getValue(INFUSED) ? InteractionResult.sidedSuccess(pLevel.isClientSide()) : InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ModBlockEntities.FLOW_INFUSER_BE.get().create(blockPos, blockState).markVirtual();
    }

    @Override
    public Class<FlowInfuserBlockEntity> getBlockEntityClass() {
        return FlowInfuserBlockEntity.class;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }
        return createTickerHelper(pBlockEntityType, ModBlockEntities.FLOW_INFUSER_BE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1,pPos,pState1));
    }

    static {
        INFUSED = ModBlockStateProperties.INFUSED;
    }
}
