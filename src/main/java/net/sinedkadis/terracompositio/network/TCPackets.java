package net.sinedkadis.terracompositio.network;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.network.packets.*;

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

        CHANNEL.messageBuilder(S2CPlayerCfeContainerAndKnowledgeSync.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CPlayerCfeContainerAndKnowledgeSync::encode)
                .decoder(S2CPlayerCfeContainerAndKnowledgeSync::decode)
                .consumerMainThread(S2CPlayerCfeContainerAndKnowledgeSync::handle)
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
        CHANNEL.registerMessage(
                id++,
                C2SRequestBlockKnowledgePacket.class,
                C2SRequestBlockKnowledgePacket::encode,
                C2SRequestBlockKnowledgePacket::decode,
                C2SRequestBlockKnowledgePacket::handle
        );
        CHANNEL.registerMessage(
                id++,
                C2SRequestEntityKnowledgePacket.class,
                C2SRequestEntityKnowledgePacket::encode,
                C2SRequestEntityKnowledgePacket::decode,
                C2SRequestEntityKnowledgePacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                S2CKnowledgeDataPacket.class,
                S2CKnowledgeDataPacket::encode,
                S2CKnowledgeDataPacket::decode,
                S2CKnowledgeDataPacket::handle
        );

    }


}
