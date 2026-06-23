package net.sinedkadis.terracompositio.particle.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.sinedkadis.terracompositio.particle.ECFParticleData;
import org.jetbrains.annotations.NotNull;

public class ECFParticle extends TextureSheetParticle{

    ECFParticle(ClientLevel level,
                double x,
                double y,
                double z,
                double xd,
                double yd,
                double zd,
                ECFParticleData data) {
        super(level, x, y, z, xd, yd, zd);
        float speed = data.speed();
        double distance = Math.sqrt(xd*xd + yd*yd + zd*zd);
        this.hasPhysics = false;
        this.gravity = 0.0F;
        if (!(xd == 0 && yd == 0 && zd == 0) ) {
            this.lifetime = (int) (distance / speed);
        } else {
            this.lifetime = 5;
        }
        this.scale(0.5F);
        Vec3 direction = new Vec3(xd, yd, zd).normalize().scale(speed);

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
    public static class Provider implements ParticleProvider<ECFParticleData> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        public Particle createParticle(@NotNull ECFParticleData pType,
                                       @NotNull ClientLevel pLevel,
                                       double pX,
                                       double pY,
                                       double pZ,
                                       double pXSpeed,
                                       double pYSpeed,
                                       double pZSpeed) {
            ECFParticle particle = new ECFParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed,pType);
            particle.setSpriteFromAge(this.sprite);
            return particle;
        }
    }

}
