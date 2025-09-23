package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.entity.custom.FlowCedarEntEntity;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCEntities;
import net.sinedkadis.terracompositio.registries.TCItems;

public class EntStatueBlockEntity extends TCItemIOCFEBlockEntity{
    public EntStatueBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ENT_STATUE_BE.get(), pos, state, BlockMode.NONE);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    public void jojoReference() {
        if (level instanceof ServerLevel) {
            FlowCedarEntEntity pEntity = TCEntities.FLOW_CEDAR_ENT.get().create(level);
            if (pEntity != null) {
                level.addFreshEntity(pEntity);
                pEntity.getInnerCFEOptional().ifPresent(icfeHandler -> icfeHandler.setCFE(30));
                ItemStack crown = this.itemHandler.getStackInSlot(0);
                if (crown.is(TCItems.TECHNETIUM_CROWN.get())) {
                    pEntity.setItemSlot(EquipmentSlot.HEAD,crown.copyAndClear());
                }
                BlockPos blockPos = getBlockPos();
                pEntity.setPos(blockPos.getCenter());
                level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
            }
        }
    }
}
