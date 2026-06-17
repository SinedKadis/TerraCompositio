package net.sinedkadis.terracompositio.registries;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.recipe.*;

public class TCRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, TerraCompositio.MOD_ID);

    static {
        SERIALIZERS.register("flow_saturation", () -> FlowSaturationRecipe.Serializer.INSTANCE);
        SERIALIZERS.register("flow_infusion", () -> FlowInfusionRecipe.Serializer.INSTANCE);
        SERIALIZERS.register("matter_infusion", () -> MatterInfusionRecipe.Serializer.INSTANCE);
        SERIALIZERS.register("technetium_firing", () -> TechnetiumFiringRecipe.Serializer.INSTANCE);
        SERIALIZERS.register("tag_transfer_shaped", () -> TagTransferShapedRecipe.SERIALIZER);
        SERIALIZERS.register("armor_storage_upgrade", () -> ArmorStorageUpgradeRecipe.SERIALIZER);

    }


    public static void register(IEventBus eventBus){
        SERIALIZERS.register(eventBus);
    }
}
