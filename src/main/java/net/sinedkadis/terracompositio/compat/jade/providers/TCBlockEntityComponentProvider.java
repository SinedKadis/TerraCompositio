package net.sinedkadis.terracompositio.compat.jade.providers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum TCBlockEntityComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;


    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        BlockEntity blockEntity = blockAccessor.getBlockEntity();
        if (blockEntity instanceof TCBlockEntity tcBlockEntity) {
            tcBlockEntity.onAppendTooltip(iTooltip,blockAccessor,iPluginConfig);
        }

    }

    @Override
    public ResourceLocation getUid() {
        return TerraCompositio.modLoc("tc_be_tooltip");
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        BlockEntity blockEntity = blockAccessor.getBlockEntity();
        if (blockEntity instanceof TCBlockEntity tcBlockEntity) {
            tcBlockEntity.onAppendServerData(compoundTag);
        }
    }



    @Override
    public int getDefaultPriority() {
        return -1000;
    }
}
