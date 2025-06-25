package net.sinedkadis.terracompositio.compat.jade.providers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.api.networks.cfe.*;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

import java.util.Optional;

public enum TCCFEEntityComponentProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
    INSTANCE;


    @Override
    public void appendTooltip(ITooltip iTooltip, EntityAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getServerData().contains("cfe")) {
            iTooltip.add(Component.translatable("block.terracompositio." + "cfe", blockAccessor.getServerData().getInt("cfe")));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return TerraCompositio.modLoc("mod_cfe_be_tooltip");
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, EntityAccessor blockAccessor) {
        Entity blockEntity = blockAccessor.getEntity();
        if (blockEntity instanceof CFENetworkMemberEntity memberBE){
            Optional<ICFEHandler> cfeHandler = CFENetwork.getCFEHandler(memberBE);
            cfeHandler.ifPresent(icfeHandler -> compoundTag.putInt("cfe", icfeHandler.getCFE()));
        }

    }

    @Override
    public int getDefaultPriority() {
        return -1000;
    }
}
