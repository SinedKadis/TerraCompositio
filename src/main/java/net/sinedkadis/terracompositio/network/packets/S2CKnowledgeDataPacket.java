package net.sinedkadis.terracompositio.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.sinedkadis.terracompositio.util.KnowledgeData;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;


public class S2CKnowledgeDataPacket {

    private static final BlockPos emptyPos = BlockPos.ZERO.atY(-64);
    private static final UUID emptyUUID = UUID.randomUUID();
    private final BlockPos pos;
    private final KnowledgeData data;
    private final UUID entityUUID;

    public S2CKnowledgeDataPacket(BlockPos pos, KnowledgeData data) {
        this.entityUUID = emptyUUID;
        this.pos = pos;
        this.data = data;
    }

    public S2CKnowledgeDataPacket(UUID entityUUID, KnowledgeData data) {
        this.entityUUID = entityUUID;
        this.pos = emptyPos;
        this.data = data;
    }

    // ─── Сериализация ────────────────────────────────────────────

    public static void encode(S2CKnowledgeDataPacket pkt, FriendlyByteBuf buf) {

        BlockPos pos1 = pkt.pos;
        if (!pos1.equals(emptyPos)) {
            buf.writeBoolean(true);
            buf.writeBlockPos(pos1);
        } else {
            buf.writeBoolean(false);
            buf.writeUUID(pkt.entityUUID);
        }
        pkt.data.toNetwork(buf);
    }

    public static S2CKnowledgeDataPacket decode(FriendlyByteBuf buf) {
        boolean posWasSent = buf.readBoolean();
        BlockPos pos = null;
        UUID entityUUID = null;

        if (posWasSent)
            pos = buf.readBlockPos();
        else
            entityUUID = buf.readUUID();

        KnowledgeData data = KnowledgeData.fromNetwork(buf);
        if (posWasSent)
            return new S2CKnowledgeDataPacket(pos, data);
        else
            return new S2CKnowledgeDataPacket(entityUUID, data);
    }

    // ─── Обработка на клиенте ────────────────────────────────────

    public static void handle(S2CKnowledgeDataPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            BlockPos pos1 = pkt.pos;
            if (!pos1.equals(emptyPos))
                ClientCache.put(pos1, pkt.data);
            else {
                UUID entityUUID1 = pkt.entityUUID;
                if (!entityUUID1.equals(emptyUUID)) {
                    ClientCache.put(entityUUID1, pkt.data);
                }
            }
        });
        context.setPacketHandled(true);
    }

    // ─── Клиентский кэш ──────────────────────────────────────────
    public static final class ClientCache {

        private static final Map<Object, KnowledgeData> cache = new ConcurrentHashMap<>();


        public static void put(BlockPos pos, KnowledgeData data) {
            cache.put(pos, data);
        }
        public static void put(UUID entityUUID, KnowledgeData data) {
            cache.put(entityUUID, data);
        }


        public static KnowledgeData get(Object posOrUUID) {
            return cache.get(posOrUUID);
        }


        public static void clear() {
            cache.clear();
        }
    }
}