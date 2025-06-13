package net.sinedkadis.terracompositio.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.registries.ForgeRegistries;
import net.sinedkadis.terracompositio.TerraCompositio;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FlowSaturationRecipe implements Recipe<SimpleContainer> {
    private final ItemStack input;
    private final ItemStack output;
    private final ResourceLocation id;

    public FlowSaturationRecipe(ItemStack input, ItemStack output, ResourceLocation id) {
        this.input = input;
        this.output = output;
        this.id = id;
    }

    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        if(pLevel.isClientSide()){
            return false;
        }

        return StrictNBTIngredient.of(input).test(pContainer.getItem(0));
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY,Ingredient.of(input));
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
    public static class Type implements RecipeType<FlowSaturationRecipe>{
        public static final Type INSTANCE = new Type();
        public static final String ID = "flow_saturation";
    }
    public static class Serializer implements RecipeSerializer<FlowSaturationRecipe>{
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = ResourceLocation.tryBuild(TerraCompositio.MOD_ID,"flow_saturation");
        @Override
        public FlowSaturationRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {

            JsonObject i_outputObject = GsonHelper.getAsJsonObject(pSerializedRecipe, "input");
            ResourceLocation i_itemId = ResourceLocation.tryParse(GsonHelper.getAsString(i_outputObject, "item"));
            int i_count = GsonHelper.getAsInt(i_outputObject, "count", 1);
            ItemStack i_output = new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(i_itemId)), i_count);

            if (i_outputObject.has("nbt")) {
                try {
                    CompoundTag nbt = TagParser.parseTag(GsonHelper.getAsString(i_outputObject, "nbt"));
                    i_output.setTag(nbt);
                } catch (CommandSyntaxException e) {
                    throw new JsonSyntaxException("Invalid NBT tag in recipe input", e);
                }
            }

            JsonObject outputObject = GsonHelper.getAsJsonObject(pSerializedRecipe, "output");
            ResourceLocation itemId = ResourceLocation.tryParse(GsonHelper.getAsString(outputObject, "item"));
            int count = GsonHelper.getAsInt(outputObject, "count", 1);
            ItemStack output = new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(itemId)), count);

            if (outputObject.has("nbt")) {
                try {
                    CompoundTag nbt = TagParser.parseTag(GsonHelper.getAsString(outputObject, "nbt"));
                    output.setTag(nbt);
                } catch (CommandSyntaxException e) {
                    throw new JsonSyntaxException("Invalid NBT tag in recipe output", e);
                }
            }

            return new FlowSaturationRecipe(i_output, output, pRecipeId);
        }


        @Override
        public @Nullable FlowSaturationRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            ItemStack input = pBuffer.readItem();
            ItemStack output = pBuffer.readItem();
            return new FlowSaturationRecipe(input,output,pRecipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, FlowSaturationRecipe pRecipe) {
            pBuffer.writeItemStack(pRecipe.input,false);
            pBuffer.writeItemStack(pRecipe.getResultItem(null),false);
        }
    }
}
