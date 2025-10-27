package net.sinedkadis.terracompositio.network;

import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.network.packets.C2SJumpStatePacket;

public class TCPackets {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
           TerraCompositio.modLoc("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, C2SJumpStatePacket.class,
                C2SJumpStatePacket::encode,
                C2SJumpStatePacket::decode,
                C2SJumpStatePacket::handle);
    }
}
