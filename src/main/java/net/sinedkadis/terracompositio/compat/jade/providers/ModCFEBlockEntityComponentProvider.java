package net.sinedkadis.terracompositio.compat.jade.providers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberBE;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.entity.ModCFEBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.Optional;

public enum ModCFEBlockEntityComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;


    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getServerData().contains("cfe")) {
            iTooltip.add(Component.translatable("block.terracompositio." + "cfe", blockAccessor.getServerData().getInt("cfe")));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return TerraCompositio.modLoc("mod_cfe_be_tooltip");
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        BlockEntity blockEntity = blockAccessor.getBlockEntity();
        if (blockEntity instanceof ModCFEBlockEntity entity){
            compoundTag.putInt("cfe", entity.getCfeContainer().getCFE());
        } else if (blockEntity instanceof CFENetworkMemberBE memberBE){
            Optional<ICFEHandler> cfeHandler = CFENetwork.getCFEHandler(memberBE);
            cfeHandler.ifPresent(icfeHandler -> compoundTag.putInt("cfe", icfeHandler.getCFE()));
        }

    }

    @Override
    public int getDefaultPriority() {
        return -1000;
    }
}
