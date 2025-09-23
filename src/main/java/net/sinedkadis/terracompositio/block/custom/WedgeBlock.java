package net.sinedkadis.terracompositio.block.custom;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.sinedkadis.terracompositio.block.entity.EntStatueBlockEntity;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.registries.TCFluids;
import net.sinedkadis.terracompositio.registries.TCParticles;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL;
import static net.sinedkadis.terracompositio.registries.TCBlockStateProperties.INFUSED;

@SuppressWarnings("deprecation")
public class WedgeBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
    protected static final VoxelShape NORTH_AABB = Block.box(6.0D, 5.0D, 10.0D, 10.0D, 10.0D, 16.0D);
    protected static final VoxelShape SOUTH_AABB = Block.box(6.0D, 5.0D, 0.0D, 10.0D, 10.0D, 6.0D);
    protected static final VoxelShape WEST_AABB = Block.box(10.0D, 5.0D, 6.0D, 16.0D, 10.0D, 10.0D);
    protected static final VoxelShape EAST_AABB = Block.box(0.0D, 5.0D, 6.0D, 6.0D, 10.0D, 10.0D);
    private int AnimTick = 0;
    private static final VoxelShape REQUIRED_SPACE_TO_DRIP_THROUGH_NON_SOLID_BLOCK = Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);
    //private static final Logger LOGGER = LogUtils.getLogger();
