package net.sinedkadis.terracompositio.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record C2SJumpStatePacket(boolean jumping) {

    public static void encode(C2SJumpStatePacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.jumping());
    }

    public static C2SJumpStatePacket decode(FriendlyByteBuf buf) {
        return new C2SJumpStatePacket(buf.readBoolean());
    }

    public static void handle(C2SJumpStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                player.getPersistentData().putBoolean("isJumping", msg.jumping());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
