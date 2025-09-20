package net.sinedkadis.terracompositio.worldgen.biome;

import net.minecraft.resources.ResourceLocation;
import net.sinedkadis.terracompositio.TerraCompositio;
import terrablender.api.Regions;

public class TCTerrablender {
    public static void registerBiomes() {
        Regions.register(new TCOverworldRegion(TerraCompositio.modLoc( "overworld"), 5));
    }
}
