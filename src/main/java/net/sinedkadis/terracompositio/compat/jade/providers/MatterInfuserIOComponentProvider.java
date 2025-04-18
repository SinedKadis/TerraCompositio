package net.sinedkadis.terracompositio.compat.jade.providers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.entity.MatterInfuserIOBlockEntity;
import net.sinedkadis.terracompositio.recipe.MatterInfusionRecipe;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

import java.util.Optional;

public enum MatterInfuserIOComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;


    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getServerData().contains("cfe")) {
            iTooltip.add(Component.translatable("block.terracompositio.matter_infuser." + "cfe", blockAccessor.getServerData().getInt("cfe")));
        }
        if (blockAccessor.getServerData().contains("input_c")) {
            int inputCount = blockAccessor.getServerData().getInt("input_c");
            if (inputCount > 0) {
                //iTooltip.add(Component.empty());
                iTooltip.add(Component.translatable("block.terracompositio.matter_infuser." + "input").append(" "));
                if (blockAccessor.getServerData().contains("input")) {
                    Item input = Item.byId(blockAccessor.getServerData().getInt("input"));
                    IElementHelper elements = IElementHelper.get();
                    IElement icon = elements.item(new ItemStack(input))
                            .translate(new Vec2(-3,-5))
                            ;
                    iTooltip.append(icon);
                    iTooltip.append(Component.literal(String.valueOf(inputCount)).append(" "));
                    iTooltip.append(Component.translatable(input.getDescriptionId()));
                }
                //iTooltip.add(Component.empty());
            }

        }
        if (blockAccessor.getServerData().contains("output_c")) {
            int outputCount = blockAccessor.getServerData().getInt("output_c");
            if (outputCount > 0) {
                iTooltip.add(Component.translatable("block.terracompositio.matter_infuser." + "output").append(" "));
                if (blockAccessor.getServerData().contains("output")) {
                    Item output = Item.byId(blockAccessor.getServerData().getInt("output"));
                    IElementHelper elements = IElementHelper.get();
                    IElement icon = elements.item(new ItemStack(output))
                            .translate(new Vec2(-3,-5))
                            ;
                    iTooltip.append(icon);
                    iTooltip.append(Component.literal(String.valueOf(outputCount)).append(" "));
                    iTooltip.append(Component.translatable(output.getDescriptionId()));
                }
                //iTooltip.add(Component.empty());
            }
        }
        if (blockAccessor.getServerData().contains("catalyst")) {
            int id = blockAccessor.getServerData().getInt("catalyst");
            if (id != 0) {
                Item catalyst = Item.byId(id);
                iTooltip.add(Component.translatable("block.terracompositio.matter_infuser." + "catalyst"));
                IElementHelper elements = IElementHelper.get();
                IElement icon = elements.item(new ItemStack(catalyst))
                        .translate(new Vec2(-3,-5))
                        ;
                iTooltip.append(icon);
                iTooltip.append(Component.translatable(catalyst.getDescriptionId()));
                //iTooltip.add(Component.empty());
            }
        }
        if (blockAccessor.getServerData().contains("cfe_t")) {
            iTooltip.add(Component.translatable("block.terracompositio.matter_infuser." + "cfe_t", blockAccessor.getServerData().getFloat("cfe_t")));
        }
        if (blockAccessor.getServerData().contains("duration")) {
            iTooltip.add(Component.translatable("block.terracompositio.matter_infuser." + "duration", (float)(blockAccessor.getServerData().getInt("duration")/20)));
        }
        if (blockAccessor.getServerData().contains("decay_chance")) {
            iTooltip.add(Component.translatable("block.terracompositio.matter_infuser." + "decay_chance", blockAccessor.getServerData().getInt("decay_chance")));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return TerraCompositio.modLoc("matter_infuser_tooltip");
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        MatterInfuserIOBlockEntity blockEntity = (MatterInfuserIOBlockEntity) blockAccessor.getBlockEntity();
        compoundTag.putInt("cfe", blockEntity.getCFE());
        ItemStack inputSlot = blockEntity.getInputSlot();
        compoundTag.putInt("input_c",inputSlot.getCount());
        compoundTag.putInt("input",Item.getId(inputSlot.getItem()));
        ItemStack outputSlot = blockEntity.getOutputSlot();
        compoundTag.putInt("output_c",outputSlot.getCount());
        compoundTag.putInt("output",Item.getId(outputSlot.getItem()));
        compoundTag.putInt("catalyst", Item.getId(blockEntity.getCatalyst().getItem()));
        Optional<MatterInfusionRecipe> currentRecipe = blockEntity.getCurrentRecipe();
        currentRecipe.ifPresent(matterInfusionRecipe -> {
            compoundTag.putFloat("cfe_t", matterInfusionRecipe.getCFETick());
            compoundTag.putInt("duration", blockEntity.ticksLeft());
            compoundTag.putInt("decay_chance", matterInfusionRecipe.getCatalystDecayRate());
        });
    }

}
