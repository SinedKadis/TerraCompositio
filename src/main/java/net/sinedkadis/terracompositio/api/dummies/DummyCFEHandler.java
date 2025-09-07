package net.sinedkadis.terracompositio.api.dummies;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;

import java.util.function.Function;

public class DummyCFEHandler implements ICFEHandler {
    public static final DummyCFEHandler instance = new DummyCFEHandler();

    @Override
    public int getCFE() {
        return 0;
    }


    @Override
    public ICFEHandler setIndex(int index) {
        return instance;
    }

    @Override
    public ICFEHandler setOffset(Function<Vec3, Vec3> offset) {
        return null;
    }


    @Override
    public int takeCFE(int cfe, boolean simulate) {
        return 0;
    }

    @Override
    public int addCFE(int cfe, ICFEHandler source, boolean simulate, boolean doRender) {
        return 0;
    }


    @Override
    public int addCFE(int cfe, BlockPos sourcePos, boolean simulate) {
        return 0;
    }

    @Override
    public void setCFE(int cfe) {

    }

    @Override
    public int getMaxCFE() {
        return 0;
    }

    @Override
    public ICFEHandler setCfeTravelSpeed(float cfeTravelSpeed) {
        return instance;
    }

    @Override
    public ICFEHandler setMaxCFE(int max) {
        return instance;
    }

    @Override
    public void containerTick() {

    }

    @Override
    public void writeToNBT(CompoundTag pTag) {

    }

    @Override
    public void readFromNBT(CompoundTag pTag) {

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
    public int getQueued() {
        return 0;
    }

    @Override
    public BlockPos getBlockPos() {
        return null;
    }

    @Override
    public float getCfeTravelSpeed() {
        return 0;
    }

    @Override
    public CFENetworkMember getAttachedMember() {
        return null;
    }

    @Override
    public Function<Vec3, Vec3> getOffset() {
        return null;
    }


    @Override
    public void setAttachedMember(CFENetworkMember member) {

    }

    @Override
    public boolean isEntity() {
        return false;
    }

    @Override
    public int getIndex() {
        return 0;
    }


}
