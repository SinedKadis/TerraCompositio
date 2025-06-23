package net.sinedkadis.terracompositio.registries;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.sinedkadis.terracompositio.TerraCompositio;

import java.util.Objects;

public class TCModelLayers {
    public static final ModelLayerLocation PINE_BOAT_LAYER = new ModelLayerLocation(
            Objects.requireNonNull(ResourceLocation.tryBuild(TerraCompositio.MOD_ID, "boat/flow_cedar")), "main");
    public static final ModelLayerLocation PINE_CHEST_BOAT_LAYER = new ModelLayerLocation(
            Objects.requireNonNull(ResourceLocation.tryBuild(TerraCompositio.MOD_ID, "chest_boat/flow_cedar")), "main");

    public static final ModelLayerLocation FLOW_CEDAR_ENT_LAYER = new ModelLayerLocation(
            Objects.requireNonNull(ResourceLocation.tryBuild(TerraCompositio.MOD_ID, "flow_cedar_ent_entity")), "main");

}