package net.sinedkadis.terracompositio.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.dummies.DummyECFHandler;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.network.packets.S2CHighLightNodesSync;
import net.sinedkadis.terracompositio.network.packets.S2CPlayerEcfContainerSync;
import net.sinedkadis.terracompositio.util.accessors.PlayerKnowledgeAccessor;

import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class ClientPacketHandlers {
    public static void handlePlayerCfeSync(S2CPlayerEcfContainerSync msg) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            player.getCapability(TCCapabilities.ECF)
                    .orElse(DummyECFHandler.instance)
                    .setCFE(msg.cfe());
        }
    }
    public static void handleAddPlayerKnowledge() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ((PlayerKnowledgeAccessor) player).setCreationKnowledge(true);
        }
    }

    public static void handleHighLightNodesSync(S2CHighLightNodesSync msg) {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            BlockEntity blockEntity = level.getBlockEntity(msg.blockPos());
            if (blockEntity instanceof PathPointerBlockEntity pathPointerBlockEntity) {
                pathPointerBlockEntity.setOutputPos(msg.outputPos());
                pathPointerBlockEntity.setReceiverPos(msg.receiverPos());

                Set<BlockPos> senderPoses = pathPointerBlockEntity.getSenderPoses();
                senderPoses.clear();
                senderPoses.addAll(msg.senderPoses());

                Set<BlockPos> inputPoses = pathPointerBlockEntity.getInputPoses();
                inputPoses.clear();
                inputPoses.addAll(msg.inputPoses());
            }
        }
    }
}
