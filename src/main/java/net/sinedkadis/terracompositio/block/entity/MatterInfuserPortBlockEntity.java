package net.sinedkadis.terracompositio.block.entity;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.registries.ModBlockEntities;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

@Setter
@Getter
public class MatterInfuserPortBlockEntity extends MatterInfuserBaseBlockEntity {

    private ItemStack renderItem = ItemStack.EMPTY;


    public MatterInfuserPortBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.MATTER_INFUSER_PORT_BE.get(),pPos, pBlockState,100,10);
    }
}
