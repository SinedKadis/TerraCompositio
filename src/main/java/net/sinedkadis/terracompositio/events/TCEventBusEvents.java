package net.sinedkadis.terracompositio.events;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.entity.custom.FlowCedarEntEntity;
import net.sinedkadis.terracompositio.registries.TCEntities;
import net.sinedkadis.terracompositio.registries.TCParticles;
import net.sinedkadis.terracompositio.particle.custom.*;

@Mod.EventBusSubscriber(modid = TerraCompositio.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TCEventBusEvents {
    @SubscribeEvent
    public static void registerParticleFactories(final RegisterParticleProvidersEvent event) {
        Minecraft.getInstance().particleEngine.register(TCParticles.FLOW_PARTICLE.get(),
                FlowParticle.Provider::new);
        Minecraft.getInstance().particleEngine.register(TCParticles.CFE_PARTICLE.get(),
                CFEParticle.Provider::new);
        Minecraft.getInstance().particleEngine.register(TCParticles.BIRCH_JUICE_PARTICLE.get(),
                BirchJuiceParticle.Provider::new);
        Minecraft.getInstance().particleEngine.register(TCParticles.FLOW_SPLASH_PARTICLE.get(),
                FlowSplashParticle.Provider::new);
        Minecraft.getInstance().particleEngine.register(TCParticles.BIRCH_JUICE_SPLASH_PARTICLE.get(),
                BirchJuiceSplashParticle.Provider::new);
        Minecraft.getInstance().particleEngine.register(TCParticles.FLUID_FLOW.get(),
                FluidFlowParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event){
        event.put(TCEntities.FLOW_CEDAR_ENT.get(), FlowCedarEntEntity.createAttributes().build());
    }
}
