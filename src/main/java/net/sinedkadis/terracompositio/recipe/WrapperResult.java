package net.sinedkadis.terracompositio.recipe;

import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.function.Consumer;


//Thanks to Botania mod for that cool class
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WrapperResult implements FinishedRecipe {
    private final FinishedRecipe delegate;
    @Nullable
    private final RecipeSerializer<?> type;
    @Nullable
    private final Consumer<JsonObject> transform;

    private WrapperResult(FinishedRecipe delegate, @Nullable RecipeSerializer<?> type, @Nullable Consumer<JsonObject> transform) {
        this.delegate = delegate;
        this.type = type;
        this.transform = transform;
    }

    /**
     * Wraps recipe consumer with one that swaps the recipe type to a different one.
     */
    public static Consumer<FinishedRecipe> ofType(RecipeSerializer<?> type, Consumer<FinishedRecipe> parent) {
        return recipe -> parent.accept(new WrapperResult(recipe, type, null));
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
        delegate.serializeRecipeData(json);
        if (transform != null) {
            transform.accept(json);
        }
    }

    @Override
    public JsonObject serializeRecipe() {
        if (type == null) {
            return FinishedRecipe.super.serializeRecipe();
        }
        JsonObject jsonobject = new JsonObject();
        jsonobject.addProperty("type", Objects.requireNonNull(ForgeRegistries.RECIPE_SERIALIZERS.getKey(this.type)).toString());
        this.serializeRecipeData(jsonobject);
        return jsonobject;
    }

    @Override
    public ResourceLocation getId() {
        return delegate.getId();
    }

    @Override
    public RecipeSerializer<?> getType() {
        return type != null ? type : delegate.getType();
    }

    @Nullable
    @Override
    public JsonObject serializeAdvancement() {
        return delegate.serializeAdvancement();
    }

    @Nullable
    @Override
    public ResourceLocation getAdvancementId() {
        return delegate.getAdvancementId();
    }
}