//TODO achievement don't repeat at home(stack multiple on each other)
    public WedgeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ATTACHED,false));
    }
    @Nullable
    private static BlockPos findFillableCauldronBelowWedge(Level pLevel, BlockPos pPos, Fluid pFluid) {
        Predicate<BlockState> isCauldron = (blockState) -> (blockState.getBlock() instanceof TCCauldronBlock && ((TCCauldronBlock) blockState.getBlock()).canReceiveWedgeDrip(pFluid))
                ||blockState.is(Blocks.CAULDRON)
                ||(blockState.is(TCBlocks.FLOW_CEDAR_ENT_STATUE.get()) && pFluid == TCFluids.FLOW_FLUID.source.get());
        BiPredicate<BlockPos, BlockState> isDripableThrough = (blockPos, blockState) -> canDripThrough(pLevel, blockPos, blockState);
        return findBlockVertical(pLevel, pPos, Direction.DOWN.getAxisDirection(), isDripableThrough, isCauldron).orElse(null);
    }
    private static Optional<BlockPos> findBlockVertical(LevelAccessor pLevel, BlockPos pPos, Direction.AxisDirection pAxis, BiPredicate<BlockPos, BlockState> isDripableThrough, Predicate<BlockState> isCauldron) {
        Direction direction = Direction.get(pAxis, Direction.Axis.Y);
        BlockPos.MutableBlockPos checkPos = pPos.mutable();

        for(int i = 1; i < 11; ++i) {
            checkPos.move(direction);
            BlockState blockStateAt = pLevel.getBlockState(checkPos);
            if (isCauldron.test(blockStateAt)) {
                return Optional.of(checkPos.immutable());
            }

            if (pLevel.isOutsideBuildHeight(checkPos.getY()) || !isDripableThrough.test(checkPos, blockStateAt)) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }
    private static boolean canDripThrough(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
        if (pState.isAir()) {
            return true;
        } else if (pState.isSolidRender(pLevel, pPos)) {
            return false;
        } else if (!pState.getFluidState().isEmpty()) {
            return false;
        } else {
            VoxelShape $$3 = pState.getCollisionShape(pLevel, pPos);
            return !Shapes.joinIsNotEmpty(REQUIRED_SPACE_TO_DRIP_THROUGH_NON_SOLID_BLOCK, $$3, BooleanOp.AND);
        }
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return switch (pState.getValue(FACING)) {
            case WEST -> WEST_AABB;
            case SOUTH -> SOUTH_AABB;
            case NORTH -> NORTH_AABB;
            default -> EAST_AABB;
        };
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        Direction direction = pState.getValue(FACING);
        BlockPos blockpos = pPos.relative(direction.getOpposite());
        BlockState blockstate = pLevel.getBlockState(blockpos);
        return direction.getAxis().isHorizontal() && blockstate.isFaceSturdy(pLevel, blockpos, direction);
    }

    @Override
    public @NotNull BlockState updateShape(BlockState pState, Direction pFacing, @NotNull BlockState pFacingState, @NotNull LevelAccessor pLevel, @NotNull BlockPos pCurrentPos, @NotNull BlockPos pFacingPos) {
        return pFacing.getOpposite() == pState.getValue(FACING) && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockState blockstate = this.defaultBlockState().setValue(ATTACHED,false);
        LevelReader levelreader = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        Direction[] adirection = pContext.getNearestLookingDirections();

        for(Direction direction : adirection) {
            if (direction.getAxis().isHorizontal()) {
                Direction direction1 = direction.getOpposite();
                blockstate = blockstate.setValue(FACING, direction1);
                if (blockstate.canSurvive(levelreader, blockpos)) {
                    return blockstate;
                }
            }
        }

        return null;
    }

    @Override
    public boolean isRandomlyTicking(@NotNull BlockState pState) {
        return true;
    }

    @Override
    @ParametersAreNotNullByDefault
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        super.animateTick(pState, pLevel, pPos, pRandom);
        if (pState.getValue(ATTACHED)) {
            if (AnimTick++ > 20) {
                AnimTick = 0;
                if (pLevel.getBlockState(pPos.relative(pState.getValue(FACING).getOpposite())).hasProperty(INFUSED)
                        && pLevel.getBlockState(pPos.relative(pState.getValue(FACING).getOpposite())).getValue(INFUSED)) {
                    if (pLevel.isClientSide) {
                        generateParticles(pLevel, pPos, pState, TCParticles.FLOW_PARTICLE.get());
                    }
                }
                if (pLevel.getBlockState(pPos.relative(pState.getValue(FACING).getOpposite())).is(BlockTags.BIRCH_LOGS)) {
                    if (pLevel.isClientSide) {
                        generateParticles(pLevel, pPos, pState, TCParticles.BIRCH_JUICE_PARTICLE.get());
                    }
                }
            }
        }

    }

    @Override
    public void tick(@NotNull BlockState pState, @NotNull ServerLevel pLevel, @NotNull BlockPos pPos, @NotNull RandomSource pRandom) {
        this.calculateState(pState, pLevel, pPos);
        if (pState.getValue(ATTACHED)) {
            double random = Math.random();
            BlockPos cauldronPos = findFillableCauldronBelowWedge(pLevel, pPos, TCFluids.FLOW_FLUID.source.get());
            BlockPos cauldronPos1 = findFillableCauldronBelowWedge(pLevel, pPos, TCFluids.BIRCH_JUICE_FLUID.source.get());
            if (cauldronPos != null||cauldronPos1 != null) {
                if (pLevel.getBlockState(pPos.relative(pState.getValue(FACING).getOpposite())).hasProperty(INFUSED)
                        && pLevel.getBlockState(pPos.relative(pState.getValue(FACING).getOpposite())).getValue(INFUSED)
                        && cauldronPos != null) {
                    if (pLevel.getBlockState(cauldronPos).is(TCBlocks.FLOW_CAULDRON.get())) {
                        int levelValue = pLevel.getBlockState(cauldronPos).getValue(LEVEL);
                        if (random < 0.7D) {
                            if (levelValue != 3) {
                                pLevel.setBlock(cauldronPos, TCBlocks.FLOW_CAULDRON.get().defaultBlockState().setValue(LEVEL, levelValue + 1), 2);

                            }
                        }
                    } else if (pLevel.getBlockState(cauldronPos).is(Blocks.CAULDRON)) {
                        if (random < 0.7D) {
                            pLevel.setBlock(cauldronPos, TCBlocks.FLOW_CAULDRON.get().defaultBlockState().setValue(LEVEL, 1), 2);
                        }
                    } else if (pLevel.getBlockState(cauldronPos).is(TCBlocks.FLOW_CEDAR_ENT_STATUE.get())) {
                        if (random < 0.3D) {
                            BlockEntity blockEntity = pLevel.getBlockEntity(cauldronPos);
                            if (blockEntity instanceof EntStatueBlockEntity entStatueBlockEntity) {
                                entStatueBlockEntity.jojoReference();
                            }
                        }
                    }

                }
                if (pLevel.getBlockState(pPos.relative(pState.getValue(FACING).getOpposite())).is(BlockTags.BIRCH_LOGS)
                        && cauldronPos1 != null) {
                    if (pLevel.getBlockState(cauldronPos1).is(TCBlocks.BIRCH_JUICE_CAULDRON.get())) {
                        //LOGGER.debug("Birch Juice Cauldron detected, trying increase level");
                        int levelValue = pLevel.getBlockState(cauldronPos1).getValue(LEVEL);
                        if (random < 0.7D) {
                            if (levelValue != 3) {
                                pLevel.setBlock(cauldronPos1, TCBlocks.BIRCH_JUICE_CAULDRON.get().defaultBlockState().setValue(LEVEL, levelValue + 1), 2);
                                //LOGGER.debug(random + " - Success " + success);
                            } //else LOGGER.debug(random + " - Fail");
                        }
                    } else if (pLevel.getBlockState(cauldronPos1).is(Blocks.CAULDRON)) {
                        //LOGGER.debug("Vanilla Cauldron detected, trying increase level");
                        if (random < 0.7D) {
                            pLevel.setBlock(cauldronPos1, TCBlocks.BIRCH_JUICE_CAULDRON.get().defaultBlockState().setValue(LEVEL, 1), 2);
                            //LOGGER.debug(random + " - Success " + success);
                        } //else LOGGER.debug(random + " - Fail");
                    }
                }
            }
        }
    }



    @OnlyIn(Dist.CLIENT)
    private void generateParticles(Level pLevel, BlockPos pPos, BlockState pState, ParticleOptions particle) {
        //for (int i = 0; i < 5; i++) {
        float x;
        float z;
        switch (pState.getValue(FACING)){
            case NORTH -> {
                x = 0.5f;
                z = 0.7f;
            }
            case SOUTH -> {
                x = 0.5f;
                z = 0.3f;
            }
            case EAST -> {
                x = 0.3f;
                z = 0.5f;
            }
            case WEST -> {
                x = 0.7f;
                z = 0.5f;
            }
            default -> {
                x = 52;
                z = 52;
            }
        }
            pLevel.addParticle(particle,
                    pPos.getX() + x,
                    pPos.getY()+0.4f,
                    pPos.getZ() + z,
                    0,0,0);
        //}
    }

    @Override
    public void setPlacedBy(@NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, @org.jetbrains.annotations.Nullable LivingEntity pPlacer, @NotNull ItemStack pStack) {
        this.calculateState(pState,pLevel,pPos);
    }

    public void calculateState(BlockState pState, Level pLevel, BlockPos pPos) {
        if (pLevel.getBlockState(pPos.relative(pState.getValue(FACING).getOpposite())).hasProperty(INFUSED)
                && pLevel.getBlockState(pPos.relative(pState.getValue(FACING).getOpposite())).getValue(INFUSED)
                || pLevel.getBlockState(pPos.relative(pState.getValue(FACING).getOpposite())).is(BlockTags.BIRCH_LOGS)){
            if (!pState.getValue(ATTACHED)){
                pLevel.setBlockAndUpdate(pPos,this.defaultBlockState().setValue(FACING,pState.getValue(FACING)).setValue(ATTACHED,true));

            }
        }else if (pState.getValue(ATTACHED)){
            pLevel.setBlockAndUpdate(pPos,this.defaultBlockState().setValue(FACING,pState.getValue(FACING)).setValue(ATTACHED,false));
        }
    }

    @Override
    public @NotNull BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, ATTACHED);
    }

    @Override
    public void onRemove(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pIsMoving) {
        if (!pIsMoving && !pState.is(pNewState.getBlock())) {
            //if (pState.getValue(ATTACHED)) {
            //    this.calculateState(pState,pLevel, pPos, false);
            //}
            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }
    }
}
