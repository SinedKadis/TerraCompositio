package net.sinedkadis.terracompositio.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.network.ClientPacketHandlers;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public record S2CHighLightNodesSync(BlockPos blockPos,BlockPos outputPos, BlockPos receiverPos, Set<BlockPos> senderPoses, Set<BlockPos> inputPoses) {
    public S2CHighLightNodesSync(PathPointerBlockEntity ppbe) {
        this(ppbe.getBlockPos(),ppbe.getOutputPos(),ppbe.getReceiverPos(),ppbe.getSenderPoses(),ppbe.getInputPoses());
    }

    public static void encode(S2CHighLightNodesSync msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.blockPos.getX());
        buf.writeInt(msg.blockPos.getY());
        buf.writeInt(msg.blockPos.getZ());

        if (msg.outputPos == null) {
            buf.writeInt(0);
            buf.writeInt(-64);
            buf.writeInt(0);
        } else {
            buf.writeInt(msg.outputPos.getX());
            buf.writeInt(msg.outputPos.getY());
            buf.writeInt(msg.outputPos.getZ());
        }

        if (msg.receiverPos == null) {
            buf.writeInt(0);
            buf.writeInt(-64);
            buf.writeInt(0);
        } else {
            buf.writeInt(msg.receiverPos.getX());
            buf.writeInt(msg.receiverPos.getY());
            buf.writeInt(msg.receiverPos.getZ());
        }

        buf.writeInt(msg.senderPoses.size());
        msg.senderPoses.forEach(senderPos -> {
            buf.writeInt(senderPos.getX());
            buf.writeInt(senderPos.getY());
            buf.writeInt(senderPos.getZ());
        });
        buf.writeInt(msg.inputPoses.size());
        msg.inputPoses.forEach(inputPos -> {
            buf.writeInt(inputPos.getX());
            buf.writeInt(inputPos.getY());
            buf.writeInt(inputPos.getZ());
        });
    }

    public static S2CHighLightNodesSync decode(FriendlyByteBuf buf) {
        int x0 = buf.readInt();
        int y0 = buf.readInt();
        int z0 = buf.readInt();

        BlockPos blockPos = new BlockPos(x0,y0,z0);

        int x1 = buf.readInt();
        int y1 = buf.readInt();
        int z1 = buf.readInt();

        BlockPos outputPos = null;
        if (y1 != -64)
            outputPos = new BlockPos(x1,y1,z1);

        int x2 = buf.readInt();
        int y2 = buf.readInt();
        int z2 = buf.readInt();

        BlockPos receiverPos = null;
        if (y2 != -64)
            receiverPos = new BlockPos(x2,y2,z2);

        Set<BlockPos> senders = new HashSet<>();
        int i1 = buf.readInt();
        for (int i = 0; i < i1; i++){
            int x = buf.readInt();
            int y = buf.readInt();
            int z = buf.readInt();
            senders.add(new BlockPos(x,y,z));
        }

        Set<BlockPos> inputs = new HashSet<>();
        int i2 = buf.readInt();
        for (int i = 0; i < i2; i++){
            int x = buf.readInt();
            int y = buf.readInt();
            int z = buf.readInt();
            inputs.add(new BlockPos(x,y,z));
        }
        return new S2CHighLightNodesSync(blockPos,outputPos,receiverPos,senders,inputs);
    }


    public static void handle(S2CHighLightNodesSync msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide() == LogicalSide.CLIENT) {
                ClientPacketHandlers.handleHighLightNodesSync(msg);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
