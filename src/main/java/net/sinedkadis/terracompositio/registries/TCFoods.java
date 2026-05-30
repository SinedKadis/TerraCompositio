package net.sinedkadis.terracompositio.registries;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;

public class TCFoods {
    public static final FoodProperties FLOW = new FoodProperties.Builder()
            .effect(() -> new MobEffectInstance(TCEffects.FLOW_SATURATION.get(),200),1f).build();
    public static final FoodProperties CREATION_KNOWLEDGE = new FoodProperties.Builder()
            .effect(() -> new MobEffectInstance(TCEffects.CREATION_KNOWLEDGE.get(),1),1f).alwaysEat().build();
    public static final FoodProperties IGNORANCE = new FoodProperties.Builder()
            .effect(() -> new MobEffectInstance(TCEffects.IGNORANCE.get(), 1), 1f).alwaysEat().build();
}
