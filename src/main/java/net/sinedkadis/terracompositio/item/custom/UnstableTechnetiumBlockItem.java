package net.sinedkadis.terracompositio.item.custom;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class UnstableTechnetiumBlockItem extends BlockItem {
    private final int radiation;

    public UnstableTechnetiumBlockItem(Block block, Properties pProperties, int radiation) {
        super(block,pProperties);
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
