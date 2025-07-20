package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PathPointerBlockEntity extends TCCFEBlockEntity implements Nameable {

    // rotationYaw: вокруг Y (в горизонтальной плоскости — блок "смотрит")
    // rotationPitch: вокруг X (наклон вверх/вниз)
    // rotationRoll: вокруг Z (вокруг взгляда, для кручения)
    public float rotationYaw, rotationPitch, rotationRoll;
    public @Nullable BlockPos nextNode;
    public @Nullable BlockPos lastNode;

    public PathPointerBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.PATH_POINTER_BE.get(), pos, state, 0, 5, BlockMode.CONTAINER);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateRotation();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        pTag.putFloat("rot_y", rotationYaw);
        pTag.putFloat("rot_x", rotationPitch);
        pTag.putFloat("rot_z", rotationRoll);
        if (nextNode != null) {
            pTag.putLong("next", nextNode.asLong());
        }
        if (lastNode != null) {
            pTag.putLong("last", lastNode.asLong());
        }
        super.saveAdditional(pTag);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        rotationYaw = pTag.getFloat("rot_y");
        rotationPitch = pTag.getFloat("rot_x");
        rotationRoll = pTag.getFloat("rot_z");
        if (pTag.contains("next"))
            nextNode = BlockPos.of(pTag.getLong("next"));
        if (pTag.contains("last"))
            lastNode = BlockPos.of(pTag.getLong("last"));
    }

    public void highlightNodes() {
        if (level != null && level.isClientSide) {
            // Подсветка lastNode оранжевыми частицами (обычный огонь)
            if (lastNode != null) {
                addFireParticles(level, lastNode); // Оранжевый цвет
            }
            // Подсветка nextNode синими частицами (огонь душ)
            if (nextNode != null) {
                addSoulFireParticles(level, nextNode); // Голубой цвет
            }
        }
    }

    private static void addFireParticles(Level level, BlockPos pos) {
        RandomSource rand = level.random;
        for (int i = 0; i < 3; i++) {
            double x = pos.getX() + 0.5 + (rand.nextDouble() - 0.5) * 0.5;
            double y = pos.getY() + 0.5 + (rand.nextDouble() - 0.5) * 0.5;
            double z = pos.getZ() + 0.5 + (rand.nextDouble() - 0.5) * 0.5;

            // Оранжевые частицы огня
            level.addParticle(ParticleTypes.FLAME, x, y, z,
                    0,0,0);
        }
    }

    private static void addSoulFireParticles(Level level, BlockPos pos) {
        RandomSource rand = level.random;
        for (int i = 0; i < 3; i++) {
            double x = pos.getX() + 0.5 + (rand.nextDouble() - 0.5) * 0.5;
            double y = pos.getY() + 0.5 + (rand.nextDouble() - 0.5) * 0.5;
            double z = pos.getZ() + 0.5 + (rand.nextDouble() - 0.5) * 0.5;

            // Синие частицы огня душ
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z,
                    0,0,0);
        }
    }

    public void updateRotation() {
        if (nextNode == null && lastNode == null) return;

        Vec3 pos = Vec3.atCenterOf(getBlockPos());
        Vec3 dirForward;
        if (nextNode == null) {
            assert lastNode != null;
            dirForward = pos.subtract(Vec3.atCenterOf(lastNode)).normalize();
        } else {
            dirForward = Vec3.atCenterOf(nextNode).subtract(pos).normalize();
        }

        Vec3 dirBackward = lastNode == null ?
                dirForward :
                pos.subtract(Vec3.atCenterOf(lastNode)).normalize();

        Vec3 lookDir = dirForward.add(dirBackward).normalize();

        //noinspection SuspiciousNameCombination
        float yaw = (float) Math.toDegrees(Mth.atan2(lookDir.x,lookDir.z));


        float pitch = (float) Math.toDegrees(Mth.atan2(lookDir.y,Mth.sqrt((float) (Mth.square(lookDir.x)+Mth.square(lookDir.z)))));

        Vec3 up = dirForward.cross(dirBackward).normalize();
        Vec3 right = lookDir.cross(up).normalize();

        float roll = (float) Math.toDegrees(Mth.atan2(right.y,right.x));

        this.rotationYaw = yaw;   // YAW (вокруг Y) → "куда смотрит"
        this.rotationPitch = pitch;      // PITCH (наклон)
        this.rotationRoll = roll;       // ROLL (кручение вокруг направления взгляда)

        this.setChanged();

        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }


    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        rotationYaw = tag.getFloat("rotationX");
        rotationPitch = tag.getFloat("rotationY");
        rotationRoll = tag.getFloat("rotationZ");
    }

    @Override
    public @NotNull Component getName() {
        return Component.translatable("block.terracompositio.path_pointer");
    }

    @Override
    public @Nullable Component getCustomName() {
        return Component.translatable("block.terracompositio.path_pointer");
    }
}
