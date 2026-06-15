package net.sinedkadis.terracompositio.item.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.util.accessors.PlayerKnowledgeAccessor;

public class KnowledgeAppleItem extends Item {
    public KnowledgeAppleItem(Properties pProperties) {
        super(pProperties);
    }

    public static void onPlayerClonedEvent(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        original.reviveCaps();
        Player newPlayer = event.getEntity();
        original.getCapability(TCCapabilities.CFE).ifPresent(oldStore ->
                newPlayer.getCapability(TCCapabilities.CFE).ifPresent(newStore -> {
                    CompoundTag tag = new CompoundTag();
                    oldStore.writeToNBT(tag);
                    newStore.readFromNBT(tag);
                })
        );
        original.invalidateCaps();
        ((PlayerKnowledgeAccessor) newPlayer).setCreationKnowledge(((PlayerKnowledgeAccessor) original).isCreationAcknowledged());
    }
}
