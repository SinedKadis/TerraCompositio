package net.sinedkadis.terracompositio.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.sinedkadis.terracompositio.block.custom.FlowCedarCasingBlock;
import net.sinedkadis.terracompositio.block.entity.MatterInfuserPortBlockEntity;

import java.util.function.Supplier;

public class SyncItemHandlerPacket {
    private final BlockPos pos;
    private final ItemStack itemStack;

    public SyncItemHandlerPacket(BlockPos pos, ItemStack itemStack) {
        this.pos = pos;
        this.itemStack = itemStack;
    }

    public static void encode(SyncItemHandlerPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeItemStack(packet.itemStack, false);
    }

    public static SyncItemHandlerPacket decode(FriendlyByteBuf buffer) {
        return new SyncItemHandlerPacket(buffer.readBlockPos(), buffer.readItem());
    }

    public static void handle(SyncItemHandlerPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                BlockPos blockPos = packet.pos.relative(FlowCedarCasingBlock.getDirectionByFunctionSide(level.getBlockState(packet.pos)));
                BlockEntity blockEntity1 = level.getBlockEntity(blockPos);
                if (blockEntity1 instanceof MatterInfuserPortBlockEntity blockEntity) {
                    blockEntity.setClientItem(packet.itemStack);
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}