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
import net.sinedkadis.terracompositio.block.entity.TCCFEBlockEntity;
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
    private float cfeTravelSpeed = 1 / 20f;
    private BlockEntity blockEntity = null;
    private Entity entity = null;
    private int index = 0;
    int CFE = 0;
    private int maxCFE = 100;
    private final List<Pair<Integer, Double>> cfeQueue = new ArrayList<>();
    @Getter
    private Function<BlockPos, BlockPos> targetOffset = blockpos -> blockpos;

    public CFEContainer(BlockEntity entity) {
        this.blockEntity = entity;
    }

    public CFEContainer(Entity entity) {
        this.entity = entity;

    }

    public CFEContainer setMaxCFE(int maxCFE) {
        this.maxCFE = maxCFE;
        return this;
    }

    public CFEContainer setIndex(int index) {
        this.index = index;
        return this;
    }

    public CFEContainer setTargetOffset(Function<BlockPos,BlockPos> offset){
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
            sendCFEUpdate(false);
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
        sendCFEUpdate(true);
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

    protected void sendCFEUpdate(boolean onAdd) {
        if (blockEntity instanceof CFENetworkMemberBE cfeNetworkMemberBE
                && blockEntity instanceof TCCFEBlockEntity tcCFEBlockEntity) {
//            TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(cfeNetworkMemberBE, NetworkAction.UPDATE);
            //TerraCompositioAPI.INSTANCE.getCFENetworkInstance().networkMemberUpdated(cfeNetworkMemberBE);
            if ((tcCFEBlockEntity.getBlockMode().consumer() && !onAdd)
                    || (tcCFEBlockEntity.getBlockMode().source() && onAdd))
                TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(cfeNetworkMemberBE, NetworkAction.UPDATE);
        }
        if (entity != null) {
            TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(((CFENetworkMemberEntity) entity), NetworkAction.UPDATE);
        }
    }

    public void writeToNBT(CompoundTag pTag) {
        pTag.put("cfeContainer_"+index, this.serializeNBT());
    }

    public void readFromNBT(CompoundTag pTag) {
        CompoundTag tag = pTag.getCompound("cfeContainer_"+index);
        this.deserializeNBT(tag);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("CFE", CFE);
        nbt.putInt("maxCFE", maxCFE);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        CFE = tag.getInt("CFE");
        maxCFE = tag.getInt("maxCFE");
    }
}
