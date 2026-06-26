package net.sinedkadis.terracompositio.util.helpers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.sinedkadis.terracompositio.particle.ECFParticleData;
import net.sinedkadis.terracompositio.particle.FluidParticleData;
import net.sinedkadis.terracompositio.registries.TCParticles;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ParticleHelperInternal {

    public static void sendFluidParticles(ServerLevel level, BlockPos target, BlockPos source,
                                          int particleAmount, FluidStack fluidStack) {
        if (fluidStack.isEmpty() || particleAmount <= 0 || level == null) return;

        Vec3 targetCenter = Vec3.atCenterOf(target);
        Vec3 sourceCenter = Vec3.atCenterOf(source);

        // Создаем данные частицы
        FluidParticleData particleData = new FluidParticleData(TCParticles.FLUID_FLOW.get(), fluidStack);

        for (int i = 0; i < particleAmount; i++) {
            double offsetX = sourceCenter.x + (level.random.nextDouble() - 0.5) * 0.8;
            double offsetY = sourceCenter.y + (level.random.nextDouble() - 0.5) * 0.8;
            double offsetZ = sourceCenter.z + (level.random.nextDouble() - 0.5) * 0.8;

            // Отправляем частицу с данными о жидкости
            level.sendParticles(particleData,
                    offsetX, offsetY, offsetZ,
                    0, // count
                    targetCenter.x - offsetX, // xd (направление к цели)
                    targetCenter.y - offsetY, // yd
                    targetCenter.z - offsetZ,// zd
                    Math.sqrt(target.distSqr(source))); // speed
        }
    }

    public static void spawnParticlesIn(Level pLevel, BlockPos targetPos) {
        if (pLevel instanceof ServerLevel level) {
            float speed = 1 / 20f;
            RandomSource random = pLevel.getRandom();
            level.sendParticles(new ECFParticleData(speed),
                    targetPos.getX() + random.nextFloat(),
                    targetPos.getY() + random.nextFloat(),
                    targetPos.getZ() + random.nextFloat(),
                    0,
                    random.nextFloat(),
                    random.nextFloat(),
                    random.nextFloat(),
                    1);
        }
    }

    public static void spawnParticlesIn(Level pLevel, BlockPos targetPos, int count) {
        for (int i = 0; i < count; i++) {
            spawnParticlesIn(pLevel, targetPos);
        }
    }

    public static void sendECFParticles(ServerLevel level, Vec3 target, Vec3 source, int particleAmount, List<Vec3> offsets, float speed) {
        if (particleAmount <= 0 || level == null) return;
        if (target == null) return;

        if (offsets == null) {
            offsets = new ArrayList<>();
        }
        if (offsets.isEmpty()) {
            RandomSource random = level.random;

            for (int i = 0; i < particleAmount; i++) {
                Vec3 vec3 = getSpreadParticleOffset(random, particleAmount);
                offsets.add(vec3);
            }
        }

        for (int i = 0; i < particleAmount; i++) {
            double offsetX;
            double offsetY;
            double offsetZ;

            Vec3 vec3 = offsets.get(i);

            offsetX = vec3.x + source.x;
            offsetY = vec3.y + source.y;
            offsetZ = vec3.z + source.z;

            level.sendParticles(new ECFParticleData(speed),
                    offsetX, offsetY, offsetZ,
                    0, // count
                    target.x - offsetX + vec3.x, // xd (направление к цели)
                    target.y - offsetY + vec3.y, // yd
                    target.z - offsetZ + vec3.z,// zd
                    1); // speed
        }
    }

    public static void sendECFParticles(ServerLevel level, Vec3 target, Vec3 source, int particleAmount, List<Vec3> offsets) {
        sendECFParticles(level, target, source, particleAmount, offsets, 1 / 20f);
    }

    public static @NotNull Vec3 getSpreadParticleOffset(RandomSource random, int count) {
        double baseRadius = 0.2 + 0.3 * Math.log1p(count * 0.1);

        double u = random.nextDouble();
        double v = random.nextDouble();
        double w = random.nextDouble();

        double theta = 2 * Math.PI * u;
        double phi = Math.acos(2 * v - 1);

        double r = baseRadius * Math.sqrt(w);

        double x = r * Math.sin(phi) * Math.cos(theta);
        double y = r * Math.sin(phi) * Math.sin(theta);
        double z = r * Math.cos(phi);

        return new Vec3(x, y, z);
    }

    public static void sendECFParticles(ServerLevel level, Vec3 target, Vec3 source, int particleAmount) {
        sendECFParticles(level, target, source, particleAmount, null);
    }

    public static void drawEcfParticle(PoseStack pPoseStack, int pPackedLight, VertexConsumer buffer) {
        PoseStack.Pose pose = pPoseStack.last();
        var matrix = pose.pose();
        var normal = pose.normal();

        //pPackedLight = 0xF000F0;

        float size = 0.1f;

        int pAlpha = 200;
        buffer.vertex(matrix, -size, -size, 0)
                .color(255, 255, 255, pAlpha)
                .uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(pPackedLight)
                .normal(normal, 0, 0, 1)
                .endVertex();

        buffer.vertex(matrix, size, -size, 0)
                .color(255, 255, 255, pAlpha)
                .uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(pPackedLight)
                .normal(normal, 0, 0, 1)
                .endVertex();

        buffer.vertex(matrix, size, size, 0)
                .color(255, 255, 255, pAlpha)
                .uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(pPackedLight)
                .normal(normal, 0, 0, 1)
                .endVertex();

        buffer.vertex(matrix, -size, size, 0)
                .color(255, 255, 255, pAlpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(pPackedLight)
                .normal(normal, 0, 0, 1)
                .endVertex();
    }
}
