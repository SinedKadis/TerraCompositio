package net.sinedkadis.terracompositio.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.util.helpers.ParticleHelper;
import net.sinedkadis.terracompositio.util.helpers.WorldHelper;

import java.util.function.Supplier;


public record C2SBoardSync(int x, short y, int z, boolean place, int cfeToTake, boolean waterlogged) {
    public C2SBoardSync(BlockPos target, boolean place) {
        this(target.getX(), (short) target.getY(), target.getZ(), place, 0, false);
    }

    public C2SBoardSync(BlockPos target, boolean place, int cfeToTake, boolean waterlogged) {
        this(target.getX(), (short) target.getY(), target.getZ(), place, cfeToTake, waterlogged);
    }
    public static void encode(C2SBoardSync msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.x);
        buf.writeShort(msg.y);
        buf.writeInt(msg.z);
        buf.writeBoolean(msg.place);
        buf.writeVarInt(msg.cfeToTake);
        buf.writeBoolean(msg.waterlogged);
    }

    public static C2SBoardSync decode(FriendlyByteBuf buf) {
        return new C2SBoardSync(buf.readInt(), buf.readShort(), buf.readInt(), buf.readBoolean(), buf.readVarInt(), buf.readBoolean());
    }


    public static void handle(C2SBoardSync msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide() == LogicalSide.SERVER) {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    Level level = player.level();
                    BlockPos pPos = new BlockPos(msg.x, msg.y, msg.z);
                    WorldHelper.destroyBlockNoUpdate(level, pPos, player);
                    if (msg.place()) {
                        level.setBlock(pPos,
                            TCBlocks.TECHNETIUM_BOARD.get().defaultBlockState()
                                    .setValue(BlockStateProperties.WATERLOGGED,
                                            msg.waterlogged),
                            3);
                        player.getItemBySlot(EquipmentSlot.FEET).getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance)
                                .takeCFE(msg.cfeToTake, false);
                        ParticleHelper.spawnParticlesIn(level, pPos);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
