package net.sinedkadis.terracompositio.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;


public class S2CKnowledgeDataPacket {

    private static final BlockPos emptyPos = BlockPos.ZERO.atY(-64);
    private static final UUID emptyUUID = UUID.randomUUID();
    private final BlockPos pos;
    private final CompoundTag data;
    private final UUID entityUUID;

    public S2CKnowledgeDataPacket(BlockPos pos, CompoundTag data) {
        this.entityUUID = emptyUUID;
        this.pos = pos;
        this.data = data;
    }

    public S2CKnowledgeDataPacket(UUID entityUUID, CompoundTag data) {
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
        buf.writeNbt(pkt.data);
    }

    public static S2CKnowledgeDataPacket decode(FriendlyByteBuf buf) {
        boolean posWasSent = buf.readBoolean();
        BlockPos pos = null;
        UUID entityUUID = null;

        if (posWasSent)
            pos = buf.readBlockPos();
        else
            entityUUID = buf.readUUID();

        CompoundTag data = buf.readNbt();
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

        private static final Map<Object, CompoundTag> cache = new ConcurrentHashMap<>();


        public static void put(BlockPos pos, CompoundTag data) {
            cache.put(pos, data);
        }

        public static void put(UUID entityUUID, CompoundTag data) {
            cache.put(entityUUID, data);
        }


        public static CompoundTag get(Object posOrUUID) {
            return cache.get(posOrUUID);
        }


        public static void clear() {
            cache.clear();
        }
    }
}