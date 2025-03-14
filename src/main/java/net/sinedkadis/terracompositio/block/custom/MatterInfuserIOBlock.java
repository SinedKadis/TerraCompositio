package net.sinedkadis.terracompositio.block.custom;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MatterInfuserIOBlock extends ModIOBaseEntityBlock{
    public MatterInfuserIOBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    @ParametersAreNotNullByDefault
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return null;
    }
}
