package net.sinedkadis.terracompositio.registries;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.sinedkadis.terracompositio.effect.ModEffects;

public class ModFoods {
    public static final FoodProperties FLOW = new FoodProperties.Builder()
            .effect(() -> new MobEffectInstance(ModEffects.FLOW_SATURATION.get(),200),1f).build();
}
