package net.sinedkadis.terracompositio.cfe;

import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CFEHandlerProxy implements ICFEHandler {
    @Getter
    private final List<ICFEHandler> handlerList = new ArrayList<>() {
        @Override
        public boolean add(ICFEHandler icfeHandler) {
            if (icfeHandler instanceof DummyCFEHandler) return false;
            return super.add(icfeHandler);
        }
    };
    private int queued = 0;

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("CFEHandlerProxy{");
        handlerList.forEach(icfeHandler ->
                stringBuilder.append("\n").append(icfeHandler.toString()).append("\n"));
        stringBuilder.append(",\n\n\n queued=").append(queued)
                .append("\n\n}");
        return stringBuilder.toString();
    }

    @Override
    public int getCFE() {
        int toReturn = 0;
        for (ICFEHandler icfeHandler : handlerList) {
            toReturn += icfeHandler.getCFE();
        }
        return toReturn;
    }

    @Override
    public void setCFE(int cfe) {
        handlerList.get(0).setCFE(cfe);
    }

    @Override
    public int getMaxCFE() {
        int toReturn = 0;
        for (ICFEHandler icfeHandler : handlerList) {
            toReturn += icfeHandler.getMaxCFE();
        }
        return toReturn;
    }

    @Override
    public ICFEHandler setMaxCFE(int max) {
        handlerList.get(0).setMaxCFE(max);
        return this;
    }

    @Override
    public int addCFE(int cfe, boolean simulate) {
        int allAdded = 0;
        int toAdd = cfe;
        for (ICFEHandler icfeHandler : handlerList) {
            int added = icfeHandler.addCFE(toAdd, simulate);
            allAdded += added;
            toAdd -= added;
        }
        return allAdded;
    }

    @Override
    public int takeCFE(int cfe, boolean simulate) {
        int allTaken = 0;
        int toTake = cfe;
        for (ICFEHandler icfeHandler : handlerList) {
            int taken = icfeHandler.takeCFE(toTake, simulate);
            allTaken += taken;
            toTake -= taken;
        }
        return allTaken;
    }

    @Override
    public int sendCFE(CFENetworkMember target, int cfe, float speed, boolean simulate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeToNBT(CompoundTag pTag) {
        handlerList.forEach(icfeHandler -> icfeHandler.writeToNBT(pTag));
    }

    @Override
    public void readFromNBT(CompoundTag pTag) {
        handlerList.forEach(icfeHandler -> icfeHandler.readFromNBT(pTag));
    }

    @Override
    public int getQueued() {
        return queued;
    }

    @Override
    public void setQueued(int queued) {
        this.queued = queued;
    }

    @Override
    public int getCFEWithQueue() {
        return getCFE() + queued;
    }

    @Override
    public boolean isEmpty() {
        boolean toReturn = true;
        for (ICFEHandler icfeHandler : handlerList) {
            toReturn &= icfeHandler.isEmpty();
        }
        return toReturn;
    }

    @Override
    public int getFreeSpace() {
        int toReturn = 0;
        for (ICFEHandler icfeHandler : handlerList) {
            toReturn += icfeHandler.getFreeSpace();
        }
        return toReturn - queued;
    }

    @Override
    public void clear() {
        handlerList.forEach(ICFEHandler::clear);
        queued = 0;
    }

    @Override
    public CFENetworkMember getAttachedMember() {
        return handlerList.get(0).getAttachedMember();
    }

    @Override
    public Function<Vec3, Vec3> getOffset() {
        return handlerList.get(0).getOffset();
    }

    @Override
    public ICFEHandler setOffset(Function<Vec3, Vec3> offset) {
        return handlerList.get(0).setOffset(offset);
    }

    @Override
    public int getIndex() {
        return handlerList.get(0).getIndex();
    }

    @Override
    public ICFEHandler setIndex(int index) {
        return handlerList.get(0).setIndex(index);
    }
}
