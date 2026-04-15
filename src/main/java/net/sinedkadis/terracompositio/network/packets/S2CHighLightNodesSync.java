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

public record S2CHighLightNodesSync(BlockPos blockPos,BlockPos outputPos, BlockPos receiverPos, Set<BlockPos> senderPoses) {
    public S2CHighLightNodesSync(PathPointerBlockEntity ppbe) {
        this(ppbe.getBlockPos(),ppbe.getOutputPos(),ppbe.getReceiverPos(),ppbe.getSenderPoses());
    }

    public static void encode(S2CHighLightNodesSync msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.blockPos.getX());
        buf.writeInt(msg.blockPos.getY());
        buf.writeInt(msg.blockPos.getZ());

        buf.writeInt(msg.outputPos.getX());
        buf.writeInt(msg.outputPos.getY());
        buf.writeInt(msg.outputPos.getZ());

        buf.writeInt(msg.receiverPos.getX());
        buf.writeInt(msg.receiverPos.getY());
        buf.writeInt(msg.receiverPos.getZ());

        buf.writeInt(msg.senderPoses.size());
        msg.senderPoses.forEach(senderPos -> {
            buf.writeInt(senderPos.getX());
            buf.writeInt(senderPos.getY());
            buf.writeInt(senderPos.getZ());
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

        BlockPos outputPos = new BlockPos(x1,y1,z1);

        int x2 = buf.readInt();
        int y2 = buf.readInt();
        int z2 = buf.readInt();

        BlockPos receiverPos = new BlockPos(x2,y2,z2);

        Set<BlockPos> set = new HashSet<>();
        for (int i = 0;i < buf.readInt();i++){
            int x = buf.readInt();
            int y = buf.readInt();
            int z = buf.readInt();
            set.add(new BlockPos(x,y,z));
        }
        return new S2CHighLightNodesSync(blockPos,outputPos,receiverPos,set);
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
