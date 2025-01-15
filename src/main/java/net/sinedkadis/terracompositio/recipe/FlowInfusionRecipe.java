package net.sinedkadis.terracompositio.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.sinedkadis.terracompositio.TerraCompositio;
import org.jetbrains.annotations.Nullable;

public class FlowInfusionRecipe implements Recipe<SimpleContainer> {
    private final NonNullList<Ingredient> inputItems;
    private final ItemStack output;
    private final ResourceLocation id;
    @Getter
    private final int cfe;
    @Getter
    private final int ticks;

    public FlowInfusionRecipe(NonNullList<Ingredient> inputItems, ItemStack output, ResourceLocation id, int cfe, int ticks) {
        this.inputItems = inputItems;
        this.output = output;
        this.cfe = cfe;
        this.ticks = ticks;
        this.id = id;
    }

    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        if(pLevel.isClientSide()){
            return false;
        }

        return inputItems.get(0).test(pContainer.getItem(0));
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return inputItems;
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return output.copy();
    }
    public float getCFETick(){
        return (float) cfe /ticks;
    }
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }
    public static class Type implements RecipeType<FlowInfusionRecipe>{
        public static final Type INSTANCE = new Type();
        public static final String ID = "flow_infusion";
    }
    public static class Serializer implements RecipeSerializer<FlowInfusionRecipe>{
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(TerraCompositio.MOD_ID,"flow_infusion");
        @Override
        public FlowInfusionRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "output"));
            int cfe = GsonHelper.getAsInt(pSerializedRecipe,"cfe");
            int ticks = GsonHelper.getAsInt(pSerializedRecipe,"time");
            JsonArray ingredients = GsonHelper.getAsJsonArray(pSerializedRecipe,"ingredients");
            NonNullList<Ingredient> inputs = NonNullList.withSize(1,Ingredient.EMPTY);
            for (int i=0; i < inputs.size();i++){
                inputs.set(i,Ingredient.fromJson(ingredients.get(i)));
            }



            return new FlowInfusionRecipe(inputs,output,pRecipeId,cfe,ticks);
        }

        @Override
        public @Nullable FlowInfusionRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(pBuffer.readInt(),Ingredient.EMPTY);
            for (int i = 0; i<inputs.size();i++){
                inputs.set(i,Ingredient.fromNetwork(pBuffer));
            }
            ItemStack output = pBuffer.readItem();
            int cfe = pBuffer.readInt();
            int ticks = pBuffer.readInt();
            return new FlowInfusionRecipe(inputs,output,pRecipeId,cfe,ticks);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, FlowInfusionRecipe pRecipe) {
            pBuffer.writeInt(pRecipe.inputItems.size());
            for (Ingredient ingredient:pRecipe.getIngredients()){
                ingredient.toNetwork(pBuffer);
            }
            pBuffer.writeItemStack(pRecipe.getResultItem(null),false);
            pBuffer.writeInt(pRecipe.cfe);
            pBuffer.writeInt(pRecipe.ticks);
        }
    }
}
