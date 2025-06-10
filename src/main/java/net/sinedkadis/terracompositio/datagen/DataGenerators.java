package net.sinedkadis.terracompositio.datagen;


import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.sinedkadis.terracompositio.TerraCompositio;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = TerraCompositio.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new TCRecipeProvider(packOutput));
        generator.addProvider(event.includeServer(), TCLootTableProvider.create(packOutput));

        generator.addProvider(event.includeClient(), new TCBlockStateProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new TCItemModelProvider(packOutput, existingFileHelper));

        TCBlockTagGenerator blockTagGenerator = generator.addProvider(event.includeServer(),
                new TCBlockTagGenerator(packOutput, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new TCItemTagGenerator(packOutput, lookupProvider, blockTagGenerator.contentsGetter(), existingFileHelper));
        generator.addProvider(event.includeServer(),new TCFluidTagGenerator(packOutput,lookupProvider,existingFileHelper));
        generator.addProvider(event.includeServer(), new TCWorldGenProvider(packOutput, lookupProvider));
    }
}
