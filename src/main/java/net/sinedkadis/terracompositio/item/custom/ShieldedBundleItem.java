package net.sinedkadis.terracompositio.item.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.registries.TCItems;
import net.sinedkadis.terracompositio.registries.TCTags;
import net.sinedkadis.terracompositio.util.reflection.BundleItemReflectionHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = TerraCompositio.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ShieldedBundleItem extends BundleItem {

    private static final int BAR_COLOR = Mth.color(49/255F, 111/255F, 125/255F);

    public ShieldedBundleItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        if (getContents(stack).findAny().isPresent()){
            return 1;
        }
        return 64;
    }

    public static Stream<ItemStack> getContents(ItemStack pStack) {
        CompoundTag compoundtag = pStack.getTag();
        if (compoundtag == null) {
            return Stream.empty();
        } else {
            ListTag listtag = compoundtag.getList("Items", 10);
            Stream<Tag> var10000 = listtag.stream();
            Objects.requireNonNull(CompoundTag.class);
            return var10000.map(CompoundTag.class::cast).map(ItemStack::of);
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(@NotNull ItemStack pStack, ItemStack pOther, @NotNull Slot pSlot, @NotNull ClickAction pAction, @NotNull Player pPlayer, @NotNull SlotAccess pAccess) {
        if (pOther.is(TCTags.Items.UNSTABLE_TECHNETIUM) || pOther.isEmpty()) {
            return super.overrideOtherStackedOnMe(pStack, pOther, pSlot, pAction, pPlayer, pAccess);
        }
        return false;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack pStack, @NotNull Slot pSlot, @NotNull ClickAction pAction, @NotNull Player pPlayer) {
        if (!pStack.is(TCTags.Items.UNSTABLE_TECHNETIUM))
            return false;
        return super.overrideStackedOnOther(pStack, pSlot, pAction, pPlayer);
    }

    @Override
    public int getBarColor(@NotNull ItemStack pStack) {
        return BAR_COLOR;
    }

    @SubscribeEvent
    public static void onItemPickUpEvent(PlayerEvent.ItemPickupEvent event) {
        ItemStack stack = event.getStack();
        if (stack.is(TCTags.Items.UNSTABLE_TECHNETIUM)){
            Player player = event.getEntity();
            Inventory inventory = player.getInventory();
            ItemStack newBundle = TCItems.SHIELDED_BUNDLE.get().getDefaultInstance();
            List<ItemStack> bundles = inventory.items.stream().filter(itemStack -> itemStack.is(TCItems.SHIELDED_BUNDLE.get())).toList();
            for (ItemStack bundle : bundles){
                int added;
                if (bundle.getCount() > 1){
                    bundle.shrink(1);
                    added = BundleItemReflectionHelper.addToBundle(newBundle, stack);
                    if (!player.addItem(newBundle)){
                        player.drop(newBundle,false);
                    }
                } else {
                    added = BundleItemReflectionHelper.addToBundle(bundle,stack);
                }
                if (added != 0){
                    int itemSlot = inventory.findSlotMatchingItem(stack);
                    if (itemSlot >= 0){
                        ItemStack item = inventory.getItem(itemSlot);
                        item.shrink(added);
                        if (item.isEmpty()){
                            break;
                        }
                    }
//                    else {
//                        dupe)
//                    }
                }
            }

        }
    }


}
