package net.sinedkadis.terracompositio.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.sinedkadis.terracompositio.registries.TCParticles;
import org.jetbrains.annotations.NotNull;


public record CFEParticleData(float speed) implements ParticleOptions {
    public static final Codec<CFEParticleData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("speed").forGetter(CFEParticleData::speed)
            ).apply(instance, CFEParticleData::new)
    );

    @SuppressWarnings("deprecation")
    public static final Deserializer<CFEParticleData> DESERIALIZER = new Deserializer<>() {
        @Override
        public @NotNull CFEParticleData fromCommand(@NotNull ParticleType<CFEParticleData> type, @NotNull StringReader reader) {
            return new CFEParticleData(1 / 20f);
        }

        @Override
        public @NotNull CFEParticleData fromNetwork(@NotNull ParticleType<CFEParticleData> type, @NotNull FriendlyByteBuf buf) {
            float speed = buf.readFloat();
            return new CFEParticleData(speed);
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