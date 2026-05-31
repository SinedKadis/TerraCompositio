package net.sinedkadis.terracompositio.particle.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.sinedkadis.terracompositio.particle.FluidParticleData;
import org.jetbrains.annotations.NotNull;

public class FluidFlowParticle extends TextureSheetParticle {
    private final Vec3 targetPos;

    public FluidFlowParticle(ClientLevel level, double x, double y, double z,
                             double xd, double yd, double zd, FluidStack fluid) {
        super(level, x, y, z, xd, yd, zd);
        IClientFluidTypeExtensions clientFluid = IClientFluidTypeExtensions.of(fluid.getFluid());
        this.targetPos = new Vec3(x + xd, y + yd, z + zd); // Рассчитываем целевую позицию
        this.setSprite(Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(clientFluid.getStillTexture(fluid)));
        this.multiplyColor(clientFluid.getTintColor(fluid));
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.lifetime = (int) Math.sqrt(
                Math.sqrt(
                        new Vec3i((int) x, (int) y, (int) z).distSqr(BlockPos.containing(targetPos))
                )
        ) + 1;
        this.scale(0.5F);
    }

    protected void multiplyColor(int color) {
        this.rCol *= (float)(color >> 16 & 255) / 255.0F;
        this.gCol *= (float)(color >> 8 & 255) / 255.0F;
        this.bCol *= (float)(color & 255) / 255.0F;
    }

    @Override
    public void tick() {
        super.tick();

        // Плавное движение к цели
        Vec3 direction = targetPos.subtract(this.x, this.y, this.z).normalize();//.scale(0.2);
        this.xd = direction.x;
        this.yd = direction.y;
        this.zd = direction.z;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<FluidParticleData> {

        public Provider(SpriteSet ignoredSpriteSet) {
        }

        @Override
        public Particle createParticle(@NotNull FluidParticleData data, @NotNull ClientLevel level,
                                       double x, double y, double z,
                                       double xd, double yd, double zd) {

            return new FluidFlowParticle(level, x, y, z,
                    xd, yd, zd, // target position
                    data.getFluidStack());
        }
    }
}
