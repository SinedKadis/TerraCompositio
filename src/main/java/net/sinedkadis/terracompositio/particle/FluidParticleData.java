package net.sinedkadis.terracompositio.particle;

import com.mojang.brigadier.StringReader;
import lombok.Getter;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

@Getter
public class FluidParticleData implements ParticleOptions {
    public static final ParticleOptions.Deserializer<FluidParticleData> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public @NotNull FluidParticleData fromCommand(@NotNull ParticleType<FluidParticleData> type, @NotNull StringReader reader) {
            return new FluidParticleData(type, new FluidStack(Fluids.WATER,1000));
        }

        @Override
        public @NotNull FluidParticleData fromNetwork(@NotNull ParticleType<FluidParticleData> type, @NotNull FriendlyByteBuf buf) {
            FluidStack fluid = FluidStack.readFromPacket(buf);
            return new FluidParticleData(type, fluid);
        }
    };

    @NotNull
    private final ParticleType<FluidParticleData> type;
    private final FluidStack fluidStack;

    public FluidParticleData(@NotNull ParticleType<FluidParticleData> type, FluidStack fluidStack) {
        this.type = type;
        this.fluidStack = fluidStack;
    }

    @Override
    public void writeToNetwork(@NotNull FriendlyByteBuf buf) {
        fluidStack.writeToPacket(buf);
    }

    @Override
    public @NotNull String writeToString() {
        ResourceLocation fluidName = ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid());
        return fluidName != null ? fluidName.toString() : "empty";
    }
}