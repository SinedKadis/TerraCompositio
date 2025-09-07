package net.sinedkadis.terracompositio.cfe;


import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.*;

import java.util.*;
import java.util.function.Function;

@Getter
@Setter
public class CFEContainer implements ICFEHandler, INBTSerializable<CompoundTag> {
    protected float cfeTravelSpeed = 1 / 20f;
    protected CFENetworkMember attachedMember;
    protected boolean isEntity = false;
    protected int index = 0;
    int CFE = 0;
    protected int maxCFE = 100;
    protected final List<CfeQueueMember> cfeQueue = new ArrayList<>();
    protected Function<Vec3, Vec3> offset = t -> t;

    public CFEContainer(CFENetworkMember attachedMember) {
        this.attachedMember = attachedMember;
        if (attachedMember instanceof  CFENetworkMemberEntity) isEntity = true;
    }

    @Override
    public CFEContainer setCfeTravelSpeed(float cfeTravelSpeed) {
        this.cfeTravelSpeed = cfeTravelSpeed;
        return this;
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
        if (CFE <= 0)
            return 0;
        int taken;
        if (cfe > 0) {
            taken = Math.min(CFE, cfe);
        } else return 0;
        if (!simulate) {
            CFE -= taken;
            onContentsChanged();
            sendCFEUpdate();
        }
        return taken;
    }

    @Override
    public int addCFE(int cfe, BlockPos sourcePos, boolean simulate) {
        if (CFE >= maxCFE)
            return 0;
        int queued = getQueued();
        int added;
        if (cfe > 0) {
            added = Math.min((maxCFE - CFE), cfe);
            added = Math.min(Math.max(maxCFE - CFE - queued, 0), added);
        } else return 0;
        if (added < 1)
            return 0;
        if (!simulate) {
            cfeQueue.add(new CfeQueueMember(cfe,this,sourcePos,((ServerLevel) this.getAttachedMember().getLevel())));
        }
        return added;
    }

    @Override
    public int addCFE(int cfe, ICFEHandler source, boolean simulate,boolean doRender) {
        if (CFE >= maxCFE)
            return 0;
        int queued = getQueued();
        int added;
        if (cfe > 0) {
            added = Math.min((maxCFE - CFE), cfe);
            added = Math.min(Math.max(maxCFE - CFE - queued, 0), added);
        } else return 0;
        if (added < 1)
            return 0;
        if (!simulate) {
            Level level = this.getAttachedMember().getLevel();

            if (!level.isClientSide()) {
                CfeQueueMember  member = new CfeQueueMember(added, this, source, ((ServerLevel) level),doRender);
                cfeQueue.add(member);
            }

        }
        return added;
    }

    public int getQueued() {
        int queued = 0;
        for (CfeQueueMember member : cfeQueue) {
            queued += member.getCfeCount();
        }
        return queued;
    }

    @Override
    public BlockPos getBlockPos() {
        return attachedMember.getBlockPos();
    }

    public void containerTick() {
        Iterator<CfeQueueMember> iterator = cfeQueue.iterator();
        while (iterator.hasNext()) {
            CfeQueueMember entry = iterator.next();
            entry.memberTick();
            if (entry.isEnded()) {
                if (entry.isReached()) {
                    actuallyAddCFE(entry.getCfeCount());
                }
                iterator.remove();
            }

        }
    }

    public void actuallyAddCFE(int cfe) {
        CFE += cfe;
        sendCFEUpdate();
        onContentsChanged();
    }

    protected void onContentsChanged() {
        if (attachedMember instanceof BlockEntity blockEntity) {
            blockEntity.setChanged();
            Level level = blockEntity.getLevel();
            if (level != null && !level.isClientSide()) {
                BlockState blockState = blockEntity.getBlockState();
                level.sendBlockUpdated(blockEntity.getBlockPos(), blockState, blockState, 3);
            }
        }
    }

    protected void sendCFEUpdate() {
        if (attachedMember instanceof CFENetworkMemberBE cfeNetworkMemberBE) {
//            TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(cfeNetworkMemberBE, NetworkAction.UPDATE);
            //TerraCompositioAPI.INSTANCE.getCFENetworkInstance().networkMemberUpdated(cfeNetworkMemberBE);

            TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(cfeNetworkMemberBE, NetworkAction.UPDATE);

        }
        if (isEntity) {
            TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(attachedMember, NetworkAction.UPDATE);
        }
    }

    public void writeToNBT(CompoundTag pTag) {
        pTag.put("cfeContainer_" + index, this.serializeNBT());
    }

    public void readFromNBT(CompoundTag pTag) {
        CompoundTag tag = pTag.getCompound("cfeContainer_" + index);
        this.deserializeNBT(tag);
    }

    @Override
    public boolean isEmpty() {
        return !(this.CFE > 0 || !cfeQueue.isEmpty());
    }

    @Override
    public int getFreeSpace() {
        int queued = getQueued();
        return maxCFE - (CFE + queued);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("CFE", CFE);
        nbt.putInt("queue_size", cfeQueue.size());
        for (int i = 0; i < cfeQueue.size(); i++) {
            CfeQueueMember member = cfeQueue.get(i);
            nbt.put("member_"+i,member.serializeNBT());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        CFE = tag.getInt("CFE");
        int size = tag.getInt("queue_size");
        if (size > 0) cfeQueue.clear();
        for (int i = 0; i < size; i++) {
            CfeQueueMember member = new CfeQueueMember();
            member.deserializeNBT(tag.getCompound("member_"+i));
            cfeQueue.add(member);

        }
    }

}
