package net.sinedkadis.terracompositio.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.sinedkadis.terracompositio.registries.TCBlocks;

import java.util.function.Supplier;


public record C2SBoardSync(int x, short y, int z) {
    public C2SBoardSync(BlockPos target) {
        this(target.getX(), (short) target.getY(), target.getZ());
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
                    BlockState blockState = level.getBlockState(pPos);
                    level.setBlock(pPos,
                            TCBlocks.TECHNETIUM_BOARD.get().defaultBlockState()
                                    .setValue(BlockStateProperties.WATERLOGGED,
                                            blockState.hasProperty(BlockStateProperties.WATERLOGGED)
                                                    && blockState.getValue(BlockStateProperties.WATERLOGGED)),
                            3);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
