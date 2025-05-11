package net.sinedkadis.terracompositio.events;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.registries.ModParticles;
import net.sinedkadis.terracompositio.particle.custom.*;

@Mod.EventBusSubscriber(modid = TerraCompositio.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {
    @SubscribeEvent
    public static void registerParticleFactories(final RegisterParticleProvidersEvent event) {
        Minecraft.getInstance().particleEngine.register(ModParticles.FLOW_PARTICLE.get(),
                FlowParticle.Provider::new);
        Minecraft.getInstance().particleEngine.register(ModParticles.FLOW_STILL_PARTICLE.get(),
                FlowStillParticle.Provider::new);
        Minecraft.getInstance().particleEngine.register(ModParticles.BIRCH_JUICE_PARTICLE.get(),
                BirchJuiceParticle.Provider::new);
        Minecraft.getInstance().particleEngine.register(ModParticles.FLOW_SPLASH_PARTICLE.get(),
                FlowSplashParticle.Provider::new);
        Minecraft.getInstance().particleEngine.register(ModParticles.BIRCH_JUICE_SPLASH_PARTICLE.get(),
                BirchJuiceSplashParticle.Provider::new);

        Minecraft.getInstance().particleEngine.register(ModParticles.FLUID_FLOW.get(),
                FluidFlowParticle.Provider::new);
    }


}
