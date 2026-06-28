package net.sinedkadis.terracompositio.item.custom;

import com.google.common.base.Suppliers;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.sinedkadis.terracompositio.api.helpers.PlayerHelper;
import net.sinedkadis.terracompositio.compat.patchouli.TCPatchouliCompat;
import net.sinedkadis.terracompositio.registries.TCItems;
import vazkii.patchouli.api.PatchouliAPI;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CreationFlowJournalItem extends Item {

    public static int MAX_DAYS = 5;
    public static String DAY_FLAG = "terracompositio:day_";
    public static Supplier<Boolean> patchouliLoaded = Suppliers.memoize(() -> ModList.get().isLoaded("patchouli"));

    public CreationFlowJournalItem(Properties properties) {
        super(properties);
    }

    public static int getDay(ItemStack stack){
        CompoundTag tag = stack.getTag();
        if (tag != null && stack.hasTag() && tag.contains("day")) {
            return tag.getInt("day");
        }
        return 1;
    }
    public static boolean isInHand(ItemStack stack,@Nullable Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            boolean inMainHand = stack == livingEntity.getMainHandItem();
            boolean inOffHand = stack == livingEntity.getOffhandItem();
            return inMainHand || inOffHand;
        }
        return false;
    }

    public static void setFlags(int day) {
        if (!patchouliLoaded.get()) return;
        for (int i = 1; i <= MAX_DAYS; i++) {
            PatchouliAPI.get().setConfigFlag(DAY_FLAG + i, i <= day);
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack,
                                @org.jetbrains.annotations.Nullable Level pLevel,
                                List<Component> pTooltipComponents,
                                TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents
                .add(Component.translatable("item.terracompositio.creation_flow_journal.tooltip", pStack.getOrCreateTag().getInt("day"))
                        .withStyle(ChatFormatting.GRAY));
    }

    public static boolean isOpen() {
        return ModList.get().isLoaded("patchouli") && Objects.equals(ForgeRegistries.ITEMS.getKey(TCItems.CREATION_FLOW_JOURNAL.get()), PatchouliAPI.get().getOpenBookGui());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);

        boolean hasPatchouli = patchouliLoaded.get();
        if (playerIn instanceof ServerPlayer player && hasPatchouli) {
            //UseItemSuccessTrigger.INSTANCE.trigger(player, stack, player.serverLevel(), player.getX(), player.getY(), player.getZ());
            PatchouliAPI.get().openBookGUI(player, ForgeRegistries.ITEMS.getKey(TCItems.CREATION_FLOW_JOURNAL.get()));
            playerIn.playSound(SoundEvents.BOOK_PAGE_TURN, 1F, (float) (0.7 + Math.random() * 0.4));

        } else if (!hasPatchouli) {
            PlayerHelper.message(playerIn, Component.translatable("item.terracompositio.creation_flow_journal.no_pathchouli"));
        }

        return InteractionResultHolder.sidedSuccess(stack, worldIn.isClientSide());
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
        CompoundTag tag = pEntity.getPersistentData();
        int day = getDay(pStack);
        if (!(tag.getInt("last_hold_days") == day) && pIsSelected) {
            tag.putInt("last_hold_days", day);
            TCPatchouliCompat.reloadBookContents(pStack, pLevel);
        }
    }
}
