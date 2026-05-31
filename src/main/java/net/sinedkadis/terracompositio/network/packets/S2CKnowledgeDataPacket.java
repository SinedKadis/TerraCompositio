package net.sinedkadis.terracompositio.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.sinedkadis.terracompositio.util.KnowledgeData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Сервер → Клиент.
 * Несёт KnowledgeData для конкретной позиции блока.
 * По получении кладёт данные в ClientCache, откуда их читает KnowledgeOverlay.
 */
public class S2CKnowledgeDataPacket {

    private final BlockPos pos;
    private final KnowledgeData data;

    public S2CKnowledgeDataPacket(BlockPos pos, KnowledgeData data) {
        this.pos = pos;
        this.data = data;
    }

    // ─── Сериализация ────────────────────────────────────────────

    public static void encode(S2CKnowledgeDataPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
        pkt.data.toNetwork(buf);
    }

    public static S2CKnowledgeDataPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        KnowledgeData data = KnowledgeData.fromNetwork(buf);
        return new S2CKnowledgeDataPacket(pos, data);
    }

    // ─── Обработка на клиенте ────────────────────────────────────

    public static void handle(S2CKnowledgeDataPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientCache.put(pkt.pos, pkt.data));
        ctx.get().setPacketHandled(true);
    }

    // ─── Клиентский кэш ──────────────────────────────────────────

    /**
     * Хранит последние полученные данные по позиции блока.
     * KnowledgeOverlay читает данные отсюда, а не лезет напрямую в BlockEntity.
     * <p>
     * Кэш намеренно небольшой — оверлей показывает только один блок за раз,
     * старые записи вытесняются через evict() при смене блока.
     */
    public static final class ClientCache {

        private static final Map<BlockPos, KnowledgeData> cache = new ConcurrentHashMap<>();

        /**
         * Положить/обновить запись.
         */
        public static void put(BlockPos pos, KnowledgeData data) {
            cache.put(pos, data);
        }

        /**
         * Получить данные для позиции, или null если ещё не пришли.
         */
        public static KnowledgeData get(BlockPos pos) {
            return cache.get(pos);
        }

        /**
         * Полная очистка (при выходе из мира).
         */
        public static void clear() {
            cache.clear();
        }
    }
}