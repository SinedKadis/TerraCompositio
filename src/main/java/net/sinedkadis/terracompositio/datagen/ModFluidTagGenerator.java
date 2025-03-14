package net.sinedkadis.terracompositio.datagen;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.registries.ModFluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModFluidTagGenerator extends FluidTagsProvider {
    public ModFluidTagGenerator(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pProvider, TerraCompositio.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        this.tag(FluidTags.WATER)
                .add(ModFluids.FLOW_FLUID.source.get())
                .add(ModFluids.BIRCH_JUICE_FLUID.source.get());
    }
}
