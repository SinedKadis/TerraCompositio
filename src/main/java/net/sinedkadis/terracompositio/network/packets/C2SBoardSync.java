package net.sinedkadis.terracompositio.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.sinedkadis.terracompositio.registries.TCBlocks;

import java.util.function.Supplier;


public record C2SBoardSync(int x, short y, int z,int x0, short y0, int z0) {
    public C2SBoardSync(BlockPos target,BlockPos last) {
        this(target.getX(), (short) target.getY(), target.getZ(), last.getX(), (short) last.getY(),last.getZ());
    }
    public static void encode(C2SBoardSync msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.x);
        buf.writeShort(msg.y);
        buf.writeInt(msg.z);
        buf.writeInt(msg.x0);
        buf.writeShort(msg.y0);
        buf.writeInt(msg.z0);
    }

    public static C2SBoardSync decode(FriendlyByteBuf buf) {
        return new C2SBoardSync(buf.readInt(), buf.readShort(), buf.readInt(),buf.readInt(), buf.readShort(), buf.readInt());
    }


    public static void handle(C2SBoardSync msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide() == LogicalSide.SERVER) {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    Level level = player.level();
                    BlockPos pPos = new BlockPos(msg.x, msg.y, msg.z);

                    level.setBlock(pPos, TCBlocks.TECHNETIUM_BOARD.get().defaultBlockState(), 3);
                    level.scheduleTick(pPos,TCBlocks.TECHNETIUM_BOARD.get(),100);

                    BlockPos pPos0 = new BlockPos(msg.x0, msg.y0, msg.z0);
                    BlockState lastBlockState = level.getBlockState(pPos0);
                    if (lastBlockState.is(TCBlocks.TECHNETIUM_BOARD.get())) {
                        level.setBlock(pPos0, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
