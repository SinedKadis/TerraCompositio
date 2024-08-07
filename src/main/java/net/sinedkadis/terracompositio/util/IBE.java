package net.sinedkadis.terracompositio.util;


import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IBE<T extends BlockEntity> extends EntityBlock {

    Class<T> getBlockEntityClass();


    default void withBlockEntityDo(BlockGetter world, BlockPos pos, Consumer<T> action) {
        getBlockEntityOptional(world, pos).ifPresent(action);
    }

    default InteractionResult onBlockEntityUse(BlockGetter world, BlockPos pos, Function<T, InteractionResult> action) {
        return getBlockEntityOptional(world, pos).map(action)
                .orElse(InteractionResult.PASS);
    }
    default Optional<T> getBlockEntityOptional(BlockGetter world, BlockPos pos) {
        return Optional.ofNullable(getBlockEntity(world, pos));
    }


    @Nullable
    @SuppressWarnings("unchecked")
    default T getBlockEntity(BlockGetter worldIn, BlockPos pos) {
        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        Class<T> expectedClass = getBlockEntityClass();

        if (blockEntity == null)
            return null;
        if (!expectedClass.isInstance(blockEntity))
            return null;

        return (T) blockEntity;
    }

}
