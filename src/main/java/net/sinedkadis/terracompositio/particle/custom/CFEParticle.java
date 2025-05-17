package net.sinedkadis.terracompositio.particle.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class CFEParticle extends RisingParticle{
    private final Vec3 targetPos;

    CFEParticle(ClientLevel level, double x, double y, double z,
                double xd, double yd, double zd) {
        super(level, x, y, z, xd, yd, zd);
        this.targetPos = new Vec3(x + xd, y + yd, z + zd); // Рассчитываем целевую позицию
        this.hasPhysics = false;
        this.gravity = 0.0F;
//        this.lifetime = (int) Math.sqrt(Math.sqrt(
//                TCUtil.distSqr(
//                        new Vec3i((int) x, (int) y, (int) z),
//                        new Vec3i((int) targetPos.x, (int) targetPos.y, (int) targetPos.z))))
//                +1;
        this.lifetime = 200;
        this.scale(0.5F);
    }

    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 direction = targetPos.subtract(this.x, this.y, this.z).normalize();//.scale(0.02);
        this.xd = direction.x;
        this.yd = direction.y;
        this.zd = direction.z;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        public Particle createParticle(@NotNull SimpleParticleType pType,
                                       @NotNull ClientLevel pLevel,
                                       double pX,
                                       double pY,
                                       double pZ,
                                       double pXSpeed,
                                       double pYSpeed,
                                       double pZSpeed) {
            CFEParticle $$8 = new CFEParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
            $$8.pickSprite(this.sprite);
            return $$8;
        }
    }

}
