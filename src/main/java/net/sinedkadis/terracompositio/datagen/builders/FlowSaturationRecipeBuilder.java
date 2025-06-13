package net.sinedkadis.terracompositio.datagen.builders;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import net.sinedkadis.terracompositio.recipe.FlowSaturationRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public class FlowSaturationRecipeBuilder implements RecipeBuilder {

    private final ItemStack output;
    private final ItemStack input;

    private final Advancement.Builder advancement = Advancement.Builder.advancement();

    private FlowSaturationRecipeBuilder(ItemStack input, ItemStack output) {
        this.output = output;
        this.input = input;
    }

    public static FlowSaturationRecipeBuilder create(ItemStack input, ItemStack output) {
        return new FlowSaturationRecipeBuilder(input,output);
    }


    @Override
    public @NotNull RecipeBuilder unlockedBy(@NotNull String s, @NotNull CriterionTriggerInstance criterionTriggerInstance) {
        this.advancement.addCriterion(s, criterionTriggerInstance);
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

    protected boolean hasCriteria() {
        return !this.advancement.getCriteria().isEmpty();
    }

    @Override
    public void save(@NotNull Consumer<FinishedRecipe> consumer, @NotNull ResourceLocation resourceLocation) {
        if (hasCriteria())
            this.advancement.parent(Objects.requireNonNull(ResourceLocation.tryParse("recipes/root"))).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceLocation)).rewards(net.minecraft.advancements.AdvancementRewards.Builder.recipe(resourceLocation)).requirements(RequirementsStrategy.OR);
        consumer.accept(new Result(output,input, resourceLocation, advancement));
    }

    public static class Result implements FinishedRecipe {

        private final ItemStack output;
        private final ItemStack input;

        private final ResourceLocation id;
        private final Advancement.Builder advancement;

        public Result(ItemStack output, ItemStack input, ResourceLocation id, Advancement.Builder advancement) {
            this.output = output;
            this.input = input;

            this.id = id;
            this.advancement = advancement;
        }

        @Override
        public @NotNull JsonObject serializeRecipe() {
            JsonObject jsonObject = new JsonObject();
            assert FlowSaturationRecipe.Serializer.ID != null;
            jsonObject.addProperty("type", FlowSaturationRecipe.Serializer.ID.toString());
            this.serializeRecipeData(jsonObject);
            return jsonObject;
        }

        @Override
        public void serializeRecipeData(@NotNull JsonObject jsonObject) {
            JsonObject inputObj = new JsonObject();
            inputObj.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(input.getItem())).toString());
            inputObj.addProperty("count", input.getCount());

            if (input.hasTag()) {
                assert input.getTag() != null;
                inputObj.addProperty("nbt", input.getTag().toString());
            }

            jsonObject.add("input", inputObj);

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
        public @NotNull ResourceLocation getId() {
            return this.id;
        }

        @Override
        public @NotNull RecipeSerializer<?> getType() {
            return FlowSaturationRecipe.Serializer.INSTANCE;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return this.advancement.serializeToJson();
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return this.id.withPrefix("recipes/flow_saturation/");
        }
    }
}
