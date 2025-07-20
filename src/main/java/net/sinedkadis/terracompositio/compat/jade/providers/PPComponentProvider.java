package net.sinedkadis.terracompositio.compat.jade.providers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum PPComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;


    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {

        if (blockAccessor.getServerData().contains("yaw")) {
            iTooltip.add(Component.translatable("block.terracompositio.pp." + "yaw", blockAccessor.getServerData().getFloat("yaw")));
        }
        if (blockAccessor.getServerData().contains("pitch")) {
            iTooltip.add(Component.translatable("block.terracompositio.pp." + "pitch", blockAccessor.getServerData().getFloat("pitch")));
        }
        if (blockAccessor.getServerData().contains("roll")) {
            iTooltip.add(Component.translatable("block.terracompositio.pp." + "roll", blockAccessor.getServerData().getFloat("roll")));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return TerraCompositio.modLoc("matter_infuser_tooltip");
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        PathPointerBlockEntity blockEntity = (PathPointerBlockEntity) blockAccessor.getBlockEntity();
        compoundTag.putFloat("yaw", blockEntity.rotationYaw);
        compoundTag.putFloat("pitch", blockEntity.rotationPitch);
        compoundTag.putFloat("roll", blockEntity.rotationRoll);

    }

}
