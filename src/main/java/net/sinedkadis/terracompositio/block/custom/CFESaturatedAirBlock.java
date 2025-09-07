package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.entity.CFESaturatedAirBlockEntity;
import net.sinedkadis.terracompositio.block.entity.TCCFEBlockEntity;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
public class CFESaturatedAirBlock extends TCCFEBaseEntityBlock {
    public CFESaturatedAirBlock(Properties pProperties) {
        super(pProperties);
    }

    public static void placeCFECloud(Level pLevel, BlockPos airPos, int cfe) {
        if (cfe <= 100){
        pLevel.setBlockAndUpdate(airPos, TCBlocks.CFE_SATURATED_AIR.get().defaultBlockState());
        CFESaturatedAirBlockEntity blockEntity = (CFESaturatedAirBlockEntity) pLevel.getBlockEntity(airPos);
        assert blockEntity != null;
        blockEntity.getCfeContainer().setCFE(cfe);
        } else {
            List<BlockPos> list = TCUtil.getTouchingBlocks(pLevel,airPos, BlockStateBase::isAir,10);

            List<ICFEHandler> icfeHandlers = list.stream()
                    .filter(blockPos -> (pLevel.getBlockState(blockPos).is(TCBlocks.CFE_SATURATED_AIR.get())))
                    .map(pLevel::getBlockEntity)
                    .filter(Objects::nonNull)
                    .map(blockEntity1 -> ((CFESaturatedAirBlockEntity) blockEntity1))
                    .map(TCCFEBlockEntity::getCfeContainer)
                    .toList();
            for (ICFEHandler cfeContainer : icfeHandlers){
                if (cfe > 0) {
                    int added = cfeContainer.addCFE(cfe,cfeContainer, false,true);
                    cfe -= added;
                } else break;
            }
            if (cfe > 0) {
                List<BlockPos> emptyAirs = list.stream()
                        .filter(blockPos -> !(pLevel.getBlockState(blockPos).is(TCBlocks.CFE_SATURATED_AIR.get())))
                        .toList();
                for (BlockPos blockPos : emptyAirs) {
                    if (cfe >= 100){
                        pLevel.setBlockAndUpdate(blockPos, TCBlocks.CFE_SATURATED_AIR.get().defaultBlockState());
                        cfe -= 100;
                    } else {
                        pLevel.setBlockAndUpdate(blockPos, TCBlocks.CFE_SATURATED_AIR.get().defaultBlockState());
                        CFESaturatedAirBlockEntity blockEntity = (CFESaturatedAirBlockEntity) pLevel.getBlockEntity(blockPos);
                        if (blockEntity != null){
                            ICFEHandler cfeContainer = blockEntity.getCfeContainer();
                            cfeContainer.setCFE(0);
                            int added = cfeContainer.addCFE(cfe,cfeContainer,false,true);
                            cfe -= added;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected boolean isAir(BlockState state) {
        return true;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Block.box(0,0,0,0,0,0);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return true;
    }

    @Override
    public @Nullable PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return TCBlockEntities.CFE_SATURATED_AIR_BE.get().create(pPos, pState);
    }

    private int firstSecond = 20;

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (firstSecond > 1){
            firstSecond--;
            return;
        }
        if (level.getBlockEntity(pos) instanceof CFESaturatedAirBlockEntity blockEntity) {
            blockEntity.spawnParticles(level, pos, random);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }
        return createTickerHelper(pBlockEntityType, TCBlockEntities.CFE_SATURATED_AIR_BE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1,pPos,pState1));
    }

}
