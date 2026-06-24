package net.sinedkadis.terracompositio.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.sinedkadis.terracompositio.registries.TCParticles;
import org.jetbrains.annotations.NotNull;


public record ECFParticleData(float speed) implements ParticleOptions {
    public static final Codec<ECFParticleData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("speed").forGetter(ECFParticleData::speed)
            ).apply(instance, ECFParticleData::new)
    );

    @SuppressWarnings("deprecation")
    public static final Deserializer<ECFParticleData> DESERIALIZER = new Deserializer<>() {
        @Override
        public @NotNull ECFParticleData fromCommand(@NotNull ParticleType<ECFParticleData> type, @NotNull StringReader reader) {
            return new ECFParticleData(1 / 20f);
        }

        @Override
        public @NotNull ECFParticleData fromNetwork(@NotNull ParticleType<ECFParticleData> type, @NotNull FriendlyByteBuf buf) {
            float speed = buf.readFloat();
            return new ECFParticleData(speed);
        }
    };


    @Override
    public @NotNull ParticleType<?> getType() {
        return TCParticles.CFE_PARTICLE.get();
    }

    @Override
    public void writeToNetwork(@NotNull FriendlyByteBuf buf) {
        buf.writeFloat(speed);
    }

    @Override
    public @NotNull String writeToString() {
        return String.valueOf(speed);
    }
}