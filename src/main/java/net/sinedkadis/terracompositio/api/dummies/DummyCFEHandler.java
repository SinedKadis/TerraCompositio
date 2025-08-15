package net.sinedkadis.terracompositio.api.dummies;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Function;

public class DummyCFEHandler implements ICFEHandler {
    public static final DummyCFEHandler instance = new DummyCFEHandler();

    @Override
    public int getCFE() {
        return 0;
    }

    @Override
    public List<Pair<Integer, Double>> getCfeQueue() {
        return List.of();
    }

    @Override
    public ICFEHandler setIndex(int index) {
        return instance;
    }

    @Override
    public ICFEHandler setTargetOffset(Function<BlockPos, BlockPos> offset) {
        return instance;
    }

    @Override
    public int takeCFE(int cfe, boolean simulate) {
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
    public BlockEntity getBlockEntity() {
        return null;
    }

    @Override
    public Entity getEntity() {
        return null;
    }

    @Override
    public Function<BlockPos, BlockPos> getTargetOffset() {
        return null;
    }

    @Override
    public void setBlockEntity(BlockEntity blockEntity) {

    }

    @Override
    public void setEntity(Entity entity) {

    }
}
