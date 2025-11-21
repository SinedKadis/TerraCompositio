package net.sinedkadis.terracompositio.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.sinedkadis.terracompositio.registries.TCBlocks;

import java.util.function.Supplier;


public record C2SBoardSync(int x, short y, int z) {
    public C2SBoardSync(BlockPos blockPos) {
        this(blockPos.getX(), (short) blockPos.getY(), blockPos.getZ());
    }
    public static void encode(C2SBoardSync msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.x);
        buf.writeShort(msg.y);
        buf.writeInt(msg.z);
    }

    public static C2SBoardSync decode(FriendlyByteBuf buf) {
        return new C2SBoardSync(buf.readInt(), buf.readShort(), buf.readInt());
    }


    public static void handle(C2SBoardSync msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide() == LogicalSide.SERVER) {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    Level level = player.level();
                    BlockPos pPos = new BlockPos(msg.x, msg.y, msg.z);

                    level.setBlock(pPos, TCBlocks.TECHNETIUM_BOARD.get().defaultBlockState(), 3);
                    level.scheduleTick(pPos,TCBlocks.TECHNETIUM_BOARD.get(),20);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
