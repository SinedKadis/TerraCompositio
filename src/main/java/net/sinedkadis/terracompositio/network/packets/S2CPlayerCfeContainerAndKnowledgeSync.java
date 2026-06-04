package net.sinedkadis.terracompositio.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.sinedkadis.terracompositio.network.ClientPacketHandlers;

import java.util.function.Supplier;


public record S2CPlayerCfeContainerAndKnowledgeSync(int cfe, boolean knowledge) {
    public static void encode(S2CPlayerCfeContainerAndKnowledgeSync msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.cfe);
        buf.writeBoolean(msg.knowledge());
    }

    public static S2CPlayerCfeContainerAndKnowledgeSync decode(FriendlyByteBuf buf) {
        return new S2CPlayerCfeContainerAndKnowledgeSync(buf.readInt(), buf.readBoolean());
    }


    public static void handle(S2CPlayerCfeContainerAndKnowledgeSync msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide() == LogicalSide.CLIENT) {
                ClientPacketHandlers.handlePlayerCfeSync(msg);
            }
        });
        ctx.get().setPacketHandled(true);
    }


}
