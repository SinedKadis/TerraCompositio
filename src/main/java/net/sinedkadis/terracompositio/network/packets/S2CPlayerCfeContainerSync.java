package net.sinedkadis.terracompositio.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import net.sinedkadis.terracompositio.api.networks.cfe.CFECapability;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;

import java.util.function.Supplier;

public record S2CPlayerCfeContainerSync(int cfe) {

    public static void encode(S2CPlayerCfeContainerSync msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.cfe);
    }

    public static S2CPlayerCfeContainerSync decode(FriendlyByteBuf buf) {
        return new S2CPlayerCfeContainerSync(buf.readInt());
    }


    public static void handle(S2CPlayerCfeContainerSync msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide() == LogicalSide.CLIENT) {
                clientHandle(msg);
            }
        });
        ctx.get().setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void clientHandle(S2CPlayerCfeContainerSync msg) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ICFEHandler playerHandler = player.getCapability(CFECapability.CFE).orElse(DummyCFEHandler.instance);
            playerHandler.setCFE(msg.cfe());

        }
    }
}
