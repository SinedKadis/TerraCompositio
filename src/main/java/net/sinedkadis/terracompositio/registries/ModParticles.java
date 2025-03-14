package net.sinedkadis.terracompositio.registries;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.TerraCompositio;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, TerraCompositio.MOD_ID);

    public static final RegistryObject<SimpleParticleType> FLOW_PARTICLE =
            PARTICLE_TYPES.register("flow_particle",() -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> FLOW_STILL_PARTICLE =
            PARTICLE_TYPES.register("flow_still_particle",() -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> BIRCH_JUICE_PARTICLE =
            PARTICLE_TYPES.register("birch_juice_particle",() -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> FLOW_SPLASH_PARTICLE =
            PARTICLE_TYPES.register("flow_splash_particle",() -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> BIRCH_JUICE_SPLASH_PARTICLE =
            PARTICLE_TYPES.register("birch_juice_splash_particle",() -> new SimpleParticleType(true));

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
