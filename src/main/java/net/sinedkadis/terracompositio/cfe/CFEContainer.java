package net.sinedkadis.terracompositio.cfe;


import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.network.PacketDistributor;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.*;
import net.sinedkadis.terracompositio.cfe.burst.CFEBurstProjectileEntity;
import net.sinedkadis.terracompositio.network.TCPackets;
import net.sinedkadis.terracompositio.network.packets.S2CPlayerCfeContainerSync;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@Getter
@Setter
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CFEContainer implements ICFEHandler, INBTSerializable<CompoundTag> {
    protected CFENetworkMember attachedMember;
    protected boolean isEntity = false;
    protected int index = 0;
    protected int CFE = 0;
    protected int maxCFE = 100;
    protected Function<Vec3, Vec3> offset = t -> t;
    protected int queued = 0;

    public CFEContainer(CFENetworkMember attachedMember) {
        this.attachedMember = attachedMember;
        if (attachedMember instanceof  CFENetworkMemberEntity) isEntity = true;
    }

    public CFEContainer setMaxCFE(int max) {
        this.maxCFE = max;
        return this;
    }

    @Override
    public ICFEHandler setIndex(int index) {
        this.index = index;
        return this;
    }

    public ICFEHandler setOffset(Function<Vec3, Vec3> offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public int takeCFE(int cfe, boolean simulate) {
        int taken = Mth.clamp(cfe, 0, getCFE());

        if (!simulate) {
            setCFE(getCFE()-taken);

            sendCFEUpdate();
            onContentsChanged();
        }
        return taken;
    }


    @Override
    public int sendCFE(int cfe, ICFEHandler target,float speed, boolean simulate) {
        int freeSpace = target.getFreeSpace();
        int available = this.getCFE();
        int added = Mth.clamp(cfe, 0, Math.min(available, freeSpace));
        if (added < 1)
            return 0;

        if (!simulate) {
            CFEBurstProjectileEntity entity = CFEBurstProjectileEntity.sendBurst(this, target, added, speed);
            if (entity != null)
                target.addToQueue(added);
        }
        return added;
    }

    @Override
    public int sendCFE(int cfe, CFENetworkMember target,float speed, boolean simulate) {
        int freeSpace = target.getMainHandler().getFreeSpace();
        int available = this.getCFE();
        int added = Mth.clamp(cfe, 0, Math.min(available, freeSpace));
        if (added < 1)
            return 0;

        if (!simulate) {
            CFEBurstProjectileEntity entity = CFEBurstProjectileEntity.sendBurst(this, target, added, speed);
            if (entity != null)
                target.getMainHandler().addToQueue(added);
        }
        return added;
    }

    public int addCFE(int cfe,boolean simulate) {
        int pMax = getMaxCFE() - getCFE();
        int added = Mth.clamp(cfe, 0, pMax);
        if (!simulate) {
            setCFE(getCFE()+added);
            
            getAttachedMember().scheduleMemberUpdate();
            onContentsChanged();
        }
        return added;
    }




    protected void onContentsChanged() {
        if (getAttachedMember() instanceof BlockEntity blockEntity) {
            blockEntity.setChanged();
            Level level = blockEntity.getLevel();
            if (level != null && !level.isClientSide()) {
                BlockState blockState = blockEntity.getBlockState();
                level.sendBlockUpdated(blockEntity.getBlockPos(), blockState, blockState, 3);
            }
        }
    }

    protected void sendCFEUpdate() {
        if (getAttachedMember() instanceof CFENetworkMemberBE cfeNetworkMemberBE) {
//            TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(cfeNetworkMemberBE, NetworkAction.UPDATE);
            //TerraCompositioAPI.INSTANCE.getCFENetworkInstance().networkMemberUpdated(cfeNetworkMemberBE);

            TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(cfeNetworkMemberBE, NetworkAction.UPDATE);

        }
        if (isEntity) {
            TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(getAttachedMember(), NetworkAction.UPDATE);
            if (getAttachedMember() instanceof ServerPlayer serverPlayer) {
                TCPackets.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),new S2CPlayerCfeContainerSync(getCFE()));
            }
        }
    }

    public void writeToNBT(CompoundTag pTag) {
        pTag.put("cfeContainer_" + getIndex(), this.serializeNBT());
    }

    public void readFromNBT(CompoundTag pTag) {
        CompoundTag tag = pTag.getCompound("cfeContainer_" + getIndex());
        this.deserializeNBT(tag);
    }

    @Override
    public int getQueued() {
        return queued;
    }

    @Override
    public int getCFEWithQueue() {
        return getCFE() + getQueued();
    }

    public boolean isEmpty() {
        return !(this.getCFE() + getQueued() > 0);
    }

    @Override
    public int getFreeSpace() {
        return getMaxCFE() - (getCFE() + getQueued());
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("CFE", getCFE());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        setCFE(tag.getInt("CFE"));
    }

}
