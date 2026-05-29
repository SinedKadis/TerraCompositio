package net.sinedkadis.terracompositio.compat.jade.providers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.entity.MatterInfuserUnitBlockEntity;
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
        if (blockAccessor.getServerData().contains("catalyst") && iPluginConfig.get(catalystConfigRL())) {
            int id = blockAccessor.getServerData().getInt("catalyst");
            if (id != 0) {
                Item catalyst = Item.byId(id);
                iTooltip.add(Component.translatable("block.terracompositio.matter_infuser." + "catalyst"));
                IElementHelper elements = IElementHelper.get();
                IElement icon = elements.item(new ItemStack(catalyst),0.7f)
                        .translate(new Vec2(-2,-4))
                        ;
                iTooltip.add(icon);
                iTooltip.append(Component.translatable(catalyst.getDescriptionId()));
                //iTooltip.add(Component.empty());
            }
        }
        if (blockAccessor.getServerData().contains("cfe_t") && iPluginConfig.get(cfeTickConfigRL())) {
            iTooltip.add(Component.translatable("block.terracompositio.matter_infuser." + "cfe_t", blockAccessor.getServerData().getFloat("cfe_t")));
        }
        if (blockAccessor.getServerData().contains("duration") && iPluginConfig.get(durationConfigRL())) {
            iTooltip.add(Component.translatable("block.terracompositio.matter_infuser." + "duration", (float)(blockAccessor.getServerData().getInt("duration")/20)));
        }
        if (blockAccessor.getServerData().contains("decay_chance") && iPluginConfig.get(decayChanceConfigRL())) {
            iTooltip.add(Component.translatable("block.terracompositio.matter_infuser." + "decay_chance", blockAccessor.getServerData().getInt("decay_chance")));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return TerraCompositio.modLoc("matter_infuser_tooltip");
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        MatterInfuserUnitBlockEntity blockEntity = (MatterInfuserUnitBlockEntity) blockAccessor.getBlockEntity();
        compoundTag.putInt("catalyst", Item.getId(blockEntity.getCatalyst().getItem()));
        Optional<MatterInfusionRecipe> currentRecipe = blockEntity.getCurrentRecipe();
        currentRecipe.ifPresent(matterInfusionRecipe -> {
            compoundTag.putFloat("cfe_t", matterInfusionRecipe.getCFETick());
            compoundTag.putInt("duration", blockEntity.ticksLeft());
            compoundTag.putInt("decay_chance", matterInfusionRecipe.getCatalystDecayRate());
        });

        ItemStack inputSlot = blockEntity.getInputSlot();
        ItemStack outputSlot = blockEntity.getSlotOutput();

        compoundTag.putInt("input_c",inputSlot.getCount());
        compoundTag.putInt("input",Item.getId(inputSlot.getItem()));
        compoundTag.putInt("output_c", outputSlot.getCount());
        compoundTag.putInt("output", Item.getId(outputSlot.getItem()));

    }

    public ResourceLocation catalystConfigRL() {
        return TerraCompositio.modLoc("catalyst_config");
    }
    public ResourceLocation cfeTickConfigRL() {
        return TerraCompositio.modLoc("cfe_tick_config");
    }
    public ResourceLocation decayChanceConfigRL() {
        return TerraCompositio.modLoc("decay_chance_config");
    }
    public ResourceLocation durationConfigRL() {
        return TerraCompositio.modLoc("duration_config");
    }
}
