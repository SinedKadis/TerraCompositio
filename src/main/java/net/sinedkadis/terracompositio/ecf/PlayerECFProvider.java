package net.sinedkadis.terracompositio.ecf;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.ecf.IECFHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerECFProvider implements net.minecraftforge.common.capabilities.ICapabilityProvider, INBTSerializable<CompoundTag> {

    private ECFHandlerPlayerArmor handler = null;

    public PlayerECFProvider(Player player) {
        this.player = player;
    }

    private final Player player;
    private final LazyOptional<IECFHandler> optional = LazyOptional.of(this::createPlayerECFContainer);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == TCCapabilities.ECF) {
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    private IECFHandler createPlayerECFContainer() {
        if (this.handler == null) {
            this.handler = new ECFHandlerPlayerArmor(new ECFContainer((ECFNetworkMemberEntity) player)
                    .setMaxECF(0) // I haven't thought of a use for this yet
                    .setOffset(vec3 -> vec3.add(0, 1, 0)));
        }

        return this.handler;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createPlayerECFContainer().writeToNBT(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerECFContainer().readFromNBT(nbt);
    }
}
