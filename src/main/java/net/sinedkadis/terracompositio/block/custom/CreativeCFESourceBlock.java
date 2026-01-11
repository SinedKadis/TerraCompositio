package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import org.jetbrains.annotations.NotNull;

public class CreativeCFESourceBlock extends TCBaseEntityBlock {
    public CreativeCFESourceBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected @NotNull BlockEntityType<? extends TCBlockEntity> getBlockEntityType() {
        return TCBlockEntities.CREATIVE_CFE_SOURCE_BE.get();
    }

}
