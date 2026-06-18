package net.sinedkadis.terracompositio.worldgen.tree.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.sinedkadis.terracompositio.worldgen.tree.TCTrunkPlacers;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class FlowCedarTrunkPlacer extends TrunkPlacer {
    public static final Codec<FlowCedarTrunkPlacer> CODEC = RecordCodecBuilder.create(
            instance -> trunkPlacerParts(instance)
                    .apply(instance, FlowCedarTrunkPlacer::new)
    );

    public FlowCedarTrunkPlacer(int baseHeight, int heightRandA, int heightRandB) {
        super(baseHeight, heightRandA, heightRandB);
    }

    @Override
    protected @NotNull TrunkPlacerType<?> type() {
        return TCTrunkPlacers.FLOW_CEDAR_TRUNK_PLACER.get();
    }

    @Override
    public @NotNull List<FoliagePlacer.FoliageAttachment> placeTrunk(
            @NotNull LevelSimulatedReader pLevel,
            @NotNull BiConsumer<BlockPos, BlockState> pBlockSetter,
            @NotNull RandomSource pRandom,
            int pFreeTreeHeight,
            @NotNull BlockPos pPos,
            @NotNull TreeConfiguration pConfig) {

        List<FoliagePlacer.FoliageAttachment> foliageAttachments = new ArrayList<>();

        // Основной ствол
        for (int i = 0; i < pFreeTreeHeight; i++) {
            placeLog(pLevel, pBlockSetter, pRandom, pPos.above(i), pConfig);
        }

        // Точка для листвы на вершине
        foliageAttachments.add(new FoliagePlacer.FoliageAttachment(pPos.above(pFreeTreeHeight - 1), 0, false));

        // Небольшие ветки в верхней части
        for (int i = 0; i < 2; i++) {
            int yOffset = pFreeTreeHeight - 2 - i;
            if (yOffset > 0) {
                Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(pRandom);
                BlockPos branchPos = pPos.above(yOffset).relative(direction);
                placeLog(pLevel, pBlockSetter, pRandom, branchPos, pConfig);
                foliageAttachments.add(new FoliagePlacer.FoliageAttachment(branchPos, 0, false));
            }
        }

        return foliageAttachments;
    }
}