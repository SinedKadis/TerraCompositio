package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.cfe.CFENetworkAction;
import net.sinedkadis.terracompositio.api.cfe.CFESource;
import net.sinedkadis.terracompositio.api.cfe.CFENetwork;

import java.util.ArrayList;
import java.util.List;

import static net.sinedkadis.terracompositio.api.cfe.CFENetworkHandler.distSqr;

public abstract class ModCFEBlockEntity extends ModBlockEntity{
    private BlockPos cfeSourceBlockPos;
    private CFESource cfeSource;
    protected int CFE = 0;
    private final int connectRange;
    private final int maxCFE;

    public ModCFEBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,int maxCFE,int connectRange) {
        super(type, pos, state);
        cfeSourceBlockPos = searchForSource();
        this.connectRange = connectRange;
        this.maxCFE = maxCFE;
    }

    public BlockPos searchForSource() {
        CFENetwork network = TerraCompositioAPI.instance().getCFENetworkInstance();
        var closestSource = network.getClosestSource(getBlockPos(), getLevel(), connectRange);
        return closestSource == null ? null : closestSource.getCFESourceBlockPos();
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (!pLevel.isClientSide){
            if (cfeSourceBlockPos == null || !isValidSource(cfeSourceBlockPos)){
                cfeSourceBlockPos = searchForSource();
            }
            if (isValidSource(cfeSourceBlockPos)){
                if (CFE < maxCFE && cfeSource.getCurrentCFE() > 0){
                    double cfe = getTransfer(pPos);
                    addCFE(cfe);
                    cfeSource.takeCFE((int) cfe);
                }
            }
        }
    }

    private double getTransfer(BlockPos pPos) {
        int currentSourceCFE = cfeSource.getCurrentCFE();
        int currentSpace = maxCFE - CFE;
        double cfeTransfer = Math.sqrt(pPos.distSqr(cfeSourceBlockPos)) < connectRange
                ? Math.sqrt(pPos.distSqr(cfeSourceBlockPos))
                : connectRange - Math.sqrt(pPos.distSqr(cfeSourceBlockPos)) < 0
                    ? 0
                    : connectRange - Math.sqrt(pPos.distSqr(cfeSourceBlockPos));
        return Math.min(Math.min(currentSourceCFE,currentSpace),cfeTransfer);
    }

    private void addCFE(double transfer) {
        this.CFE = (int)Math.min(maxCFE, CFE + transfer);
        setChanged();
    }

    private boolean isValidSource(BlockPos source) {
        if (level == null || source == null || !level.isLoaded(source) || distSqr(getBlockPos(), source) > (long) connectRange * connectRange) {
            return false;
        }
        return findSourceCandidateAt(source) != null;
    }

    private CFESource findSourceCandidateAt(BlockPos source) {
        if (level == null || source == null) {
            return null;
        }
        BlockEntity be = level.getBlockEntity(source);
        if (be instanceof CFESource){
            cfeSource = (CFESource) be;
            return cfeSource;
        }
        return null;
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        List<Integer> cords = new ArrayList<>();
        if (cfeSourceBlockPos != null) {
            cords.add(cfeSourceBlockPos.getX());
            cords.add(cfeSourceBlockPos.getY());
            cords.add(cfeSourceBlockPos.getZ());
            pTag.putIntArray("source", cords);
        }
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        int[] cords = pTag.getIntArray("source");
        if (cords.length>0) {
            cfeSourceBlockPos = new BlockPos(cords[0], cords[1], cords[2]);
            findSourceCandidateAt(cfeSourceBlockPos);
        }
    }
}
