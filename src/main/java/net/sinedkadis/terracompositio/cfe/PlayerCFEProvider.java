package net.sinedkadis.terracompositio.cfe;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerCFEProvider implements net.minecraftforge.common.capabilities.ICapabilityProvider, INBTSerializable<CompoundTag> {

    private CFEHandlerProxy handler = null;

    public PlayerCFEProvider(Player player) {
        this.player = player;
    }

    private final Player player;
    private final LazyOptional<ICFEHandler> optional = LazyOptional.of(this::createPlayerCFEContainer);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == TCCapabilities.CFE) {
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    private ICFEHandler createPlayerCFEContainer() {
        if (this.handler == null) {
            this.handler = new CFEHandlerProxy();
            handler.getHandlerList().add(
                    new CFEContainer((CFENetworkMemberEntity) player)
                            .setMaxCFE(10)
                            .setOffset(vec3 -> vec3.add(0, 1, 0)));
        }

        return this.handler;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createPlayerCFEContainer().writeToNBT(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerCFEContainer().readFromNBT(nbt);
    }
}
