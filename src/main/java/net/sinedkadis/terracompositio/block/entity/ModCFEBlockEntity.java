package net.sinedkadis.terracompositio.block.entity;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.cfe.CFESource;
import net.sinedkadis.terracompositio.api.cfe.CFENetwork;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.sinedkadis.terracompositio.util.CFENetworkHandler.distSqr;

public abstract class ModCFEBlockEntity extends ModBlockEntity{

    @Getter
    private BlockPos cfeSourceBlockPos;
    private CFESource cfeSource;
    @Getter
    protected int CFE = 0;
    private final int connectRange;
    private final int maxCFE;

    public ModCFEBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,int maxCFE,int connectRange) {
        super(type, pos, state);
        if (!(maxCFE == 0 || connectRange == 0)){
            cfeSourceBlockPos = searchForSource();
        }
        this.connectRange = connectRange;
        this.maxCFE = maxCFE;
    }

    public ModCFEBlockEntity(BlockEntityType<?> type, BlockPos pPos, BlockState pBlockState) {
        this(type,pPos,pBlockState,0,0);
    }

    public BlockPos searchForSource() {
        CFENetwork network = TerraCompositioAPI.instance().getCFENetworkInstance();
        var closestSource = network.getClosestSourceWithCFE(getBlockPos(), getLevel(), connectRange*2);
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
                    addCFE(cfeSource.takeCFE((int) cfe));
                    ModCFEBlockEntity blockEntity = (ModCFEBlockEntity) pLevel.getBlockEntity(pPos);
                    if (blockEntity != null) {
                        TCUtil.spawnParticles(pLevel,pPos, blockEntity.getCfeSourceBlockPos());
                    }
                }
            }
        }
    }

    private double getTransfer(BlockPos pPos) {
        if (maxCFE == 0)
            return 0;
        int currentSourceCFE = cfeSource.getCurrentCFE();
        int currentSpace = maxCFE - CFE;
        double cfeTransfer;
        if (Math.sqrt(pPos.distSqr(cfeSourceBlockPos)) < connectRange)
            cfeTransfer = Math.sqrt(pPos.distSqr(cfeSourceBlockPos));
        else {
            if (connectRange - Math.sqrt(pPos.distSqr(cfeSourceBlockPos)) < 0)
                cfeTransfer = 0;
            else
                cfeTransfer = connectRange - Math.sqrt(pPos.distSqr(cfeSourceBlockPos));
        }
        return Math.min(Math.min(currentSourceCFE,currentSpace),cfeTransfer);
    }

    private void addCFE(double transfer) {
        if (maxCFE == 0)
            return;
        this.CFE = (int)Math.min(maxCFE, CFE + transfer);
        setChanged();
    }

    private boolean isValidSource(BlockPos source) {
        if (connectRange == 0)
            return false;
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
            if (cfeSource.getCurrentCFE() > 0)
                return cfeSource;
        }
        return null;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        if (!(maxCFE == 0 || connectRange == 0)) {
            List<Integer> cords = new ArrayList<>();
            if (cfeSourceBlockPos != null) {
                cords.add(cfeSourceBlockPos.getX());
                cords.add(cfeSourceBlockPos.getY());
                cords.add(cfeSourceBlockPos.getZ());
                pTag.putIntArray("source", cords);
            }
            pTag.putInt("cfe", CFE);
        }
        super.saveAdditional(pTag);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        if (!(maxCFE == 0 || connectRange == 0)) {
            int[] cords = pTag.getIntArray("source");
            CFE = pTag.getInt("cfe");
            if (cords.length > 0) {
                cfeSourceBlockPos = new BlockPos(cords[0], cords[1], cords[2]);
                findSourceCandidateAt(cfeSourceBlockPos);
            }
        }
    }

}
