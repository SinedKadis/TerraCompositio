package net.sinedkadis.terracompositio.datagen.builders;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import net.sinedkadis.terracompositio.recipe.TechnetiumFiringRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public class TechnetiumFiringRecipeBuilder implements RecipeBuilder {

    private final Item ingredient;
    private final int cfe;
    private TechnetiumFiringRecipeBuilder(Item ingredient,
                                          int cfe) {
        this.ingredient = ingredient;
        this.cfe = cfe;

    }

    public static TechnetiumFiringRecipeBuilder create(Item result,
                                                       int cfe) {
        return new TechnetiumFiringRecipeBuilder(result, cfe);
    }

    @Override
    public @NotNull RecipeBuilder unlockedBy(@NotNull String s, @NotNull CriterionTriggerInstance criterionTriggerInstance) {
        return this;
    }

    @Override
    public @NotNull RecipeBuilder group(@Nullable String pGroupName) {
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return ingredient;
    }


    @Override
    public void save(@NotNull Consumer<FinishedRecipe> consumer, @NotNull ResourceLocation pRecipeId) {
        consumer.accept(new Result(pRecipeId,
                this.ingredient,
                cfe
        ));
    }

    static class Result implements FinishedRecipe {
        @Getter
        private final ResourceLocation id;
        private final Item ingredient;
        private final int cfe;

        public Result(ResourceLocation pId,
                      Item pIngredient,
                      int cfe) {
            this.id = pId;
            this.ingredient = pIngredient;
            this.cfe = cfe;
        }

        public void serializeRecipeData(@NotNull JsonObject pJson) {
            pJson.addProperty("furnace_input", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(ingredient)).toString());
            pJson.addProperty("cfe",cfe);
        }

        public @NotNull RecipeSerializer<?> getType() {
            return TechnetiumFiringRecipe.Serializer.INSTANCE;
        }

        @Nullable
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
