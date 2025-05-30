package net.sinedkadis.terracompositio.entity.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.sinedkadis.terracompositio.TerraCompositio;

import java.util.Objects;

public class ModModelLayers {
    public static final ModelLayerLocation PINE_BOAT_LAYER = new ModelLayerLocation(
            Objects.requireNonNull(ResourceLocation.tryBuild(TerraCompositio.MOD_ID, "boat/flow_cedar")), "main");
    public static final ModelLayerLocation PINE_CHEST_BOAT_LAYER = new ModelLayerLocation(
            Objects.requireNonNull(ResourceLocation.tryBuild(TerraCompositio.MOD_ID, "chest_boat/flow_cedar")), "main");

}