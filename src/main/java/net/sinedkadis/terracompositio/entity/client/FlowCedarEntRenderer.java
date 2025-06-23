package net.sinedkadis.terracompositio.entity.client;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.entity.custom.FlowCedarEntEntity;
import net.sinedkadis.terracompositio.registries.TCModelLayers;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FlowCedarEntRenderer extends MobRenderer<FlowCedarEntEntity,FlowCedarEntModel<FlowCedarEntEntity>> {
    public FlowCedarEntRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new FlowCedarEntModel<>(pContext.bakeLayer(TCModelLayers.FLOW_CEDAR_ENT_LAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(FlowCedarEntEntity pEntity) {
        return TerraCompositio.modLoc("textures/entity/flow_cedar_ent.png");
    }
}
