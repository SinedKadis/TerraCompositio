package net.sinedkadis.terracompositio.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@Getter
@ParametersAreNonnullByDefault
public class TechnetiumFiringRecipe implements Recipe<Container> {
    private final ItemStack furnaceOutputItem;
    private final ResourceLocation pRecipeId;
    private final int ecf;
    private boolean assembled;

    public TechnetiumFiringRecipe(
            ItemStack furnaceOutputItem,
            int ecf,
            ResourceLocation pRecipeId) {
        this.furnaceOutputItem = furnaceOutputItem;
        this.pRecipeId = pRecipeId;
        assembled = false;
        this.ecf = ecf;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return furnaceOutputItem.is(container.getItem(0).getItem());
    }

    @Override
    public @NotNull ItemStack assemble(Container container, RegistryAccess registryAccess) {
        assembled = true;
        return container.getItem(0);
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY,Ingredient.of(furnaceOutputItem));
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(RegistryAccess registryAccess) {
        return furnaceOutputItem;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return this.pRecipeId;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<TechnetiumFiringRecipe>{
        public static final TechnetiumFiringRecipe.Type INSTANCE = new TechnetiumFiringRecipe.Type();
        public static final String ID = "technetium_firing";

        @Override
        public String toString() {
            return ID;
        }
    }
    public static class Serializer implements RecipeSerializer<TechnetiumFiringRecipe>{
        public static final Serializer INSTANCE = new Serializer();
        //public static final ResourceLocation ID = new ResourceLocation(TerraCompositio.MOD_ID,"flow_infusion");
        @Override
        public @NotNull TechnetiumFiringRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
            String furnaceInput = GsonHelper.getAsString(pJson, "furnace_input");
            Item ingredient = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(furnaceInput));
            int cfe = GsonHelper.getAsInt(pJson, "ecf");
            if (ingredient != null){
                return new TechnetiumFiringRecipe(ingredient.getDefaultInstance(),cfe,pRecipeId);
            } else{
                throw new JsonSyntaxException("Item " + furnaceInput + " not found ");
            }
        }

        @Override
        public @Nullable TechnetiumFiringRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            ItemStack ingredient = pBuffer.readItem();
            int cfe = pBuffer.readInt();
            return new TechnetiumFiringRecipe(ingredient,cfe,pRecipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, TechnetiumFiringRecipe pRecipe) {
            pBuffer.writeItem(pRecipe.furnaceOutputItem);
            pBuffer.writeInt(pRecipe.ecf);
        }
    }
}
