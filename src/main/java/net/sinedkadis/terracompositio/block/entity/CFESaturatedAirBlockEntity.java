package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.particle.CFEParticleData;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CFESaturatedAirBlockEntity extends TCCFEBlockEntity {

    private final List<Vec3> particlePlace = new ArrayList<>();

    public CFESaturatedAirBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(TCBlockEntities.CFE_SATURATED_AIR_BE.get(),pPos, pBlockState,BlockMode.SOURCE);
    }

    @Override
    public int getLimit() {
        return 5;
    }

    private int firstSecond = 20;
    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        super.tick(pLevel, pPos, pState);
        if (this.cfeContainer.getFreeSpace() == this.cfeContainer.getMaxCFE() && firstSecond <=0 ){
            pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(),3);
        }
        if (firstSecond>0)firstSecond--;
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

            float speed = this.cfeContainer.getCfeTravelSpeed();
            level.addParticle(new CFEParticleData(speed),
                    true, cords.x,cords.y,cords.z, 0, 0, 0);
        }
    }

}
