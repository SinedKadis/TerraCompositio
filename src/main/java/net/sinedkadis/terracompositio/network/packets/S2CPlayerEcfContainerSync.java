package net.sinedkadis.terracompositio.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.sinedkadis.terracompositio.network.ClientPacketHandlers;

import java.util.function.Supplier;


public record S2CPlayerEcfContainerSync(int ecf) {
    public static void encode(S2CPlayerEcfContainerSync msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.ecf);
    }

    public static S2CPlayerEcfContainerSync decode(FriendlyByteBuf buf) {
        return new S2CPlayerEcfContainerSync(buf.readVarInt());
    }


    public static void handle(S2CPlayerEcfContainerSync msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide() == LogicalSide.CLIENT) {
                ClientPacketHandlers.handlePlayerEcfSync(msg);
            }
        });
        ctx.get().setPacketHandled(true);
    }


}
