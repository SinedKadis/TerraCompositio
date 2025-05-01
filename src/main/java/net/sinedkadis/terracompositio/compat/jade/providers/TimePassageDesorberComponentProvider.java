package net.sinedkadis.terracompositio.compat.jade.providers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.entity.TimePassageDesorberBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum TimePassageDesorberComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;


    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getServerData().contains("time") && iPluginConfig.get(timeConfigRL())) {
            iTooltip.add(Component.translatable("block.terracompositio.time_passage_desorber." + "time", blockAccessor.getServerData().getInt("time")));
        }
        if (blockAccessor.getServerData().contains("chance") && iPluginConfig.get(chanceConfigRL())) {
            iTooltip.add(Component.translatable("block.terracompositio.time_passage_desorber." + "chance", blockAccessor.getServerData().getInt("chance")));
        }

    }

    @Override
    public ResourceLocation getUid() {
        return TerraCompositio.modLoc("time_passage_desorber_tooltip");
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        TimePassageDesorberBlockEntity blockEntity = (TimePassageDesorberBlockEntity) blockAccessor.getBlockEntity();
        compoundTag.putInt("time", blockEntity.getTimeBuffer());
        compoundTag.putInt("chance", (int) Math.round((TimePassageDesorberBlockEntity.function.apply(blockEntity.getTimeCounter()))*100));
    }

    public ResourceLocation timeConfigRL() {
        return TerraCompositio.modLoc("time_config");
    }
    public ResourceLocation chanceConfigRL() {
        return TerraCompositio.modLoc("chance_config");
    }
}
