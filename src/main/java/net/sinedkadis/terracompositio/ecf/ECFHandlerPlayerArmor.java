package net.sinedkadis.terracompositio.ecf;

import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.api.dummies.DummyECFHandler;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMember;
import net.sinedkadis.terracompositio.api.networks.ecf.IECFHandler;
import net.sinedkadis.terracompositio.api.registries.TCCapabilities;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ECFHandlerPlayerArmor implements IECFHandler {
    @Getter
    private final IECFHandler handler;
    @Getter
    private final Iterable<ItemStack> handlerList;
    private int queued = 0;

    public ECFHandlerPlayerArmor(IECFHandler IECFHandler) {
        this.handler = IECFHandler;
        handlerList = ((Player) IECFHandler.getAttachedMember().getEntity()).getArmorSlots();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("CFEPlayerProxy{").append(handler).append("\n").append("\n");

        handlerList.forEach(icfeHandler ->
        {
            LazyOptional<IECFHandler> capability = icfeHandler.getCapability(TCCapabilities.ECF);
            if (capability.isPresent()) {
                stringBuilder.append("\n")
                        .append(capability.orElse(DummyECFHandler.instance))
                        .append("\n");
            }
        });
        stringBuilder.append(",\n\n\n Shared Queued=").append(queued)
                .append("\n\n}");
        return stringBuilder.toString();
    }

    @Override
    public int getECF() {
        int toReturn = handler.getECF();
        for (ItemStack itemStack : handlerList) {
            IECFHandler IECFHandler = itemStack.getCapability(TCCapabilities.ECF).orElse(DummyECFHandler.instance);
            toReturn += IECFHandler.getECF();
        }
        return toReturn;
    }

    @Override
    public void setECF(int ecf) {
        handler.setECF(ecf);
    }

    @Override
    public int getMaxECF() {
        int toReturn = handler.getMaxECF();
        for (ItemStack itemStack : handlerList) {
            IECFHandler IECFHandler = itemStack.getCapability(TCCapabilities.ECF).orElse(DummyECFHandler.instance);
            toReturn += IECFHandler.getMaxECF();
        }
        return toReturn;
    }

    @Override
    public IECFHandler setMaxECF(int max) {
        handler.setMaxECF(max);
        return this;
    }

    @Override
    public int addECF(int cfe, boolean simulate) {
        int allAdded = 0;
        int toAdd = cfe - handler.addECF(cfe, false);
        for (ItemStack itemStack : handlerList) {
            IECFHandler IECFHandler = itemStack.getCapability(TCCapabilities.ECF).orElse(DummyECFHandler.instance);
            int added = IECFHandler.addECF(toAdd, simulate);
            allAdded += added;
            toAdd -= added;
        }
        return allAdded;
    }

    @Override
    public int takeECF(int cfe, boolean simulate) {
        int allTaken = 0;
        int toTake = cfe - handler.takeECF(cfe, false);
        for (ItemStack itemStack : handlerList) {
            IECFHandler IECFHandler = itemStack.getCapability(TCCapabilities.ECF).orElse(DummyECFHandler.instance);
            int taken = IECFHandler.takeECF(toTake, simulate);
            allTaken += taken;
            toTake -= taken;
        }
        return allTaken;
    }

    @Override
    public int sendECF(ECFNetworkMember target, int cfe, float speed) {
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
    public int getECFWithQueue() {
        return getECF() + queued;
    }

    @Override
    public boolean isEmpty() {
        boolean toReturn = handler.isEmpty();
        for (ItemStack itemStack : handlerList) {
            IECFHandler IECFHandler = itemStack.getCapability(TCCapabilities.ECF).orElse(DummyECFHandler.instance);
            toReturn &= IECFHandler.isEmpty();
        }
        return toReturn;
    }

    @Override
    public int getFreeSpace() {
        int toReturn = handler.getFreeSpace();
        for (ItemStack itemStack : handlerList) {
            IECFHandler IECFHandler = itemStack.getCapability(TCCapabilities.ECF).orElse(DummyECFHandler.instance);
            toReturn += IECFHandler.getFreeSpace();
        }
        return toReturn - queued;
    }

    @Override
    public void clear() {
        handler.clear();
        for (ItemStack itemStack : handlerList) {
            IECFHandler IECFHandler = itemStack.getCapability(TCCapabilities.ECF).orElse(DummyECFHandler.instance);
            IECFHandler.clear();
        }
        queued = 0;
    }

    @Override
    public ECFNetworkMember getAttachedMember() {
        return handler.getAttachedMember();
    }

    @Override
    public IECFHandler getMainHandler() {
        return handler;
    }

    @Override
    public Function<Vec3, Vec3> getOffset() {
        return handler.getOffset();
    }

    @Override
    public IECFHandler setOffset(Function<Vec3, Vec3> offset) {
        return handler.setOffset(offset);
    }

    @Override
    public int getIndex() {
        return handler.getIndex();
    }

    @Override
    public IECFHandler setIndex(int index) {
        return handler.setIndex(index);
    }
}
