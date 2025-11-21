package net.sinedkadis.terracompositio.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.sinedkadis.terracompositio.registries.TCBlocks;

import java.util.List;
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

    static String xTag ="last_x";
    static String yTag ="last_y";
    static String zTag ="last_z";
    public static void handle(C2SBoardSync msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide() == LogicalSide.SERVER) {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    CompoundTag persistentData = player.getPersistentData();
                    Level level = player.level();
                    if (persistentData.contains(xTag)) {
                        BlockPos oldPos = new BlockPos(
                                persistentData.getInt(xTag),
                                persistentData.getInt(yTag),
                                persistentData.getInt(zTag));
                        List<Entity> entities = level.getEntities(null, new AABB(oldPos.above(), oldPos.above(2)));
                        if (entities.isEmpty()) {
                            BlockState replaceState =
                                     Blocks.AIR.defaultBlockState();
                            level.setBlockAndUpdate(oldPos, replaceState);
                        }
                    }


                    BlockPos pPos = new BlockPos(msg.x, msg.y, msg.z);

                    level.setBlock(pPos, TCBlocks.TECHNETIUM_BOARD.get().defaultBlockState(), 3);

                    persistentData.putInt(xTag,msg.x);
                    persistentData.putInt(yTag,msg.y);
                    persistentData.putInt(zTag,msg.z);

                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
