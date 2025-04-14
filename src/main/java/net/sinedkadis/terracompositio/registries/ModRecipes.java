package net.sinedkadis.terracompositio.registries;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.recipe.FlowInfusionRecipe;
import net.sinedkadis.terracompositio.recipe.FlowSaturationRecipe;
import net.sinedkadis.terracompositio.recipe.MatterInfusionRecipe;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, TerraCompositio.MOD_ID);
    public static final RegistryObject<RecipeSerializer<FlowSaturationRecipe>> FLOW_SATURATION_SERIALIZER =
            SERIALIZERS.register("flow_saturation",()->FlowSaturationRecipe.Serializer.INSTANCE);
    public static final RegistryObject<RecipeSerializer<FlowInfusionRecipe>> FLOW_INFUSION_SERIALIZER =
            SERIALIZERS.register("flow_infusion",()->FlowInfusionRecipe.Serializer.INSTANCE);
    public static final RegistryObject<RecipeSerializer<MatterInfusionRecipe>> MATTER_INFUSION_SERIALIZER =
            SERIALIZERS.register("matter_infusion",()-> MatterInfusionRecipe.Serializer.INSTANCE);


    public static void register(IEventBus eventBus){
        SERIALIZERS.register(eventBus);
    }
}
