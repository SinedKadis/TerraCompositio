package net.sinedkadis.terracompositio.block.entity;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;


@Getter
public class TimePassageDesorberBlockEntity extends AbstractDesorberBlockEntity {
    private int timeBuffer = 0;
    private int timeReSetter = 20;
    private int timeCounter = 0;
    public static final Function<Integer,Double> function = (x) -> (-Math.cos((double) x /50)+1)/1.5f;
    public TimePassageDesorberBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.TIME_PASSAGE_DESORBER_BE.get(), pos, state);
    }

    @Override
    protected int getTankCapacity() {
        return 1000;
    }

    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        super.tick(pLevel, pPos, pState);
        timeReSetter--;
        if (timeReSetter <= 1) {
            timeReSetter = 20;
            if (pLevel.hasNeighborSignal(pPos)){
                addingCFEProcess();
            }else if (isEnoughFluid()) {
                addingTimeProcess(pLevel);
            }
        }
    }

    private void addingTimeProcess(Level pLevel) {
        timeCounter++;
        consumeFluid();
        Double chance = function.apply(timeCounter);
        float random = pLevel.getRandom().nextFloat();
        if (random < chance) {
            timeBuffer++;
            if (chance > 1 && random < (chance-1)){
                timeBuffer++;
                TCUtil.spawnParticlesIn(pLevel,this.worldPosition);
            }
        }
    }

    private void addingCFEProcess() {
        timeCounter = 0;
        if (timeBuffer > 20) {
            timeBuffer -= cfeContainer.addCFE(20,cfeContainer.getBlockPos(), false);
        } else if (timeBuffer > 0){
            timeBuffer -= cfeContainer.addCFE(timeBuffer,cfeContainer.getBlockPos(),false);
        }
    }


    private void consumeFluid() {
        this.fluidHandler.drain(10, IFluidHandler.FluidAction.EXECUTE);
    }

    private boolean isEnoughFluid() {
        return !this.fluidHandler.isEmpty() && this.fluidHandler.getFluidAmount() >= 10;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        pTag.putInt("buffer", timeBuffer);
        pTag.putInt("counter", timeCounter);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        timeBuffer = pTag.getInt("buffer");
        timeCounter = pTag.getInt("counter");
    }
}
