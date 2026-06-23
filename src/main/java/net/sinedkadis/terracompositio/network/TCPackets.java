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

        CHANNEL.registerMessage(id++,
                S2CPlayerEcfContainerSync.class,
                S2CPlayerEcfContainerSync::encode,
                S2CPlayerEcfContainerSync::decode,
                S2CPlayerEcfContainerSync::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(id++,
                C2SBoardSync.class,
                C2SBoardSync::encode,
                C2SBoardSync::decode,
                C2SBoardSync::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        CHANNEL.registerMessage(id++,
                S2CHighLightNodesSync.class,
                S2CHighLightNodesSync::encode,
                S2CHighLightNodesSync::decode,
                S2CHighLightNodesSync::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                id++,
                C2SRequestBlockKnowledgePacket.class,
                C2SRequestBlockKnowledgePacket::encode,
                C2SRequestBlockKnowledgePacket::decode,
                C2SRequestBlockKnowledgePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        CHANNEL.registerMessage(
                id++,
                C2SRequestEntityKnowledgePacket.class,
                C2SRequestEntityKnowledgePacket::encode,
                C2SRequestEntityKnowledgePacket::decode,
                C2SRequestEntityKnowledgePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        CHANNEL.registerMessage(
                id++,
                S2CKnowledgeDataPacket.class,
                S2CKnowledgeDataPacket::encode,
                S2CKnowledgeDataPacket::decode,
                S2CKnowledgeDataPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                id++,
                S2CAddPlayerKnowledge.class,
                S2CAddPlayerKnowledge::encode,
                S2CAddPlayerKnowledge::decode,
                S2CAddPlayerKnowledge::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

    }


}
