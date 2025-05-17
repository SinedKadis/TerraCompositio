package net.sinedkadis.terracompositio.cfe;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.INBTSerializable;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberBE;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;

@Getter
@Setter
public class CFEContainer implements ICFEHandler, INBTSerializable<CompoundTag> {
    private final BlockEntity blockEntity;
    int CFE = 0;
    private int maxCFE;
    private int minCFE;

    public CFEContainer(BlockEntity blockEntity,int minCFE, int maxCFE) {
        this.blockEntity = blockEntity;
        this.minCFE = minCFE;
        this.maxCFE = maxCFE;
    }

    public CFEContainer(BlockEntity blockEntity) {
        this(blockEntity,0,100);
    }

    @Override
    public int takeCFE(int cfe) {
        if (cfe != 0){
            int deltaCFE = CFE - cfe;
            if (Mth.clamp(deltaCFE, minCFE, maxCFE) == deltaCFE) {
                this.CFE = deltaCFE;
                onContentsChanged();
                return cfe;
            } else {
                if (cfe < 0) {
                    this.CFE = maxCFE;
                    onContentsChanged();
                    return cfe + (deltaCFE - maxCFE);
                } else {
                    this.CFE = minCFE;
                    onContentsChanged();
                    return cfe + (deltaCFE - minCFE);
                }
            }
        }
        return 0;
    }

    protected void onContentsChanged(){
        blockEntity.setChanged();
        Level level = blockEntity.getLevel();
        if (level != null && !level.isClientSide()) {
            BlockState blockState = blockEntity.getBlockState();
            level.sendBlockUpdated(blockEntity.getBlockPos(), blockState, blockState, 3);
        }
        if (blockEntity instanceof CFENetworkMemberBE cfeNetworkMemberBE){
            TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(cfeNetworkMemberBE, NetworkAction.UPDATE);
        }
    }

    public void writeToNBT(CompoundTag pTag){
        pTag.put("cfeContainer", this.serializeNBT());
    }

    public void readFromNBT(CompoundTag pTag){
        CompoundTag tag = pTag.getCompound("cfeContainer");
        this.deserializeNBT(tag);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("CFE",CFE);
        nbt.putInt("maxCFE", maxCFE);
        nbt.putInt("minCFE",minCFE);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        CFE = tag.getInt("CFE");
        maxCFE = tag.getInt("maxCFE");
        minCFE = tag.getInt("minCFE");
    }
}
