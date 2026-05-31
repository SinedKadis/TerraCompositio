package net.sinedkadis.terracompositio.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.sinedkadis.terracompositio.api.IHaveKnowledge;
import net.sinedkadis.terracompositio.network.TCPackets;
import net.sinedkadis.terracompositio.util.KnowledgeData;
import net.sinedkadis.terracompositio.util.accessors.PlayerKnowledgeAccessor;

import java.util.function.Supplier;

public class C2SRequestKnowledgePacket {

    private final BlockPos pos;

    public C2SRequestKnowledgePacket(BlockPos pos) {
        this.pos = pos;
    }

    // ─── Сериализация ────────────────────────────────────────────

    public static void encode(C2SRequestKnowledgePacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
    }

    public static C2SRequestKnowledgePacket decode(FriendlyByteBuf buf) {
        return new C2SRequestKnowledgePacket(buf.readBlockPos());
    }

    // ─── Обработка на сервере ────────────────────────────────────

    public static void handle(C2SRequestKnowledgePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // Проверяем что у игрока есть знание
            if (!((PlayerKnowledgeAccessor) player).isCreationAcknowledged()) return;

            ServerLevel level = player.serverLevel();
            BlockEntity be = level.getBlockEntity(pkt.pos);
            if (!(be instanceof IHaveKnowledge ihk)) return;

            // Проверяем дистанцию (защита от спама с дальней дистанции)
            if (player.distanceToSqr(
                    pkt.pos.getX() + 0.5,
                    pkt.pos.getY() + 0.5,
                    pkt.pos.getZ() + 0.5) > 64 * 64) return;

            // Собираем данные на сервере
            KnowledgeData data = new KnowledgeData();
            ihk.collectKnowledgeData(data);

            if (data.isEmpty()) return;

            // Отправляем обратно клиенту
            TCPackets.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CKnowledgeDataPacket(pkt.pos, data));
        });
        ctx.get().setPacketHandled(true);
    }
}