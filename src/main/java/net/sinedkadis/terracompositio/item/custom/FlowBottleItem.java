package net.sinedkadis.terracompositio.item.custom;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.sinedkadis.terracompositio.particle.CFEParticleData;
import net.sinedkadis.terracompositio.registries.TCArmorMaterials;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.registries.TCEffects;
import net.sinedkadis.terracompositio.registries.TCItems;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

import static net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL;
import static net.sinedkadis.terracompositio.item.custom.CedarArmorItem.setOldDamage;

public class FlowBottleItem extends Item {

    public FlowBottleItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack pStack) {
        return UseAnim.DRINK;
    }

    @Override
    @ParametersAreNonnullByDefault
    public @NotNull ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving) {
        Player player = pEntityLiving instanceof Player ? (Player)pEntityLiving : null;
        if (player instanceof ServerPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)player, pStack);
        }
        if (player != null
                && hasFlowCedarArmorOn(player)){

            float bootsDamagePercentage = (float) getDamage(player.getInventory().getArmor(0)) / player.getInventory().getArmor(0).getMaxDamage();
            float leggingsDamagePercentage = (float) getDamage(player.getInventory().getArmor(1)) / player.getInventory().getArmor(1).getMaxDamage();
            float chestplateDamagePercentage = (float) getDamage(player.getInventory().getArmor(2)) / player.getInventory().getArmor(2).getMaxDamage();
            float helmetDamagePercentage = (float) getDamage(player.getInventory().getArmor(3)) / player.getInventory().getArmor(3).getMaxDamage();


            ItemStack boots = TCItems.FLOWING_FLOW_CEDAR_BOOTS.get().getDefaultInstance();
            boots.setTag(player.getInventory().getArmor(0).getTag());
            setOldDamage(boots, bootsDamagePercentage);
            boots.setDamageValue((int) (bootsDamagePercentage * boots.getMaxDamage()));

            ItemStack leggings = TCItems.FLOWING_FLOW_CEDAR_LEGGINGS.get().getDefaultInstance();
            leggings.setTag(player.getInventory().getArmor(1).getTag());
            setOldDamage(leggings, bootsDamagePercentage);
            leggings.setDamageValue((int) (leggingsDamagePercentage * leggings.getMaxDamage()));

            ItemStack chestplate = TCItems.FLOWING_FLOW_CEDAR_CHESTPLATE.get().getDefaultInstance();
            chestplate.setTag(player.getInventory().getArmor(2).getTag());
            setOldDamage(chestplate, chestplateDamagePercentage);
            chestplate.setDamageValue((int) (chestplateDamagePercentage * chestplate.getMaxDamage()));

            ItemStack helmet = TCItems.FLOWING_FLOW_CEDAR_HELMET.get().getDefaultInstance();
            helmet.setTag(player.getInventory().getArmor(3).getTag());
            setOldDamage(helmet, helmetDamagePercentage);
            helmet.setDamageValue((int) (helmetDamagePercentage * helmet.getMaxDamage()));

            player.setItemSlot(EquipmentSlot.FEET,boots);
            player.setItemSlot(EquipmentSlot.LEGS,leggings);
            player.setItemSlot(EquipmentSlot.CHEST,chestplate);
            player.setItemSlot(EquipmentSlot.HEAD,helmet);
        } else {
            pEntityLiving.gameEvent(GameEvent.DRINK);
            pEntityLiving.addEffect(new MobEffectInstance(TCEffects.FLOW_SATURATION.get(),200));
        }

        if (player != null) {
            player.awardStat(Stats.ITEM_USED.get(this));
            if (!player.getAbilities().instabuild) {
                pStack.shrink(1);
            }
        }

        if (player == null || !player.getAbilities().instabuild) {
            if (pStack.isEmpty()) {
                return new ItemStack(Items.GLASS_BOTTLE);
            }

            if (player != null) {
                player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
            }
        }
        return pStack;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext pContext) {
        ItemStack itemStack = pContext.getItemInHand();
        BlockState blockState = pContext.getLevel().getBlockState(pContext.getClickedPos());
        Player player = pContext.getPlayer();
        if (player != null && blockState.hasProperty(LEVEL)){
            int levelValue = blockState.getValue(LEVEL);
            if (itemStack.getCount()==1){
                if (levelValue !=3) {
                    pContext.getLevel().setBlock(pContext.getClickedPos(),blockState.setValue(LEVEL, levelValue + 1),1);
                    player.setItemInHand(pContext.getHand(),new ItemStack(Items.GLASS_BOTTLE));
                    player.playSound(SoundEvents.BOTTLE_EMPTY);
                    return InteractionResult.SUCCESS;
                }
            }else {
                if (levelValue !=3) {
                    pContext.getLevel().setBlock(pContext.getClickedPos(),blockState.setValue(LEVEL, levelValue + 1),1);
                    if (!player.addItem(new ItemStack(Items.GLASS_BOTTLE))){
                        player.drop(new ItemStack(Items.GLASS_BOTTLE),false);
                    }
                    player.playSound(SoundEvents.BOTTLE_EMPTY);
                    return InteractionResult.SUCCESS;
                }
            }

        }else if (player != null && blockState == Blocks.CAULDRON.defaultBlockState()){
            pContext.getLevel().setBlock(pContext.getClickedPos(), TCBlocks.FLOW_CAULDRON.get().defaultBlockState().setValue(LEVEL,1),1);
            if (itemStack.getCount()==1){
                player.setItemInHand(pContext.getHand(),new ItemStack(Items.GLASS_BOTTLE));
            }else {
                if (!player.addItem(new ItemStack(Items.GLASS_BOTTLE))){
                    player.drop(new ItemStack(Items.GLASS_BOTTLE),false);
                }
            }
            player.playSound(SoundEvents.BOTTLE_EMPTY);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    @ParametersAreNonnullByDefault
    public @NotNull InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        return ItemUtils.startUsingInstantly(pLevel, pPlayer, pHand);
    }

    public static void onClientLivingTickEvent(LivingEvent.LivingTickEvent event, ClientLevel clientLevel) {
        LivingEntity livingEntity = event.getEntity();
        RandomSource random = clientLevel.getRandom();
        Vec3 pos = livingEntity.position().add(
                0,
                Mth.lerp(random.nextFloat(), 0, livingEntity.getBbHeight()),
                0
        );
        if (livingEntity.hasEffect(TCEffects.FLOW_SATURATION.get())) {

            clientLevel.addParticle(new CFEParticleData(1 / 20f),
                    pos.x,
                    pos.y,
                    pos.z,
                    random.nextFloat(),
                    random.nextFloat(),
                    random.nextFloat());
        }
    }

    private boolean hasFlowCedarArmorOn(Player player) {
        for (ItemStack armorStack : player.getInventory().armor) {
            if(!(armorStack.getItem() instanceof ArmorItem)) {
                return false;
            }
        }

        ArmorItem boots = ((ArmorItem)player.getInventory().getArmor(0).getItem());
        ArmorItem leggings = ((ArmorItem)player.getInventory().getArmor(1).getItem());
        ArmorItem breastplate = ((ArmorItem)player.getInventory().getArmor(2).getItem());
        ArmorItem helmet = ((ArmorItem)player.getInventory().getArmor(3).getItem());

        return helmet.getMaterial() == TCArmorMaterials.FLOW_CEDAR && breastplate.getMaterial() == TCArmorMaterials.FLOW_CEDAR &&
                leggings.getMaterial() == TCArmorMaterials.FLOW_CEDAR && boots.getMaterial() == TCArmorMaterials.FLOW_CEDAR;
    }

}
