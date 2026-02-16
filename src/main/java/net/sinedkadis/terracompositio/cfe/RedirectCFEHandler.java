package net.sinedkadis.terracompositio.cfe;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;

import java.util.function.Function;

@MethodsReturnNonnullByDefault
public class RedirectCFEHandler implements ICFEHandler {
    ICFEHandler redirectedHandler;
    CFENetworkMember member;

    public RedirectCFEHandler(CFENetworkMember member,ICFEHandler redirectedHandler) {
        this.redirectedHandler = redirectedHandler;
        this.member = member;
    }

    @Override
    public int getCFE() {
        return redirectedHandler.getCFE();
    }

    @Override
    public ICFEHandler setIndex(int index) {
        return redirectedHandler.setIndex(index);
    }

    @Override
    public ICFEHandler setOffset(Function<Vec3, Vec3> offset) {
        return redirectedHandler.setOffset(offset);
    }

    @Override
    public int takeCFE(int cfe, boolean simulate) {
        return redirectedHandler.takeCFE(cfe,simulate);
    }

    @Override
    public int addCFE(int cfe, boolean simulate) {
        return redirectedHandler.addCFE(cfe,simulate);
    }

    @Override
    public int sendCFE(int cfe, ICFEHandler target, boolean simulate) {
        return redirectedHandler.sendCFE(cfe, target, simulate);
    }

    @Override
    public void setCFE(int cfe) {
        redirectedHandler.setCFE(cfe);
    }

    @Override
    public int getMaxCFE() {
        return redirectedHandler.getMaxCFE();
    }

    @Override
    public ICFEHandler setCfeTravelSpeed(float cfeTravelSpeed) {
        redirectedHandler.setCfeTravelSpeed(cfeTravelSpeed);
        return this;
    }

    @Override
    public ICFEHandler setMaxCFE(int max) {
        redirectedHandler.setMaxCFE(max);
        return this;
    }

    @Override
    public void writeToNBT(CompoundTag pTag) {

    }

    @Override
    public void readFromNBT(CompoundTag pTag) {

    }

    @Override
    public int getQueued() {
        return redirectedHandler.getQueued();
    }

    @Override
    public void setQueued(int queued) {
        redirectedHandler.setQueued(queued);
    }

    @Override
    public int getCFEWithQueue() {
        return redirectedHandler.getCFEWithQueue();
    }

    @Override
    public boolean isEmpty() {
        return redirectedHandler.isEmpty();
    }

    @Override
    public int getFreeSpace() {
        return redirectedHandler.getFreeSpace();
    }

    @Override
    public float getCfeTravelSpeed() {
        return redirectedHandler.getCfeTravelSpeed();
    }

    @Override
    public CFENetworkMember getAttachedMember() {
        return member;
    }

    @Override
    public Function<Vec3, Vec3> getOffset() {
        return redirectedHandler.getOffset();
    }

    @Override
    public int getIndex() {
        return redirectedHandler.getIndex();
    }
}
