package net.sinedkadis.terracompositio.block.custom;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.sinedkadis.terracompositio.util.HoldTorch;
import net.sinedkadis.terracompositio.util.helpers.PlayerHelper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

import java.util.Map;

import static net.sinedkadis.terracompositio.registries.TCBlockStateProperties.HOLD_TORCH;

@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FloatingTorchHolderBlock extends RedstoneTorchBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final Map<Direction, VoxelShape> AABBS = Maps.newEnumMap(ImmutableMap.of(
            Direction.NORTH,
            Block.box(5.5D, 3.0D, 11.0D, 10.5D, 13.0D, 16.0D),
            Direction.SOUTH,
            Block.box(5.5D, 3.0D, 0.0D, 10.5D, 13.0D, 5.0D),
            Direction.WEST,
            Block.box(11.0D, 3.0D, 5.5D, 16.0D, 13.0D, 10.5D),
            Direction.EAST,
            Block.box(0.0D, 3.0D, 5.5D, 5.0D, 13.0D, 10.5D)));
    private static final Map<Direction, VoxelShape> AABBS_EMPTY = Maps.newEnumMap(ImmutableMap.of(
            Direction.NORTH,
            Block.box(5.5D, 3.0D, 11.0D, 10.5D, 8.0D, 16.0D),
            Direction.SOUTH,
            Block.box(5.5D, 3.0D, 0.0D, 10.5D, 8.0D, 5.0D),
            Direction.WEST,
            Block.box(11.0D, 3.0D, 5.5D, 16.0D, 8.0D, 10.5D),
            Direction.EAST,
            Block.box(0.0D, 3.0D, 5.5D, 5.0D, 8.0D, 10.5D)));



    public FloatingTorchHolderBlock(Properties pProperties) {
        super(pProperties.lightLevel(blockstate -> switch (blockstate.getValue(HOLD_TORCH)) {
            case NORMAL -> 14;
            case REDSTONE -> 7;
            case SOUL -> 10;
            default -> 0;
        }));
        registerDefaultState(defaultBlockState().setValue(HOLD_TORCH, HoldTorch.NONE));
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction direction = pState.getValue(FACING);
        if (direction.equals(Direction.UP)) {
            return Shapes.empty();
        }
        if (direction.equals(Direction.DOWN)) {
            if (pState.getValue(HOLD_TORCH).isEmpty())
                return Block.box(6.0D, 1.0D, 6.0D, 10.0D, 3.0D, 10.0D);
            return super.getShape(pState, pLevel, pPos, pContext);
        } else {
            if (pState.getValue(HOLD_TORCH).isEmpty())
                return AABBS_EMPTY.get(direction);
            return AABBS.get(direction);
        }
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        return true;
    }
    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        if (state.getValue(HOLD_TORCH).isRedstoneTorch())
            return true;
        return super.canConnectRedstone(state,level,pos,direction);
    }

    @Override
    protected boolean hasNeighborSignal(Level pLevel, BlockPos pPos, BlockState pState) {
        Direction direction = pState.getValue(FACING).getOpposite();
        if (direction.getAxis().isVertical()) direction = direction.getOpposite();
        return pLevel.hasSignal(pPos.relative(direction), direction);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack itemInHand = pPlayer.getItemInHand(pHand);
        HoldTorch torch = pState.getValue(HOLD_TORCH);
        Map<HoldTorch,Item> map = Map.of(
                HoldTorch.REDSTONE,Items.REDSTONE_TORCH,
                HoldTorch.NORMAL,Items.TORCH,
                HoldTorch.SOUL,Items.SOUL_TORCH
        );
        if (torch.isEmpty()){
            for (Map.Entry<HoldTorch,Item> entry : map.entrySet()){
                if (itemInHand.is(entry.getValue())) {
                    pLevel.setBlockAndUpdate(pPos,pState.setValue(HOLD_TORCH,entry.getKey()));
                    if (!pPlayer.isCreative())
                        itemInHand.shrink(1);
                    pLevel.playSound(null,pPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        for (Map.Entry<HoldTorch,Item> entry : map.entrySet()){
            if (itemInHand.isEmpty() || itemInHand.is(entry.getValue())) {
                if (torch.equals(entry.getKey())){
                    PlayerHelper.addOrDropToPlayer(pPlayer, entry.getValue().getDefaultInstance());
                    pLevel.setBlockAndUpdate(pPos, pState.setValue(HOLD_TORCH, HoldTorch.NONE));
                    pLevel.playSound(null, pPos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(LIT,HOLD_TORCH,FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockState blockstate = this.defaultBlockState();
        LevelReader levelreader = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        Direction[] adirection = pContext.getNearestLookingDirections();
        Direction clickedFace = pContext.getClickedFace();
        if (clickedFace.getAxis().isVertical()){
            return blockstate.setValue(FACING,Direction.DOWN);
        }

        for(Direction direction : adirection) {
            if (direction.getAxis().isHorizontal()) {
                Direction direction1 = direction.getOpposite();
                blockstate = blockstate.setValue(FACING, direction1);
                if (blockstate.canSurvive(levelreader, blockpos)) {
                    return blockstate;
                }
            }
        }
        return blockstate.setValue(FACING,Direction.DOWN);
    }

    @Override
    public int getSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
        if (pBlockState.getValue(HOLD_TORCH).isRedstoneTorch()) {
            if (pBlockState.getValue(LIT)) {
                Direction direction = pBlockState.getValue(FACING);
                if (direction.getAxis().isVertical()) direction = direction.getOpposite();
                if (direction != pSide ) {
                    return 15;
                }
            }
            return 0;
        }
        return 0;
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (!pState.getValue(HOLD_TORCH).isRedstoneTorch()) return;
        super.tick(pState, pLevel, pPos, pRandom);
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        if (!pState.getValue(HOLD_TORCH).isRedstoneTorch()) return;
        super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        Direction direction = pState.getValue(FACING);
        HoldTorch torch = pState.getValue(HOLD_TORCH);

        double d0 = (double)pPos.getX() + 0.5D;
        double d1 = (double)pPos.getY() + 0.7D;
        double d2 = (double)pPos.getZ() + 0.5D;

        if (direction.equals(Direction.DOWN)) {
            switch (torch) {
                case REDSTONE -> super.animateTick(pState, pLevel, pPos, pRandom);
                case NORMAL -> {
                    pLevel.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                    pLevel.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                }
                case SOUL -> {
                    pLevel.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                    pLevel.addParticle(ParticleTypes.SOUL_FIRE_FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                }
            }
        }

        double d3 = 0.22D;
        double d4 = 0.27D;
        Direction direction1 = direction.getOpposite();

        switch (torch) {
            case REDSTONE -> pLevel.addParticle(DustParticleOptions.REDSTONE, d0 + d4 * (double)direction1.getStepX(), d1 + d3, d2 + d4 * (double)direction1.getStepZ(), 0.0D, 0.0D, 0.0D);
            case SOUL -> {
                pLevel.addParticle(ParticleTypes.SMOKE,
                        d0 + d4 * (double)direction1.getStepX(),
                        d1 + d3,
                        d2 + d4 * (double)direction1.getStepZ(),
                        0.0D,
                        0.0D,
                        0.0D);
                pLevel.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        d0 + d4 * (double)direction1.getStepX(),
                        d1 + d3,
                        d2 + d4 * (double)direction1.getStepZ(),
                        0.0D,
                        0.0D,
                        0.0D);

            }
            case NORMAL -> {
                pLevel.addParticle(ParticleTypes.SMOKE,
                        d0 + d4 * (double)direction1.getStepX(),
                        d1 + d3,
                        d2 + d4 * (double)direction1.getStepZ(),
                        0.0D,
                        0.0D,
                        0.0D);
                pLevel.addParticle(ParticleTypes.FLAME,
                        d0 + d4 * (double)direction1.getStepX(),
                        d1 + d3,
                        d2 + d4 * (double)direction1.getStepZ(),
                        0.0D,
                        0.0D,
                        0.0D);

            }

        }

    }

    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return Blocks.WALL_TORCH.rotate(pState, pRotation);
    }

    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return Blocks.WALL_TORCH.mirror(pState, pMirror);
    }
}
