package net.sinedkadis.terracompositio.cfe;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.INBTSerializable;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberBE;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.entity.ModCFEBlockEntity;

@Getter
@Setter
public class CFEContainer implements ICFEHandler, INBTSerializable<CompoundTag> {
    private final BlockEntity blockEntity;
    int CFE = 0;
    private int maxCFE;

    public CFEContainer(BlockEntity blockEntity, int maxCFE) {
        this.blockEntity = blockEntity;
        this.maxCFE = maxCFE;
    }

    public CFEContainer(BlockEntity blockEntity) {
        this(blockEntity, 100);
    }

    @Override
    public int takeCFE(int cfe,boolean simulate) {
        if (CFE <= 0)
            return 0;
        int taken;
        if (cfe > 0){
            taken = Math.min(CFE,cfe);
        } else if (cfe < 0){
            return -addCFE(-cfe,simulate);
        } else return 0;
        if (!simulate) {
            CFE -= taken;
            onContentsChanged();
            sendCFEUpdate(false);
        }
        return taken;
    }

    @Override
    public int addCFE(int cfe,boolean simulate) {
        if (CFE >= maxCFE)
            return 0;
        int added;
        if (cfe > 0){
            added = Math.min((maxCFE-CFE),cfe);
        } else if (cfe < 0){
            return -takeCFE(-cfe,simulate);
        } else return 0;
        if (!simulate) {
            CFE += added;
            onContentsChanged();
            sendCFEUpdate(true);
        }
        return added;
    }

    @Override
    public int getMinCFE() {
        return 0;
    }

    protected void onContentsChanged(){
        blockEntity.setChanged();
        Level level = blockEntity.getLevel();
        if (level != null && !level.isClientSide()) {
            BlockState blockState = blockEntity.getBlockState();
            level.sendBlockUpdated(blockEntity.getBlockPos(), blockState, blockState, 3);
        }
    }

    protected void sendCFEUpdate(boolean onAdd) {
        if (blockEntity instanceof CFENetworkMemberBE cfeNetworkMemberBE
                && blockEntity instanceof ModCFEBlockEntity modCFEBlockEntity){
//            TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(cfeNetworkMemberBE, NetworkAction.UPDATE);
            //TerraCompositioAPI.INSTANCE.getCFENetworkInstance().networkMemberUpdated(cfeNetworkMemberBE);
            if ((modCFEBlockEntity.getBlockMode().consumer() && !onAdd )
                    || (modCFEBlockEntity.getBlockMode().source() && onAdd))
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
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        CFE = tag.getInt("CFE");
        maxCFE = tag.getInt("maxCFE");
    }
}
