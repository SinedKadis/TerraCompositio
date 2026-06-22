package net.sinedkadis.terracompositio.recipe;

import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Objects;

//Thanks to Botania mod for that cool class
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TagTransferShapedRecipe extends ShapedRecipe {
    public static final RecipeSerializer<TagTransferShapedRecipe> SERIALIZER = new Serializer();

    public TagTransferShapedRecipe(ShapedRecipe compose) {
        super(compose.getId(), compose.getGroup(), compose.category(), compose.getWidth(), compose.getHeight(),
                compose.getIngredients(),
                // XXX: Hacky, but compose should always be a vanilla shaped recipe which doesn't do anything with the
                // RegistryAccess
                compose.getResultItem(RegistryAccess.EMPTY));
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registries) {
        ItemStack out = super.assemble(inv, registries);
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(getResultItem(RegistryAccess.EMPTY).getItem())) return ItemStack.EMPTY;
            if (stack.hasTag() && stack.getItem() instanceof ArmorItem) {
                out.setTag(stack.getTag());
                break;
            }
        }
        return out;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> original = super.getIngredients();
        ItemStack result = getResultItem(RegistryAccess.EMPTY);

        NonNullList<Ingredient> out = NonNullList.withSize(original.size(), Ingredient.EMPTY);
        for (int i = 0; i < original.size(); i++) {
            out.set(i, ExcludeItemIngredient.of(original.get(i), result));
        }
        return out;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    private static class Serializer implements RecipeSerializer<TagTransferShapedRecipe> {
        @Override
        public TagTransferShapedRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            return new TagTransferShapedRecipe(SHAPED_RECIPE.fromJson(recipeId, json));
        }

        @Override
        public TagTransferShapedRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            return new TagTransferShapedRecipe(Objects.requireNonNull(SHAPED_RECIPE.fromNetwork(recipeId, buffer)));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, TagTransferShapedRecipe recipe) {
            SHAPED_RECIPE.toNetwork(buffer, recipe);
        }
    }

    public static class ExcludeItemIngredient extends Ingredient {

        private ExcludeItemIngredient(Ingredient wrapped, Item excluded) {
            super(Arrays.stream(wrapped.getItems())
                    .filter(s -> !s.is(excluded))
                    .map(Ingredient.ItemValue::new)
            );
        }

        public static Ingredient of(Ingredient original, ItemStack exclude) {
            if (!original.test(exclude)) return original;
            return new ExcludeItemIngredient(original, exclude.getItem());
        }
    }
}
