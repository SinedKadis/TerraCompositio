package net.sinedkadis.terracompositio.cfe;

import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CFEHandlerPlayerArmor implements ICFEHandler {
    @Getter
    private final ICFEHandler handler;
    @Getter
    private final Iterable<ItemStack> handlerList;
    private int queued = 0;

    public CFEHandlerPlayerArmor(ICFEHandler icfeHandler) {
        this.handler = icfeHandler;
        handlerList = ((Player) icfeHandler.getAttachedMember().getEntity()).getArmorSlots();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("CFEPlayerProxy{").append(handler).append("\n").append("\n");

        handlerList.forEach(icfeHandler ->
        {
            LazyOptional<ICFEHandler> capability = icfeHandler.getCapability(TCCapabilities.CFE);
            if (capability.isPresent()) {
                stringBuilder.append("\n")
                        .append(capability.orElse(DummyCFEHandler.instance))
                        .append("\n");
            }
        });
        stringBuilder.append(",\n\n\n Shared Queued=").append(queued)
                .append("\n\n}");
        return stringBuilder.toString();
    }

    @Override
    public int getCFE() {
        int toReturn = handler.getCFE();
        for (ItemStack itemStack : handlerList) {
            ICFEHandler icfeHandler = itemStack.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance);
            toReturn += icfeHandler.getCFE();
        }
        return toReturn;
    }

    @Override
    public void setCFE(int cfe) {
        handler.setCFE(cfe);
    }

    @Override
    public int getMaxCFE() {
        int toReturn = handler.getMaxCFE();
        for (ItemStack itemStack : handlerList) {
            ICFEHandler icfeHandler = itemStack.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance);
            toReturn += icfeHandler.getMaxCFE();
        }
        return toReturn;
    }

    @Override
    public ICFEHandler setMaxCFE(int max) {
        handler.setMaxCFE(max);
        return this;
    }

    @Override
    public int addCFE(int cfe, boolean simulate) {
        int allAdded = 0;
        int toAdd = cfe - handler.addCFE(cfe, false);
        for (ItemStack itemStack : handlerList) {
            ICFEHandler icfeHandler = itemStack.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance);
            int added = icfeHandler.addCFE(toAdd, simulate);
            allAdded += added;
            toAdd -= added;
        }
        return allAdded;
    }

    @Override
    public int takeCFE(int cfe, boolean simulate) {
        int allTaken = 0;
        int toTake = cfe - handler.takeCFE(cfe, false);
        for (ItemStack itemStack : handlerList) {
            ICFEHandler icfeHandler = itemStack.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance);
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
        handler.writeToNBT(pTag);
    }

    @Override
    public void readFromNBT(CompoundTag pTag) {
        handler.readFromNBT(pTag);
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
        boolean toReturn = handler.isEmpty();
        for (ItemStack itemStack : handlerList) {
            ICFEHandler icfeHandler = itemStack.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance);
            toReturn &= icfeHandler.isEmpty();
        }
        return toReturn;
    }

    @Override
    public int getFreeSpace() {
        int toReturn = handler.getFreeSpace();
        for (ItemStack itemStack : handlerList) {
            ICFEHandler icfeHandler = itemStack.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance);
            toReturn += icfeHandler.getFreeSpace();
        }
        return toReturn - queued;
    }

    @Override
    public void clear() {
        handler.clear();
        for (ItemStack itemStack : handlerList) {
            ICFEHandler icfeHandler = itemStack.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance);
            icfeHandler.clear();
        }
        queued = 0;
    }

    @Override
    public CFENetworkMember getAttachedMember() {
        return handler.getAttachedMember();
    }

    @Override
    public ICFEHandler getMainHandler() {
        return handler;
    }

    @Override
    public Function<Vec3, Vec3> getOffset() {
        return handler.getOffset();
    }

    @Override
    public ICFEHandler setOffset(Function<Vec3, Vec3> offset) {
        return handler.setOffset(offset);
    }

    @Override
    public int getIndex() {
        return handler.getIndex();
    }

    @Override
    public ICFEHandler setIndex(int index) {
        return handler.setIndex(index);
    }
}
