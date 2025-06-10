package net.sinedkadis.terracompositio.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.registries.ForgeRegistries;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.worldgen.TCBiomeModifiers;
import net.sinedkadis.terracompositio.worldgen.TCConfiguredFeatures;
import net.sinedkadis.terracompositio.worldgen.TCPlacedFeatures;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class TCWorldGenProvider extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            //.add(Registries.DIMENSION_TYPE, ModDimensions::bootstrapType)
            .add(Registries.CONFIGURED_FEATURE, TCConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, TCPlacedFeatures::bootstrap)
            .add(ForgeRegistries.Keys.BIOME_MODIFIERS, TCBiomeModifiers::bootstrap);
            //.add(Registries.BIOME, ModBiomes::boostrap)
            //.add(Registries.LEVEL_STEM, ModDimensions::bootstrapStem);

    public TCWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(TerraCompositio.MOD_ID));
    }
}