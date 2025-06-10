package net.sinedkadis.terracompositio.worldgen.tree.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.sinedkadis.terracompositio.worldgen.tree.TCFoliagePlacers;
import org.jetbrains.annotations.NotNull;

public class FlowCedarFoliagePlacer extends FoliagePlacer {
    public static final Codec<FlowCedarFoliagePlacer> CODEC = RecordCodecBuilder.create(
            instance -> foliagePlacerParts(instance)
                    .and(Codec.intRange(0, 16).fieldOf("height").forGetter(fp -> fp.height))
                    .apply(instance, FlowCedarFoliagePlacer::new)
    );

    private final int height;

    public FlowCedarFoliagePlacer(IntProvider radius, IntProvider offset, int height) {
        super(radius, offset);
        this.height = height;
    }

    @Override
    protected @NotNull FoliagePlacerType<?> type() {
        return TCFoliagePlacers.FLOW_CEDAR_FOLIAGE_PLACER.get();
    }

    @Override
    protected void createFoliage(@NotNull LevelSimulatedReader pLevel,
                                 @NotNull FoliageSetter pBlockSetter,
                                 @NotNull RandomSource pRandom,
                                 @NotNull TreeConfiguration pConfig,
                                 int pMaxFreeTreeHeight,
                                 @NotNull FoliageAttachment pAttachment,
                                 int pFoliageHeight,
                                 int pFoliageRadius,
                                 int pOffset) {
        BlockPos pos = pAttachment.pos();

        // Основная листва - пирамидальная форма
        for (int y = 0; y <= pFoliageHeight; y++) {
            int radius = getRadiusAtHeight(pFoliageHeight, y);
            this.placeLeavesRow(pLevel, pBlockSetter, pRandom, pConfig,
                    pos.above(y), radius, 0, pAttachment.doubleTrunk());
        }

        // Добавляем случайные "ветки"
        for (int i = 0; i < 3; i++) {
            int yOffset = pRandom.nextInt(pFoliageHeight);
            Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(pRandom);
            BlockPos branchPos = pos.above(yOffset).relative(direction, pRandom.nextInt(2) + 1);
            this.placeLeavesRow(pLevel, pBlockSetter, pRandom, pConfig, branchPos, 1, 0, pAttachment.doubleTrunk());
        }
    }

    private int getRadiusAtHeight(int totalHeight, int currentY) {
        return (totalHeight - currentY) / 2 + 1;
    }

    @Override
    public int foliageHeight(@NotNull RandomSource pRandom, int pHeight, @NotNull TreeConfiguration pConfig) {
        // Используем заданную высоту или вычисляем, если не задана
        return height > 0 ? height : Math.max(3, pHeight * 2 / 3 + pRandom.nextInt(2));
    }

    @Override
    protected boolean shouldSkipLocation(@NotNull RandomSource pRandom, int pLocalX, int pLocalY, int pLocalZ, int pRange, boolean pLarge) {
        if (pLocalY == 0 && (pLocalX > 1 || pLocalZ > 1)) {
            return pRandom.nextFloat() > 0.25f;
        }
        return pLocalX + pLocalZ + pLocalY > pRange + 1;
    }
}