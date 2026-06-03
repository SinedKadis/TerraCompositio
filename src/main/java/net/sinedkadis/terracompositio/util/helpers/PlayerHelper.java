package net.sinedkadis.terracompositio.util.helpers;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PlayerHelper {
    public static void addOrDropToPlayer(@NotNull Player pPlayer, ItemStack toAdd) {
        addOrDropToPlayer(pPlayer, toAdd, false);
    }

    public static void addOrDropToPlayer(@NotNull Player pPlayer, ItemStack toAdd, boolean addInCreative) {
        if (addInCreative || !pPlayer.isCreative()) {
            if (!pPlayer.addItem(toAdd)) {
                pPlayer.drop(toAdd, false);
            }
        }
    }

    public static void message(Player pPlayer, Component pMessageComponent) {
        if (pPlayer instanceof ServerPlayer player)
            player.sendSystemMessage(pMessageComponent, true);
    }
}
