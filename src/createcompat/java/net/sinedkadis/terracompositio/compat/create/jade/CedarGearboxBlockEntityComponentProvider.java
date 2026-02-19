package net.sinedkadis.terracompositio.compat.create.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.compat.CompatUtils;
import net.sinedkadis.terracompositio.compat.create.block.entity.CedarGearboxBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum CedarGearboxBlockEntityComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;


    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (!CompatUtils.CREATE_EXISTENCE.get()) return;
        BlockEntity blockEntity = blockAccessor.getBlockEntity();
        if (blockEntity instanceof CedarGearboxBlockEntity cedarGearboxBlockEntity) {
            CompoundTag serverData = blockAccessor.getServerData();
            cedarGearboxBlockEntity.onAppendTooltip(iTooltip,serverData,iPluginConfig);
        }

    }

    @Override
    public ResourceLocation getUid() {
        return TerraCompositio.modLoc("tc_be_tooltip");
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        BlockEntity blockEntity = blockAccessor.getBlockEntity();
        if (blockEntity instanceof CedarGearboxBlockEntity cedarGearboxBlockEntity) {
            cedarGearboxBlockEntity.onAppendServerData(compoundTag);
        }

    }



    @Override
    public int getDefaultPriority() {
        return -1000;
    }
}
