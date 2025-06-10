package net.sinedkadis.terracompositio.item.custom;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.registries.TCTags;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = TerraCompositio.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class UnstableTechnetiumItem extends Item {
    private final int radiation;

    public UnstableTechnetiumItem(Properties pProperties, int radiation) {
        super(pProperties);
        this.radiation = radiation;
    }

    @Override
    public void inventoryTick(@NotNull ItemStack pStack, @NotNull Level pLevel, @NotNull Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);

        if (pEntity instanceof LivingEntity entity){
            entity.addEffect(new MobEffectInstance(MobEffects.WITHER,200,radiation,false,false));
        }

    }


    @SubscribeEvent
    public static void onPlayerTickEvent(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        Level level = player.level();
        Inventory inventory= player.getInventory();
        List<ItemStack> containers = inventory.items.stream().filter(itemStack -> itemStack.is(Items.BUNDLE) || itemStack.is(Items.SHULKER_BOX)).toList();
        for (ItemStack container : containers){
            Stream<ItemStack> stream;
            if (container.is(Items.BUNDLE)) {
                stream = ShieldedBundleItem.getContents(container);
            } else {
                stream = TCUtil.getContainerContents(container).stream();
            }
            stream.filter(itemStack -> itemStack.is(TCTags.Items.UNSTABLE_TECHNETIUM))
                    .forEach(itemStack -> itemStack.inventoryTick(level,player,-1,true));
        }
    }
}
