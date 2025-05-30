package net.sinedkadis.terracompositio.particle.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;

public class CFEParticle extends TextureSheetParticle{
    private final Vec3 targetPos;

    CFEParticle(ClientLevel level, double x, double y, double z,
                double xd, double yd, double zd) {
        super(level, x, y, z, xd, yd, zd);
        this.targetPos = new Vec3(x + xd, y + yd, z + zd); // Рассчитываем целевую позицию
        this.hasPhysics = false;
        this.gravity = 0.0F;
        if (xd != 0 && yd != 0 && zd != 0 ) {
            this.lifetime = (int) Math.sqrt(Math.sqrt(
                    TCUtil.distSqr(
                            new Vec3i((int) x, (int) y, (int) z),
                            new Vec3i((int) targetPos.x, (int) targetPos.y, (int) targetPos.z))));
        } else {
            this.lifetime = 40;
        }
        this.scale(0.5F);
    }

    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        // Пересчитываем направление к targetPos (если нужно)
        Vec3 direction = new Vec3(
                targetPos.x - this.x,
                targetPos.y - this.y,
                targetPos.z - this.z
        ).normalize();//.scale(0.05);  // нормализуем и умножаем на скорость

        // Обновляем скорость
        this.xd = direction.x;
        this.yd = direction.y;
        this.zd = direction.z;

        // Двигаем частицу
        this.x += this.xd;
        this.y += this.yd;
        this.z += this.zd;
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
            $$8.setSpriteFromAge(this.sprite);
            return $$8;
        }
    }

}
