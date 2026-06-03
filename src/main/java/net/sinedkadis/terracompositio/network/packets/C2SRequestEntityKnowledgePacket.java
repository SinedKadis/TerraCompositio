package net.sinedkadis.terracompositio.network.packets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.sinedkadis.terracompositio.api.IHaveKnowledge;
import net.sinedkadis.terracompositio.network.TCPackets;
import net.sinedkadis.terracompositio.util.accessors.PlayerKnowledgeAccessor;

import java.util.UUID;
import java.util.function.Supplier;

public class C2SRequestEntityKnowledgePacket {


    private final UUID entityUUID;

    public C2SRequestEntityKnowledgePacket(UUID entityUUID) {
        this.entityUUID = entityUUID;
    }

    // ─── Сериализация ────────────────────────────────────────────

    public static void encode(C2SRequestEntityKnowledgePacket pkt, FriendlyByteBuf buf) {
        buf.writeUUID(pkt.entityUUID);
    }

    public static C2SRequestEntityKnowledgePacket decode(FriendlyByteBuf buf) {
        return new C2SRequestEntityKnowledgePacket(buf.readUUID());
    }

    // ─── Обработка на сервере ────────────────────────────────────

    public static void handle(C2SRequestEntityKnowledgePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (!((PlayerKnowledgeAccessor) player).isCreationAcknowledged()) return;

            ServerLevel level = player.serverLevel();
            Entity entity = level.getEntity(pkt.entityUUID);
            if (!(entity instanceof IHaveKnowledge ihk)) return;


            if (player.distanceToSqr(
                    entity.position().x() + 0.5,
                    entity.position().y() + 0.5,
                    entity.position().z() + 0.5) > 64 * 64) return;

            CompoundTag data = new CompoundTag();
            ihk.collectKnowledgeData(data);

            if (data.isEmpty()) return;

            TCPackets.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CKnowledgeDataPacket(pkt.entityUUID, data));
        });
        ctx.get().setPacketHandled(true);
    }
}