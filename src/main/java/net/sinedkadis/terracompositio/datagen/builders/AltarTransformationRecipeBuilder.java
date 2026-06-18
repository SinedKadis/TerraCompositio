package net.sinedkadis.terracompositio.datagen.builders;

import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.recipe.AltarTransformationRecipe;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AltarTransformationRecipeBuilder implements RecipeBuilder {

    private final ItemStack output;
    private final Ingredient input;

    private AltarTransformationRecipeBuilder(Ingredient input, ItemStack output) {
        this.output = output;
        this.input = input;
    }

    public static AltarTransformationRecipeBuilder create(Ingredient input, ItemStack output) {
        return new AltarTransformationRecipeBuilder(input, output);
    }


    @Override
    public RecipeBuilder unlockedBy(String s, CriterionTriggerInstance criterionTriggerInstance) {
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String s) {
        return this;
    }

    @Override
    public Item getResult() {
        return this.output.getItem();
    }

    @Override
    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation pRecipeId) {
        consumer.accept(new Result(output, input, pRecipeId));
    }


    @Override
    public void save(Consumer<FinishedRecipe> consumer, String resourceLocation) {
        this.save(consumer, TerraCompositio.modLoc(AltarTransformationRecipe.Type.ID + "/" + resourceLocation));
    }

    public static class Result implements FinishedRecipe {

        private final ItemStack output;
        private final Ingredient input;

        private final ResourceLocation id;

        public Result(ItemStack output, Ingredient input, ResourceLocation id) {
            this.output = output;
            this.input = input;

            this.id = id;

        }

        @Override
        public JsonObject serializeRecipe() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", AltarTransformationRecipe.Serializer.ID.toString());
            this.serializeRecipeData(jsonObject);
            return jsonObject;
        }

        @Override
        public void serializeRecipeData(JsonObject jsonObject) {
            jsonObject.add("ingredients", input.toJson());

            JsonObject outputObj = new JsonObject();
            outputObj.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(output.getItem())).toString());
            outputObj.addProperty("count", output.getCount());

            if (output.hasTag()) {
                assert output.getTag() != null;
                outputObj.addProperty("nbt", output.getTag().toString());
            }

            jsonObject.add("output", outputObj);
        }


        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return AltarTransformationRecipe.Serializer.INSTANCE;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
