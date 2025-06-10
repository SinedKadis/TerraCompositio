package net.sinedkadis.terracompositio.registries;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;

public class TCFoods {
    public static final FoodProperties FLOW = new FoodProperties.Builder()
            .effect(() -> new MobEffectInstance(TCEffects.FLOW_SATURATION.get(),200),1f).build();
}
