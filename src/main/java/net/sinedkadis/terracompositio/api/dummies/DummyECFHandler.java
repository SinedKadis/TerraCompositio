package net.sinedkadis.terracompositio.api.dummies;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetwork;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMember;
import net.sinedkadis.terracompositio.api.networks.ecf.IECFHandler;
import net.sinedkadis.terracompositio.util.IEntityInstance;

import java.util.function.Function;

/**
 * The placeholder, returned by {@link ECFNetwork#createDefaultECFHandler(net.sinedkadis.terracompositio.util.IEntityInstance)}, if Terracompositio is not present
 */
@SuppressWarnings("DataFlowIssue")
@MethodsReturnNonnullByDefault
public class DummyECFHandler implements IECFHandler {
    public static final DummyECFHandler instance = new DummyECFHandler();

    @Override
    public int getECF() {
        return 0;
    }


    @Override
    public IECFHandler setIndex(int index) {
        return instance;
    }

    @Override
    public IECFHandler setOffset(Function<Vec3, Vec3> offset) {
        return null;
    }

    @Override
    public void setECF(int ecf) {

    }

    @Override
    public int takeECF(int cfe, boolean simulate) {
        return 0;
    }

    @Override
    public int sendECF(ECFNetworkMember target, int cfe, float speed) {
        return 0;
    }

    @Override
    public int addECF(int cfe, boolean simulate) {
        return 0;
    }

    @Override
    public int getMaxECF() {
        return 0;
    }



    @Override
    public IECFHandler setMaxECF(int max) {
        return instance;
    }



    @Override
    public void writeToNBT(CompoundTag pTag) {

    }

    @Override
    public void readFromNBT(CompoundTag pTag) {

    }

    @Override
    public int getQueued() {
        return 0;
    }

    @Override
    public void setQueued(int queued) {

    }

    @Override
    public int getECFWithQueue() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }


    @Override
    public int getFreeSpace() {
        return 0;
    }

    @Override
    public void clear() {

    }


    @Override
    public IEntityInstance getAttachedEntity() {
        return null;
    }

    @Override
    public Function<Vec3, Vec3> getOffset() {
        return null;
    }


    @Override
    public int getIndex() {
        return 0;
    }




}
