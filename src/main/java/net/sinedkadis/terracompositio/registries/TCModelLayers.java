package net.sinedkadis.terracompositio.registries;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.sinedkadis.terracompositio.TerraCompositio;

public class TCModelLayers {
    public static final ModelLayerLocation PINE_BOAT_LAYER = new ModelLayerLocation(
            TerraCompositio.modLoc("boat/flow_cedar"), "main");
    public static final ModelLayerLocation PINE_CHEST_BOAT_LAYER = new ModelLayerLocation(
            TerraCompositio.modLoc("chest_boat/flow_cedar"), "main");

    public static final ModelLayerLocation FLOW_CEDAR_ENT_LAYER = new ModelLayerLocation(
            TerraCompositio.modLoc( "flow_cedar_ent_entity"), "main");
    public static final ModelLayerLocation CFE_CUBE_LAYER = new ModelLayerLocation(
            TerraCompositio.modLoc("cfe_cube"), "main");

    public static final ModelLayerLocation TECHNETIUM_CROWN_LAYER = new ModelLayerLocation(
            TerraCompositio.modLoc( "technetium_crown"), "main");
}