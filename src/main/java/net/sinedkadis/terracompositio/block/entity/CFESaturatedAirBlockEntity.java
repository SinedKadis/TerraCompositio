package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCParticles;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CFESaturatedAirBlockEntity extends TCCFEBlockEntity {

    private final List<Vec3> particlePlace = new ArrayList<>();

    public CFESaturatedAirBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(TCBlockEntities.CFE_SATURATED_AIR_BE.get(),pPos, pBlockState,BlockMode.SOURCE);
        this.cfeContainer.setCFE(100);
    }

    @Override
    public int getLimit() {
        return 5;
    }

    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        super.tick(pLevel, pPos, pState);
        if (this.cfeContainer.getCFE() <= 0){
            pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(),3);
        }
    }

    @Override
    public void onCFENetworkMemberUpdate(Level level, BlockPos pos) {
    }

    public void spawnParticles(Level level, BlockPos pos, RandomSource random) {
        int particleCount = this.cfeContainer.getCFE();
        if (particleCount <= 0) return;


        Supplier<Float> floatSupplier = () -> (float) (0.5f + (random.nextFloat() - 0.5f)*2);

        for (int i = 0; i < particleCount/10; i++) {
            if (i >= particlePlace.size()){
                particlePlace.add(new Vec3(pos.getX() + floatSupplier.get(),
                        pos.getY() + floatSupplier.get(),
                        pos.getZ() + floatSupplier.get()));

            }
            Vec3 cords = particlePlace.get(i);
            level.addParticle(TCParticles.CFE_PARTICLE.get(), true, cords.x,cords.y,cords.z, 0, 0, 0);
        }
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
