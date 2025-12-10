package net.sinedkadis.terracompositio.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import net.sinedkadis.terracompositio.api.networks.cfe.CFECapability;
import net.sinedkadis.terracompositio.network.packets.S2CPlayerCfeContainerSync;

@OnlyIn(Dist.CLIENT)
public class ClientPacketHandlers {
    public static void handlePlayerCfeSync(S2CPlayerCfeContainerSync msg) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            player.getCapability(CFECapability.CFE)
                    .orElse(DummyCFEHandler.instance)
                    .setCFE(msg.cfe());
        }
    }
}
