package net.sinedkadis.terracompositio.api.dummies;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;

import java.util.function.Function;

@SuppressWarnings("DataFlowIssue")
@MethodsReturnNonnullByDefault
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
    public int sendCFE(int cfe, CFENetworkMember target, float speed, boolean simulate) {
        return 0;
    }

    @Override
    public int sendCFE(int cfe, ICFEHandler target, float speed, boolean simulate) {
        return 0;
    }

    @Override
    public int addCFE(int cfe, boolean simulate) {
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
    public ICFEHandler setMaxCFE(int max) {
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
    public int getCFEWithQueue() {
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
    public double x() {
        return 0;
    }

    @Override
    public double y() {
        return 0;
    }

    @Override
    public double z() {
        return 0;
    }

    @Override
    public BlockPos getPos() {
        return null;
    }

    @Override
    public BlockState getBlockState() {
        return null;
    }

    @Override
    public <T extends BlockEntity> T getEntity() {
        return null;
    }

    @Override
    public ServerLevel getLevel() {
        return null;
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
    public int getIndex() {
        return 0;
    }




}
