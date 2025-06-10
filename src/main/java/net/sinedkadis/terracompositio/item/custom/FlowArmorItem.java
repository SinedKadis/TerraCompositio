package net.sinedkadis.terracompositio.item.custom;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.sinedkadis.terracompositio.registries.TCArmorMaterials;
import net.sinedkadis.terracompositio.registries.TCItems;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class FlowArmorItem extends TCArmorItem {
    private float[] damage = new float[4];

    @Override
    public @NotNull Type getType() {
        return type;
    }

    private final Type type;

    public FlowArmorItem(ArmorMaterial pMaterial, Type pType, Properties pProperties) {
        super(pMaterial, pType, pProperties);
        this.type = pType;
    }

    public FlowArmorItem setOldDamage(float[] damage){
        this.damage = damage;
        return this;
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        int currentDurability = stack.getMaxDamage() - stack.getItem().getDamage(stack);
        if (amount >= currentDurability){
            if(entity instanceof ServerPlayer pPlayer) {
                setNonFlowArmorBack(pPlayer,this.type.getSlot().getIndex(),true);
                return 0;
            }
        }
        return amount;
    }

    @Override
    public void inventoryTick(@NotNull ItemStack pStack, @NotNull Level pLevel, @NotNull Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
        if (!pLevel.isClientSide()
                && pEntity instanceof Player pPlayer) {
            if (this.type.getSlot().getIndex() != pSlotId){
                this.setNonFlowArmorBack(pPlayer,pSlotId,false);
            }
            ItemStack boots = pPlayer.getItemBySlot(EquipmentSlot.FEET);
            ItemStack leggings = pPlayer.getItemBySlot(EquipmentSlot.LEGS);
            ItemStack chestplate = pPlayer.getItemBySlot(EquipmentSlot.CHEST);
            ItemStack helmet = pPlayer.getItemBySlot(EquipmentSlot.HEAD);
            if (!hasCorrectArmorOn(TCArmorMaterials.FLOWING_FLOW_CEDAR,pPlayer)) {
                if (boots.getItem() == TCItems.FLOWING_FLOW_CEDAR_BOOTS.get()) {
                    ItemStack stack = new ItemStack(TCItems.FLOW_CEDAR_BOOTS.get());
                    stack.setTag(pPlayer.getItemBySlot(EquipmentSlot.FEET).getTag());
                    stack.setDamageValue((int) (damage[0] * stack.getMaxDamage()));
                    pPlayer.setItemSlot(EquipmentSlot.FEET, stack);
                }
                if (leggings.getItem() == TCItems.FLOWING_FLOW_CEDAR_LEGGINGS.get()) {
                    ItemStack stack = new ItemStack(TCItems.FLOW_CEDAR_LEGGINGS.get());
                    stack.setTag(pPlayer.getItemBySlot(EquipmentSlot.LEGS).getTag());
                    stack.setDamageValue((int) (damage[1] * stack.getMaxDamage()));
                    pPlayer.setItemSlot(EquipmentSlot.LEGS, stack);
                }
                if (chestplate.getItem() == TCItems.FLOWING_FLOW_CEDAR_CHESTPLATE.get()) {
                    ItemStack stack = new ItemStack(TCItems.FLOW_CEDAR_CHESTPLATE.get());
                    stack.setTag(pPlayer.getItemBySlot(EquipmentSlot.CHEST).getTag());
                    stack.setDamageValue((int) (damage[2] * stack.getMaxDamage()));
                    pPlayer.setItemSlot(EquipmentSlot.CHEST, stack);
                }
                if (helmet.getItem() == TCItems.FLOWING_FLOW_CEDAR_HELMET.get()) {
                    ItemStack stack = new ItemStack(TCItems.FLOW_CEDAR_HELMET.get());
                    stack.setTag(pPlayer.getItemBySlot(EquipmentSlot.HEAD).getTag());
                    stack.setDamageValue((int) (damage[3] * stack.getMaxDamage()));
                    pPlayer.setItemSlot(EquipmentSlot.HEAD, stack);
                }
            }
        }
    }

    private void setNonFlowArmorBack(Player pPlayer,int slotID,boolean inArmorSlot){
        if (this.type.getSlot() == EquipmentSlot.FEET){
            ItemStack stack = new ItemStack(TCItems.FLOW_CEDAR_BOOTS.get());
            stack.setTag(pPlayer.getInventory().getItem(slotID).getTag());
            stack.setDamageValue((int) (damage[0] * stack.getMaxDamage()));
            if (inArmorSlot){
                pPlayer.getInventory().armor.set(slotID,stack);
            }else {
                pPlayer.getInventory().setItem(slotID,stack);
            }
        }
        if (this.type.getSlot() == EquipmentSlot.LEGS){
            ItemStack stack = new ItemStack(TCItems.FLOW_CEDAR_LEGGINGS.get());
            stack.setTag(pPlayer.getInventory().getItem(slotID).getTag());
            stack.setDamageValue((int) (damage[1] * stack.getMaxDamage()));
            if (inArmorSlot){
                pPlayer.getInventory().armor.set(slotID,stack);
            }else {
                pPlayer.getInventory().setItem(slotID,stack);
            }
        }
        if (this.type.getSlot() == EquipmentSlot.CHEST){
            ItemStack stack = new ItemStack(TCItems.FLOW_CEDAR_CHESTPLATE.get());
            stack.setTag(pPlayer.getInventory().getItem(slotID).getTag());
            stack.setDamageValue((int) (damage[2] * stack.getMaxDamage()));
            if (inArmorSlot){
                pPlayer.getInventory().armor.set(slotID,stack);
            }else {
                pPlayer.getInventory().setItem(slotID,stack);
            }
        }
        if (this.type.getSlot() == EquipmentSlot.HEAD){
            ItemStack stack = new ItemStack(TCItems.FLOW_CEDAR_HELMET.get());
            stack.setTag(pPlayer.getInventory().getItem(slotID).getTag());
            stack.setDamageValue((int) (damage[3] * stack.getMaxDamage()));
            if (inArmorSlot){
                pPlayer.getInventory().armor.set(slotID,stack);
            }else {
                pPlayer.getInventory().setItem(slotID,stack);
            }
        }
    }
 /*
    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        ItemStack stack = new ItemStack(ModItems.NONFLOW_WOOD_HELMET.get());
        stack.setTag(item.getTag());
        stack.setDamageValue((int) (damage[3] * stack.getMaxDamage()));
        player.drop(stack,true);
        return true;
    }

 */
}
