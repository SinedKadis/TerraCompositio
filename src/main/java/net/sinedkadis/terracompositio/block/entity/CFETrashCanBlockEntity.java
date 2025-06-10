package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CFETrashCanBlockEntity extends TCCFEBlockEntity {

    public CFETrashCanBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(TCBlockEntities.CFE_TRASH_CAN_BE.get(),pPos, pBlockState,Integer.MAX_VALUE,10,BlockMode.CONSUMER);
        cfeContainer.setMaxCFE(Integer.MAX_VALUE);
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        super.tick(pLevel,pPos,pState);
    }

    @Override
    public void onCFENetworkMemberUpdate() {
        TerraCompositioAPI.instance().getCFENetworkInstance().getAllCFENetworkMembers(level).stream()
                .filter(cfeNetworkMemberBE -> TCUtil.distSqr(cfeNetworkMemberBE.getBlockPos(),worldPosition) <= 100)
                .filter(cfeNetworkMemberBE -> !(cfeNetworkMemberBE.getBE() instanceof CreativeCFESourceBlockEntity))
                .filter(cfeNetworkMemberBE -> ((TCCFEBlockEntity) cfeNetworkMemberBE.getBE()).getCfeContainer().getCFE() > 0)
                .filter(cfeNetworkMemberBE -> cfeNetworkMemberBE != this)
                .forEach(cfeNetworkMemberBE -> {
                    int transfer = TCUtil.tryCFETransfer(this, cfeNetworkMemberBE, Integer.MAX_VALUE);
                    if (level != null) {
                        if (level instanceof ServerLevel serverLevel){
                            TCUtil.sendCFEParticles(serverLevel, Vec3.atLowerCornerWithOffset(worldPosition,
                                    this.particleTargetOffset().x,
                                    this.particleTargetOffset().y,
                                    this.particleTargetOffset().z),cfeNetworkMemberBE.getBlockPos(),transfer);
                        }
                    }
                });
    }

    @Override
    public int getLimit() {
        return 10;
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }
}
