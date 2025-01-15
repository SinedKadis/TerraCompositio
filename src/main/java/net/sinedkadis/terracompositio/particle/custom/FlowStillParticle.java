package net.sinedkadis.terracompositio.particle.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class FlowStillParticle extends RisingParticle{
    private boolean stoppedByCollision;

    FlowStillParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        this.quadSize = 0.05F;
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public void move(double pX, double pY, double pZ) {
        if (!this.stoppedByCollision) {
            double d3 = 0;
            double d2 = 0;
            double d1 = 0;
            if ((pX != 0.0 || pY != 0.0 || pZ != 0.0) && pX * pX + pY * pY + pZ * pZ < Mth.square(100.0)) {
                Vec3 vec3 = Entity.collideBoundingBox((Entity) null, new Vec3(pX, pY, pZ), this.getBoundingBox(), this.level, List.of());
                d1 = vec3.x;
                d2 = vec3.y;
                d3 = vec3.z;
            }
            if (d1 != 0.0 || d2 != 0.0 || d3 != 0.0) {
                this.setBoundingBox(this.getBoundingBox().move(pX, pY, pZ));
                this.setLocationFromBoundingbox();
            }
            if (Math.abs(d1) >= 9.999999747378752E-6 && Math.abs(pY) < 9.999999747378752E-6) {
                this.stoppedByCollision = true;
            }
        }
    }

    public float getQuadSize(float pScaleFactor) {
        return this.quadSize;
    }

    public int getLightColor(float pPartialTick) {
        float $$1 = ((float)this.age + pPartialTick) / (float)this.lifetime;
        $$1 = Mth.clamp($$1, 0.0F, 1.0F);
        int $$2 = super.getLightColor(pPartialTick);
        int $$3 = $$2 & 255;
        int $$4 = $$2 >> 16 & 255;
        $$3 += (int)($$1 * 15.0F * 16.0F);
        if ($$3 > 240) {
            $$3 = 240;
        }

        return $$3 | $$4 << 16;
    }

    @OnlyIn(Dist.CLIENT)
    public static class SmallFlowStillProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public SmallFlowStillProvider(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            FlowStillParticle $$8 = new FlowStillParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
            $$8.pickSprite(this.sprite);
            $$8.scale(0.5F);
            return $$8;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            FlowStillParticle $$8 = new FlowStillParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
            $$8.pickSprite(this.sprite);
            return $$8;
        }
    }

}
