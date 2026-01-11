package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import org.jetbrains.annotations.NotNull;

public class CFETrashCanBlock extends TCBaseEntityBlock {
    public CFETrashCanBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected @NotNull BlockEntityType<? extends TCBlockEntity> getBlockEntityType() {
        return TCBlockEntities.CFE_TRASH_CAN_BE.get();
    }

}
