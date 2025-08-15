package net.sinedkadis.terracompositio.cfe;


import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.INBTSerializable;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberBE;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Getter
@Setter
public class CFEContainer implements ICFEHandler, INBTSerializable<CompoundTag> {
    protected float cfeTravelSpeed = 1 / 20f;
    protected BlockEntity blockEntity = null;
    protected Entity entity = null;
    protected int index = 0;
    int CFE = 0;
    protected int maxCFE = 100;
    protected final List<Pair<Integer, Double>> cfeQueue = new ArrayList<>();
    protected Function<BlockPos, BlockPos> targetOffset = blockpos -> blockpos;

    public CFEContainer(BlockEntity entity) {
        this.blockEntity = entity;
    }

    public CFEContainer(Entity entity) {
        this.entity = entity;

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

    @Override
    public ICFEHandler setTargetOffset(Function<BlockPos, BlockPos> offset) {
        this.targetOffset = offset;
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
        Optional<Pair<Integer, Double>> quequed = cfeQueue.stream().reduce(
                (integerDoublePair, integerDoublePair2) ->
                        Pair.of(integerDoublePair.getKey() + integerDoublePair2.getKey(), integerDoublePair.getValue()));
        int added;
        if (cfe > 0) {
            added = Math.min((maxCFE - CFE), cfe);
            if (quequed.isPresent()) {
                added = Math.min(Math.max(maxCFE - CFE - quequed.get().getKey(), 0), added);
            }
        } else return 0;
        if (added < 1)
            return 0;
        if (!simulate) {
            if (blockEntity != null)
                cfeQueue.add(new MutablePair<>(cfe, Math.sqrt(TCUtil.distSqr(sourcePos, this.blockEntity.getBlockPos()))));
            else if (entity != null)
                cfeQueue.add(new MutablePair<>(cfe, Math.sqrt(TCUtil.distSqr(sourcePos, targetOffset.apply(((CFENetworkMemberEntity) this.entity).getBlockPos())))));
        }
        return added;
    }

    public int getQueued() {
        int queued = 0;
        for (Pair<Integer, Double> pair : cfeQueue) {
            queued += pair.getKey();
        }
        return queued;
    }

    @Override
    public BlockPos getBlockPos() {
        if (blockEntity != null) return blockEntity.getBlockPos();
        return entity.getOnPos();
    }

    public void containerTick() {
        Iterator<Pair<Integer, Double>> iterator = cfeQueue.iterator();
        while (iterator.hasNext()) {
            Pair<Integer, Double> entry = iterator.next();
            int cfe = entry.getKey();
            double dist = entry.getValue();
            if (dist > cfeTravelSpeed) {
                entry.setValue(dist - cfeTravelSpeed);
            } else {
                actuallyAddCFE(cfe);
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
        if (blockEntity != null) {
            blockEntity.setChanged();
            Level level = blockEntity.getLevel();
            if (level != null && !level.isClientSide()) {
                BlockState blockState = blockEntity.getBlockState();
                level.sendBlockUpdated(blockEntity.getBlockPos(), blockState, blockState, 3);
            }
        }
    }

    protected void sendCFEUpdate() {
        if (blockEntity instanceof CFENetworkMemberBE cfeNetworkMemberBE) {
//            TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(cfeNetworkMemberBE, NetworkAction.UPDATE);
            //TerraCompositioAPI.INSTANCE.getCFENetworkInstance().networkMemberUpdated(cfeNetworkMemberBE);

            TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(cfeNetworkMemberBE, NetworkAction.UPDATE);

        }
        if (entity != null) {
            TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(((CFENetworkMemberEntity) entity), NetworkAction.UPDATE);
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
        int queued = 0;
        for (Pair<Integer, Double> pair : cfeQueue) {
            queued += pair.getKey();
        }
        return maxCFE - (CFE + queued);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("CFE", CFE);
        nbt.putInt("queue_size", cfeQueue.size());
        for (int i = 0; i < cfeQueue.size(); i++) {
            Pair<Integer, Double> pair = cfeQueue.get(i);
            nbt.putInt("queued_int_" + i, pair.getKey());
            nbt.putDouble("queued_double_" + i, pair.getValue());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        CFE = tag.getInt("CFE");
        int size = tag.getInt("queue_size");
        if (size > 0) cfeQueue.clear();
        for (int i = 0; i < size; i++) {
            cfeQueue.add(new MutablePair<>(tag.getInt("queued_int_" + i), tag.getDouble("queued_double_" + i)));
        }
    }

}
