package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import org.jetbrains.annotations.NotNull;

public class CreativeECFSourceBlock extends TCBaseEntityBlock {
    public CreativeECFSourceBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    @NotNull
    public BlockEntityType<? extends TCBlockEntity> getBlockEntityType() {
        return TCBlockEntities.CREATIVE_ECF_SOURCE_BE.get();
    }

}
