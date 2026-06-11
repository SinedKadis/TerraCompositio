package net.sinedkadis.terracompositio.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.sinedkadis.terracompositio.network.ClientPacketHandlers;

import java.util.function.Supplier;


public record S2CAddPlayerKnowledge() {
    public static void encode(S2CAddPlayerKnowledge ignoredMsg, FriendlyByteBuf ignoredBuf) {

    }

    public static S2CAddPlayerKnowledge decode(FriendlyByteBuf ignoredBuf) {
        return new S2CAddPlayerKnowledge();
    }


    public static void handle(S2CAddPlayerKnowledge ignoredMsg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide() == LogicalSide.CLIENT) {
                ClientPacketHandlers.handleAddPlayerKnowledge();
            }
        });
        ctx.get().setPacketHandled(true);
    }


}
