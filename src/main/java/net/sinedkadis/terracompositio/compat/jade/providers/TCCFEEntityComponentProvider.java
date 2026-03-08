package net.sinedkadis.terracompositio.compat.jade.providers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.api.networks.cfe.*;
import net.sinedkadis.terracompositio.cfe.burst.CFEBurstProjectileEntity;
import net.sinedkadis.terracompositio.entity.custom.FlowCedarEntEntity;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

public enum TCCFEEntityComponentProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
    INSTANCE;


    @Override
    public void appendTooltip(ITooltip iTooltip, EntityAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getServerData().contains("cfe")) {
            iTooltip.add(Component.translatable("block.terracompositio." + "extracted_cfe", blockAccessor.getServerData().getInt("cfe")));
        }
        if (blockAccessor.getServerData().contains("inner_cfe")) {
            iTooltip.add(Component.translatable("block.terracompositio." + "cfe", blockAccessor.getServerData().getInt("inner_cfe")));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return TerraCompositio.modLoc("tc_be_tooltip");
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, EntityAccessor blockAccessor) {
        Entity entity = blockAccessor.getEntity();
        if (entity instanceof CFENetworkMemberEntity memberBE){
            ICFEHandler cfeHandler = memberBE.getMainHandler();
            compoundTag.putInt("cfe", cfeHandler.getCFE());
            if (memberBE instanceof FlowCedarEntEntity flowCedarEntEntity){
                flowCedarEntEntity.getInnerCFEOptional().ifPresent(icfeHandler -> compoundTag.putInt("inner_cfe", icfeHandler.getCFE()));
            }
        } else if (entity instanceof CFEBurstProjectileEntity cfeBurstProjectileEntity) {
            compoundTag.putInt("cfe", cfeBurstProjectileEntity.getCFE());
        }

    }

    @Override
    public int getDefaultPriority() {
        return -1000;
    }
}
