package net.sinedkadis.terracompositio.recipe;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.sinedkadis.terracompositio.TerraCompositio;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MatterInfusionRecipe implements Recipe<SimpleContainer> {

    private final Item catalyst;
    private final ItemStack input;
    private final ItemStack output;
    private final ResourceLocation id;
    @Getter
    private final int catalystDecayRate;
    @Getter
    private final int cfe;
    @Getter
    private final int ticks;

    public MatterInfusionRecipe(Item catalyst, ItemStack input, ItemStack output, int catalystDecayRate, ResourceLocation pRecipeId, int cfe, int ticks) {
        this.catalyst = catalyst;
        this.input = input;
        this.output = output;
        this.catalystDecayRate = catalystDecayRate;
        this.cfe = cfe;
        this.ticks = ticks;
        this.id = pRecipeId;
    }

    @Override
    public boolean matches(@NotNull SimpleContainer pContainer, Level pLevel) {
        if(pLevel.isClientSide()){
            return false;
        }

        ItemStack catalystSlot = pContainer.getItem(0);
        ItemStack inputSlot = pContainer.getItem(1);

        return catalystSlot.is(catalyst)
                && inputSlot.is(input.getItem())
                && inputSlot.getCount() >= input.getCount();
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.of(),Ingredient.of(catalyst),Ingredient.of(input));
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull SimpleContainer pContainer, @NotNull RegistryAccess pRegistryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(@Nullable RegistryAccess pRegistryAccess) {
        return output.copy();
    }
    public float getCFETick(){
        return (float) cfe /ticks;
    }
    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return Type.INSTANCE;
    }
    public static class Type implements RecipeType<MatterInfusionRecipe>{
        public static final Type INSTANCE = new Type();
        //public static final String ID = "matter_infusion";
    }
    public static class Serializer implements RecipeSerializer<MatterInfusionRecipe>{
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(TerraCompositio.MOD_ID,"matter_infusion");
        @Override
        public @NotNull MatterInfusionRecipe fromJson(@NotNull ResourceLocation pRecipeId, @NotNull JsonObject pSerializedRecipe) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "output"));

            String catalystStr = GsonHelper.getAsString(pSerializedRecipe, "catalyst");
            Item catalyst = ForgeRegistries.ITEMS.getDelegateOrThrow(ResourceLocation.tryParse(catalystStr)).get();
            ItemStack input = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "input"));
            int cfe = GsonHelper.getAsInt(pSerializedRecipe,"cfe");
            int ticks = GsonHelper.getAsInt(pSerializedRecipe,"time");
            int catalystDecayRate = GsonHelper.getAsInt(pSerializedRecipe,"rate");

            return new MatterInfusionRecipe(catalyst,input,output,catalystDecayRate,pRecipeId,cfe,ticks);
        }

        @Override
        public @Nullable MatterInfusionRecipe fromNetwork(@NotNull ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            ItemStack output = pBuffer.readItem();
            ItemStack catalyst = pBuffer.readItem();
            ItemStack input = pBuffer.readItem();
            int cfe = pBuffer.readInt();
            int ticks = pBuffer.readInt();
            int catalystDecayRate = pBuffer.readInt();

            return new MatterInfusionRecipe(catalyst.getItem(),input,output,catalystDecayRate,pRecipeId,cfe,ticks);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, MatterInfusionRecipe pRecipe) {
            pBuffer.writeItemStack(pRecipe.output,false);
            pBuffer.writeItemStack(new ItemStack(pRecipe.catalyst),true);
            pBuffer.writeItemStack(pRecipe.input,false);
            pBuffer.writeInt(pRecipe.cfe);
            pBuffer.writeInt(pRecipe.ticks);
            pBuffer.writeInt(pRecipe.catalystDecayRate);
        }
    }
}
