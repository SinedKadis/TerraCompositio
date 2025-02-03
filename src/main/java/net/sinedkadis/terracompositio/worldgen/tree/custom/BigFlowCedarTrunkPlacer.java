package net.sinedkadis.terracompositio.worldgen.tree.custom;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.sinedkadis.terracompositio.block.ModBlocks;
import net.sinedkadis.terracompositio.worldgen.tree.ModTrunkPlacerTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class BigFlowCedarTrunkPlacer extends TrunkPlacer {
    public static final Codec<BigFlowCedarTrunkPlacer> CODEC = RecordCodecBuilder.create(bigFlowCedarTrunkPlacerInstance ->
            trunkPlacerParts(bigFlowCedarTrunkPlacerInstance).apply(bigFlowCedarTrunkPlacerInstance, BigFlowCedarTrunkPlacer::new));

    public BigFlowCedarTrunkPlacer(int pBaseHeight, int pHeightRandA, int pHeightRandB) {
        super(pBaseHeight, pHeightRandA, pHeightRandB);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return ModTrunkPlacerTypes.BIG_FLOW_CEDAR_TRUNK_PLACER.get();
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader pLevel, BiConsumer<BlockPos, BlockState> pBlockSetter,
                                                            RandomSource pRandom, int pFreeTreeHeight, BlockPos pPos, TreeConfiguration pConfig) {
        // THIS WHERE BLOCK PLACING LOGIC GOES
        setDirtAt(pLevel, pBlockSetter, pRandom, pPos.below(), pConfig);
        int height = 5;
        List<Integer> bounds = getSlice(pRandom);
        int x0 = pRandom.nextInt(bounds.get(2),bounds.get(3));
        int z0 = pRandom.nextInt(bounds.get(0),bounds.get(1));
        int x1 = random(bounds.get(2),bounds.get(3));
        int z1 = random(bounds.get(0),bounds.get(1));
        placeLogForce(pLevel, pBlockSetter, pRandom, pPos.relative(Direction.NORTH,x0*2).relative(Direction.EAST,z0*2).below(), pConfig);
        placeLogForce(pLevel, pBlockSetter, pRandom, pPos.relative(Direction.NORTH,x0*2).relative(Direction.EAST,z0*2), pConfig);
        placeLogForce(pLevel, pBlockSetter, pRandom, pPos.relative(Direction.SOUTH,x1*2).relative(Direction.EAST,z1*2).below(), pConfig);
        placeLogForce(pLevel, pBlockSetter, pRandom, pPos.relative(Direction.SOUTH,x1*2).relative(Direction.EAST,z1*2), pConfig);
        placeLogForce(pLevel, pBlockSetter, pRandom, pPos.relative(Direction.SOUTH,x1*2).relative(Direction.EAST,z1*2).above(), pConfig);
        for(int y = 0; y < height; y++) {
            for(int x = bounds.get(0); x <= bounds.get(1); x++) {
                for(int z = bounds.get(2); z <= bounds.get(3); z++) {
                    if (height/2 == y) {
                        if (z == 0 && x == 0) {
                            pBlockSetter.accept(pPos.above(y),
                                    ModBlocks.FLOW_PORT.get().defaultBlockState());
                            continue;
                        }
                    }
                    placeLog(pLevel, pBlockSetter, pRandom, pPos.above(y).relative(Direction.EAST,x).relative(Direction.SOUTH,z), pConfig);
                }
            }
            placeLog(pLevel, pBlockSetter, pRandom, pPos.above(y), pConfig);




            if (y == height-1) {
                if (pRandom.nextFloat() > 0.2f) {
                    int a = pRandom.nextInt(-1, 1);
                    for (int i = 0; i < random(4); i++) {
                        pBlockSetter.accept(pPos.above(y).relative(Direction.Axis.X, bounds.get(2)).relative(Direction.Axis.X, i).relative(Direction.Axis.Z, a), ((BlockState)
                                Function.identity().apply(pConfig.trunkProvider.getState(pRandom, pPos).setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z))));
                    }
                }


                if (pRandom.nextFloat() > 0.2f) {
                    int a = pRandom.nextInt(-1, 1);
                    for (int i = 0; i < random(4); i++) {
                        pBlockSetter.accept(pPos.above(y).relative(Direction.Axis.Z, bounds.get(3)).relative(Direction.Axis.X, i*-1).relative(Direction.Axis.Z, a), ((BlockState)
                                Function.identity().apply(pConfig.trunkProvider.getState(pRandom, pPos).setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z))));
                    }
                }


                if (pRandom.nextFloat() > 0.2f) {
                    int a = pRandom.nextInt(-1, 1);
                    for (int i = 0; i < random(4); i++) {
                        pBlockSetter.accept(pPos.above(y).relative(Direction.Axis.Z, bounds.get(0)).relative(Direction.Axis.Z, i).relative(Direction.Axis.X, a), ((BlockState)
                                Function.identity().apply(pConfig.trunkProvider.getState(pRandom, pPos).setValue(RotatedPillarBlock.AXIS, Direction.Axis.X))));
                    }
                }

                if (pRandom.nextFloat() > 0.2f) {
                    int a = pRandom.nextInt(-1, 1);
                    for (int i = 0; i < random(4); i++) {
                        pBlockSetter.accept(pPos.above(y).relative(Direction.Axis.Z, bounds.get(1)).relative(Direction.Axis.Z, i*-1).relative(Direction.Axis.X, a), ((BlockState)
                                Function.identity().apply(pConfig.trunkProvider.getState(pRandom, pPos).setValue(RotatedPillarBlock.AXIS, Direction.Axis.X))));
                    }
                }
            }
        }

        return ImmutableList.of(new FoliagePlacer.FoliageAttachment(pPos.above(height-3), 0, false));
    }

    private void placeLogForce(LevelSimulatedReader pLevel, BiConsumer<BlockPos, BlockState> pBlockSetter, RandomSource pRandom, BlockPos pPos, TreeConfiguration pConfig) {
        pBlockSetter.accept(pPos, (BlockState)Function.identity().apply(pConfig.trunkProvider.getState(pRandom, pPos)));
    }
    private List<Integer> getSlice(RandomSource pRandom){
        int rand = pRandom.nextInt(0,3);
        switch (rand){
            case 1 -> {
                List<Integer> toReturn = new ArrayList<>();
                toReturn.add(-1);
                toReturn.add(0);
                toReturn.add(-1);
                toReturn.add(1);
                return toReturn;
            }
            case 2 -> {
                List<Integer> toReturn = new ArrayList<>();
                toReturn.add(-1);
                toReturn.add(1);
                toReturn.add(0);
                toReturn.add(1);
                return toReturn;
            }
            case 3 -> {
                List<Integer> toReturn = new ArrayList<>();
                toReturn.add(-1);
                toReturn.add(1);
                toReturn.add(-1);
                toReturn.add(0);
                return toReturn;
            }
            default -> {
                List<Integer> toReturn = new ArrayList<>();
                toReturn.add(0);
                toReturn.add(1);
                toReturn.add(-1);
                toReturn.add(1);
                return toReturn;
            }
        }
    }
    private int random(int origin,int bound){
        if (origin >= bound) {
            throw new IllegalArgumentException("bound - origin is non positive");
        } else {
            return origin + this.random(bound - origin);
        }
    }
    private int random(int bound){
        return (int)(Math.round(Math.random()*bound));
    }

}
