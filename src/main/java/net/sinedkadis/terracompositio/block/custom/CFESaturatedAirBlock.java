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
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
public class CFESaturatedAirBlock extends TCCFEBaseEntityBlock {
    public CFESaturatedAirBlock(Properties pProperties) {
        super(pProperties);
    }

    public static int placeCFECloud(Level pLevel, BlockPos targetPos, int cfe) {
        if (pLevel.isClientSide()) return cfe;

        int oCFE = cfe;
        int lastListSize = -1;


        Set<BlockPos> visited = new HashSet<>();

        for (int i = 1; cfe > 0; i++){
            List<BlockPos> list = TCUtil.getTouchingBlocks(pLevel, targetPos, BlockStateBase::isAir, i);
            if (lastListSize != -1 && lastListSize == list.size()) return oCFE - cfe;
            for (BlockPos pos : list) {
                if (visited.contains(pos)) continue;

                BlockEntity blockEntity = pLevel.getBlockEntity(pos);
                if ((blockEntity instanceof CFESaturatedAirBlockEntity airBE)) {
                    ICFEHandler cfeContainer = airBE.getCfeContainer();
                    if (cfeContainer.getFreeSpace()>0) {
                        int cfeWas = cfeContainer.getQueued() + cfeContainer.getCFE();
                        int set = Math.min(cfe + cfeWas, cfeContainer.getFreeSpace());
                        cfeContainer.setCFE(set);
                        cfe -= set-cfeWas;
                    }
                }
                else {
                    pLevel.setBlockAndUpdate(pos, TCBlocks.CFE_SATURATED_AIR.get().defaultBlockState());
                    blockEntity = pLevel.getBlockEntity(pos);
                    if (blockEntity instanceof CFESaturatedAirBlockEntity airBE) {
                        int added = Math.min(cfe, airBE.getCfeContainer().getFreeSpace());
                        airBE.getCfeContainer().setCFE(added);
                        cfe -= added;
                    }
                }

                visited.add(pos);
                if (cfe <= 0) break;
            }
            lastListSize = list.size();
        }

        return oCFE - cfe;
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



    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
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
