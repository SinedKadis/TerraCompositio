package net.sinedkadis.terracompositio.registries;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.effect.custom.TCEffectBase;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TCEffects {
    public static final DeferredRegister<MobEffect> MOD_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, TerraCompositio.MOD_ID);

    public static final RegistryObject<MobEffect> FLOW_SATURATION =
            MOD_EFFECTS.register("flow_saturation",() -> new TCEffectBase(MobEffectCategory.BENEFICIAL,0x1e8dc6));
    public static final RegistryObject<MobEffect> CREATION_KNOWLEDGE =
            MOD_EFFECTS.register("flow_saturation",() -> new TCEffectBase(MobEffectCategory.BENEFICIAL,0x1e8dc6){
                @Override
                public boolean isInstantenous() {
                    return true;
                }

                @Override
                public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
                    if (pLivingEntity instanceof Player) {

                    }
                }
            });
    public static void register(IEventBus eventBus){
        MOD_EFFECTS.register(eventBus);
    }
}
