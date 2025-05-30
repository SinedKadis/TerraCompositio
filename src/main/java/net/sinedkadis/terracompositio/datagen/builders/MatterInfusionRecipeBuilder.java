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
import net.sinedkadis.terracompositio.recipe.MatterInfusionRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public class MatterInfusionRecipeBuilder implements RecipeBuilder {

    private final ItemStack output;
    private final ItemStack input;
    private final Item catalyst;
    private final int cfe;
    private final int time;
    private final int rate;
    private final Advancement.Builder advancement = Advancement.Builder.advancement();

    private MatterInfusionRecipeBuilder(ItemStack input,ItemStack output, Item catalyst, int cfe, int time, int rate) {
        this.output = output;
        this.input = input;
        this.catalyst = catalyst;
        this.cfe = cfe;
        this.time = time;
        this.rate = rate;
    }

    public static MatterInfusionRecipeBuilder create(ItemStack input, ItemStack output,  Item catalyst, int cfe, int time, int rate) {
        return new MatterInfusionRecipeBuilder(input,output,  catalyst, cfe, time, rate);
    }
    public static MatterInfusionRecipeBuilder create(Item input,int i_count, Item output,int o_count, Item catalyst, int cfe, int time, int rate) {
        return new MatterInfusionRecipeBuilder(new ItemStack(input,i_count),new ItemStack(output,o_count),  catalyst, cfe, time, rate);
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
        consumer.accept(new Result(output,input,catalyst,cfe,time,rate, resourceLocation, advancement));
    }

    public static class Result implements FinishedRecipe {

        private final ItemStack output;
        private final ItemStack input;
        private final Item catalyst;
        private final int cfe;
        private final int time;
        private final int rate;
        private final ResourceLocation id;
        private final Advancement.Builder advancement;

        public Result(ItemStack output, ItemStack input, Item catalyst, int cfe, int time, int rate, ResourceLocation id, Advancement.Builder advancement) {
            this.output = output;
            this.input = input;
            this.catalyst = catalyst;
            this.cfe = cfe;
            this.time = time;
            this.rate = rate;
            this.id = id;
            this.advancement = advancement;
        }

        @Override
        public @NotNull JsonObject serializeRecipe() {
            JsonObject jsonObject = new JsonObject();
            assert MatterInfusionRecipe.Serializer.ID != null;
            jsonObject.addProperty("type", MatterInfusionRecipe.Serializer.ID.toString());
            this.serializeRecipeData(jsonObject);
            return jsonObject;
        }

        @Override
        public void serializeRecipeData(@NotNull JsonObject jsonObject) {
            JsonObject inputObj = new JsonObject();
            inputObj.addProperty("count",input.getCount());
            inputObj.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(input.getItem())).toString());
            jsonObject.add("input",inputObj);

            jsonObject.addProperty("catalyst",Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(catalyst)).toString());

            JsonObject outputObj = new JsonObject();
            outputObj.addProperty("count",output.getCount());
            outputObj.addProperty("item",Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(output.getItem())).toString());
            jsonObject.add("output",outputObj);

            jsonObject.addProperty("cfe",cfe);
            jsonObject.addProperty("time",time);
            jsonObject.addProperty("rate",rate);
        }


        @Override
        public @NotNull ResourceLocation getId() {
            return this.id;
        }

        @Override
        public @NotNull RecipeSerializer<?> getType() {
            return MatterInfusionRecipe.Serializer.INSTANCE;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return this.advancement.serializeToJson();
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return this.id.withPrefix("recipes/matter_infuser/");
        }
    }
}
