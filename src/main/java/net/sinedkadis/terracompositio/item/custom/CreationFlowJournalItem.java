package net.sinedkadis.terracompositio.item.custom;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.sinedkadis.terracompositio.registries.TCItems;
import vazkii.patchouli.api.PatchouliAPI;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CreationFlowJournalItem extends Item {

    public static int MAX_DAYS = 5;
    public static String DAY_FLAG = "terracompositio:day_";

    public CreationFlowJournalItem(Properties properties) {
        super(properties);
    }

    @Override
    public void onCraftedBy(ItemStack pStack, Level pLevel, Player pPlayer) {
        CompoundTag tag = pStack.getOrCreateTag();
        long time = pLevel.getLevelData().getGameTime();
        tag.putLong("tick_crafted",time);
        super.onCraftedBy(pStack, pLevel, pPlayer);
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

    public static boolean isOpen() {
        return Objects.equals(ForgeRegistries.ITEMS.getKey(TCItems.CREATION_FLOW_JOURNAL.get()), PatchouliAPI.get().getOpenBookGui());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);

        if (playerIn instanceof ServerPlayer player) {
            //UseItemSuccessTrigger.INSTANCE.trigger(player, stack, player.serverLevel(), player.getX(), player.getY(), player.getZ());
            setFlags(getDay(stack));
            PatchouliAPI.get().openBookGUI(player, ForgeRegistries.ITEMS.getKey(TCItems.CREATION_FLOW_JOURNAL.get()));
            playerIn.playSound(SoundEvents.BOOK_PAGE_TURN, 1F, (float) (0.7 + Math.random() * 0.4));

        }

        return InteractionResultHolder.sidedSuccess(stack, worldIn.isClientSide());
    }

    private void setFlags(int day) {
        for (int i = 1; i <= MAX_DAYS; i++){
            PatchouliAPI.get().setConfigFlag(DAY_FLAG+i, i <= day);
        }
    }
}
