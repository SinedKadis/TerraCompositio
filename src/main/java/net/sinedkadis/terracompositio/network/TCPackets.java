package net.sinedkadis.terracompositio.network;

import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.network.packets.S2CPlayerCfeContainerSync;

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
        CHANNEL.registerMessage(id++, S2CPlayerCfeContainerSync.class,
                S2CPlayerCfeContainerSync::encode,
                S2CPlayerCfeContainerSync::decode,
                S2CPlayerCfeContainerSync::handle);
    }
}
