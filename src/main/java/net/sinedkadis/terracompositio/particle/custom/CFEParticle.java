package net.sinedkadis.terracompositio.particle.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.sinedkadis.terracompositio.particle.CFEParticleData;
import org.jetbrains.annotations.NotNull;

public class CFEParticle extends TextureSheetParticle{

    CFEParticle(ClientLevel level,
                double x,
                double y,
                double z,
                double xd,
                double yd,
                double zd,
                CFEParticleData data) {
        super(level, x, y, z, xd, yd, zd);
        float speed = data.speed();
        Vec3 targetPos = new Vec3(x + xd, y + yd, z + zd); // Рассчитываем целевую позицию
        this.hasPhysics = false;
        this.gravity = 0.0F;
        if (!(xd == 0 && yd == 0 && zd == 0) ) {
            this.lifetime = (int) ((targetPos.distanceTo(new Vec3(x,y,z))/ speed));
        } else {
            this.lifetime = 20;
        }
        this.scale(0.5F);
        Vec3 direction = new Vec3(
                targetPos.x - this.x,
                targetPos.y - this.y,
                targetPos.z - this.z
        ).normalize().scale(speed);  // нормализуем и умножаем на скорость

        // Обновляем скорость
        this.xd = direction.x;
        this.yd = direction.y;
        this.zd = direction.z;
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
        this.x += this.xd;
        this.y += this.yd;
        this.z += this.zd;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<CFEParticleData> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        public Particle createParticle(@NotNull CFEParticleData pType,
                                       @NotNull ClientLevel pLevel,
                                       double pX,
                                       double pY,
                                       double pZ,
                                       double pXSpeed,
                                       double pYSpeed,
                                       double pZSpeed) {
            CFEParticle particle = new CFEParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed,pType);
            particle.setSpriteFromAge(this.sprite);
            return particle;
        }
    }

}
