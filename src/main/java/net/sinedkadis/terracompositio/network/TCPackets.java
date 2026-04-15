package net.sinedkadis.terracompositio.network;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.network.packets.C2SBoardSync;
import net.sinedkadis.terracompositio.network.packets.S2CHighLightNodesSync;
import net.sinedkadis.terracompositio.network.packets.S2CPlayerCfeContainerSync;

import java.util.Optional;

public class TCPackets {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
           TerraCompositio.modLoc("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int id = 0;

    public static void register()  {

        CHANNEL.messageBuilder(S2CPlayerCfeContainerSync.class, id++,NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CPlayerCfeContainerSync::encode)
                .decoder(S2CPlayerCfeContainerSync::decode)
                .consumerMainThread(S2CPlayerCfeContainerSync::handle)
                .add();
        CHANNEL.registerMessage(id++,
                C2SBoardSync.class,
                C2SBoardSync::encode,
                C2SBoardSync::decode,
                C2SBoardSync::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.messageBuilder(S2CHighLightNodesSync.class, id++,NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CHighLightNodesSync::encode)
                .decoder(S2CHighLightNodesSync::decode)
                .consumerMainThread(S2CHighLightNodesSync::handle)
                .add();
    }


}
