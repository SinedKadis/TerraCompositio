package net.sinedkadis.terracompositio.worldgen.tree;


import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.sinedkadis.terracompositio.worldgen.ModConfiguredFeatures;
import org.jetbrains.annotations.Nullable;

public class BigFlowCedarTreeGrower extends AbstractTreeGrower {
    @Nullable
    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource pRandom, boolean pHasFlowers) {
        return ModConfiguredFeatures.BIG_FLOW_CEDAR_KEY;
    }
}