package net.sinedkadis.terracompositio.item.custom;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

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
}
