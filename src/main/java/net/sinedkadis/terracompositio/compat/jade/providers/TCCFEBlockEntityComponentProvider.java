package net.sinedkadis.terracompositio.compat.jade.providers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberBE;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.entity.TCCFEBlockEntity;
import net.sinedkadis.terracompositio.compat.jade.JadeTerraCompositioPlugin;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.Optional;

public enum TCCFEBlockEntityComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;


    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getServerData().contains("cfe")) {
            iTooltip.add(Component.translatable("block.terracompositio." + "cfe", blockAccessor.getServerData().getInt("cfe")));
        }
        if (blockAccessor.getServerData().contains("max_cfe")  && iPluginConfig.get(JadeTerraCompositioPlugin.debugConfigRL())) {
            iTooltip.add(Component.translatable("block.terracompositio." + "max_cfe", blockAccessor.getServerData().getInt("max_cfe")));
        }
        if (blockAccessor.getServerData().contains("queued")  && iPluginConfig.get(JadeTerraCompositioPlugin.debugConfigRL())) {
            iTooltip.add(Component.translatable("block.terracompositio." + "queued", blockAccessor.getServerData().getInt("queued")));
        }
        if (blockAccessor.getServerData().contains("priority")  && iPluginConfig.get(JadeTerraCompositioPlugin.debugConfigRL())) {
            iTooltip.add(Component.translatable("block.terracompositio." + "priority", blockAccessor.getServerData().getInt("priority")));
        }
        if (blockAccessor.getServerData().contains("limit")  && iPluginConfig.get(JadeTerraCompositioPlugin.debugConfigRL())) {
            iTooltip.add(Component.translatable("block.terracompositio." + "limit", blockAccessor.getServerData().getInt("limit")));
        }
        if (blockAccessor.getServerData().contains("speed")  && iPluginConfig.get(JadeTerraCompositioPlugin.debugConfigRL())) {
            iTooltip.add(Component.translatable("block.terracompositio." + "speed", blockAccessor.getServerData().getFloat("speed")));
        }

    }

    @Override
    public ResourceLocation getUid() {
        return TerraCompositio.modLoc("mod_cfe_be_tooltip");
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        BlockEntity blockEntity = blockAccessor.getBlockEntity();
        if (blockEntity instanceof TCCFEBlockEntity entity){
            compoundTag.putInt("cfe", entity.getCfeContainer().getCFE());
            putDebugData(compoundTag,entity);
        } else if (blockEntity instanceof CFENetworkMemberBE memberBE){
            Optional<ICFEHandler> cfeHandler = CFENetwork.getCFEHandler(memberBE);
            cfeHandler.ifPresent(icfeHandler -> compoundTag.putInt("cfe", icfeHandler.getCFE()));
            putDebugData(compoundTag,memberBE);
        }

    }

    private void putDebugData(CompoundTag compoundTag, CFENetworkMemberBE blockEntity) {
        compoundTag.putInt("priority",blockEntity.getPriority());
        compoundTag.putInt("limit",blockEntity.getLimit());
        if (blockEntity instanceof TCCFEBlockEntity entity) {
            ICFEHandler cfeContainer = entity.getCfeContainer();
            compoundTag.putInt("max_cfe",cfeContainer.getMaxCFE());
            compoundTag.putInt("queued",cfeContainer.getQueued());
            compoundTag.putFloat("speed",cfeContainer.getCfeTravelSpeed());
        }
    }

    @Override
    public int getDefaultPriority() {
        return -1000;
    }
}
