package net.sinedkadis.terracompositio.datagen.builders;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import net.sinedkadis.terracompositio.recipe.FlowInfusionRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public class FlowInfusionRecipeBuilder implements RecipeBuilder {

    private final ItemStack output;
    private final Ingredient input;
    private final int cfe;
    private final int time;

    private FlowInfusionRecipeBuilder(ItemStack output, Ingredient input, int cfe, int time) {
        this.output = output;
        this.input = input;
        this.cfe = cfe;
        this.time = time;
    }

    public static FlowInfusionRecipeBuilder create(ItemStack output, Ingredient input, int cfe, int time) {
        return new FlowInfusionRecipeBuilder(output, input, cfe, time);
    }


    @Override
    public @NotNull RecipeBuilder unlockedBy(@NotNull String s, @NotNull CriterionTriggerInstance criterionTriggerInstance) {
        return this;
    }

    @Override
    public @NotNull RecipeBuilder group(@Nullable String s) {
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return this.output.getItem();
    }



    @Override
    public void save(@NotNull Consumer<FinishedRecipe> consumer, @NotNull ResourceLocation resourceLocation) {
        consumer.accept(new Result(output,input,cfe,time, resourceLocation));
    }

    public static class Result implements FinishedRecipe {

        private final ItemStack output;
        private final Ingredient input;

        private final int cfe;
        private final int time;
        private final ResourceLocation id;


        public Result(ItemStack output, Ingredient input, int cfe, int time, ResourceLocation id) {
            this.output = output;
            this.input = input;
            this.cfe = cfe;
            this.time = time;

            this.id = id;
        }

        @Override
        public @NotNull JsonObject serializeRecipe() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", FlowInfusionRecipe.Serializer.ID.toString());
            this.serializeRecipeData(jsonObject);
            return jsonObject;
        }

        @Override
        public void serializeRecipeData(@NotNull JsonObject jsonObject) {


            JsonElement json = input.toJson();
            if (json.isJsonObject()) {
                var inputArr = new JsonArray();
                inputArr.add(json);
                jsonObject.add("ingredients", inputArr);
            } else {
                jsonObject.add("ingredients", json);
            }

            JsonObject outputObj = new JsonObject();
            outputObj.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(output.getItem())).toString());
            outputObj.addProperty("count", output.getCount());

            if (output.hasTag()) {
                assert output.getTag() != null;
                outputObj.addProperty("nbt", output.getTag().toString());
            }

            jsonObject.add("output", outputObj);
            jsonObject.addProperty("cfe",cfe);
            jsonObject.addProperty("time",time);
        }


        @Override
        public @NotNull ResourceLocation getId() {
            return this.id;
        }

        @Override
        public @NotNull RecipeSerializer<?> getType() {
            return FlowInfusionRecipe.Serializer.INSTANCE;
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
