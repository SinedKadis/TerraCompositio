package net.sinedkadis.terracompositio.recipe;

import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import javax.annotation.ParametersAreNonnullByDefault;
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
            if (stack.hasTag() && stack.getItem() instanceof ArmorItem) {
                out.setTag(stack.getTag());
                break;
            }
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
}
