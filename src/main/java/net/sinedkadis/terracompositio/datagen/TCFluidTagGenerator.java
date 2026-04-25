package net.sinedkadis.terracompositio.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.ModList;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.registries.TCFluids;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
public class TCFluidTagGenerator extends FluidTagsProvider {
    public TCFluidTagGenerator(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pProvider, TerraCompositio.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        this.tag(FluidTags.WATER)
                .add(TCFluids.FLOW_FLUID.source.get())
                .add(TCFluids.BIRCH_JUICE_FLUID.source.get());

        if (ModList.get().isLoaded("create")) {
            TerraCompositio.createCompat.getDataGen().addFluidTags(this, pProvider);
        }
    }
}
