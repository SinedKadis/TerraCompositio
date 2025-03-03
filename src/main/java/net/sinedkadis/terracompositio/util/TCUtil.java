package net.sinedkadis.terracompositio.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.sinedkadis.terracompositio.particle.ModParticles;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.sinedkadis.terracompositio.block.ModBlockStateProperties.INFUSED;

public class TCUtil {
    public static @NotNull InteractionResult handleInWorldBlockCraft(BlockState oldState, BlockState newState, Level pLevel, BlockPos pPos, ItemStack item, int count) {
        pLevel.setBlock(pPos,copyBlockStates(oldState,newState),3);
        item.shrink(count);
        if (pLevel instanceof ServerLevel level && oldState.hasProperty(INFUSED) && oldState.getValue(INFUSED)){
            level.playSound(null, pPos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS);
            level.sendParticles(ModParticles.FLOW_STILL_PARTICLE.get(),
                    pPos.getX(),
                    pPos.getY(),
                    pPos.getZ(),
                    10,
                    pLevel.getRandom().nextFloat(),
                    pLevel.getRandom().nextFloat(),
                    pLevel.getRandom().nextFloat(),
                    0.5D);
        }
        return InteractionResult.sidedSuccess(pLevel.isClientSide);
    }

    public static BlockState copyBlockStates(BlockState oldState, BlockState newState) {
        for (Property<?> property : oldState.getProperties()) {
            if (newState.hasProperty(property)) {
                newState = copyProperty(oldState, newState, property);
            }
        }
        return newState;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> BlockState copyProperty(BlockState oldState, BlockState newState, Property<?> property) {
        Property<T> typedProperty = (Property<T>) property;
        T value = oldState.getValue(typedProperty);
        T newValue = newState.getValue(typedProperty);
        T newValueDafault = newState.getBlock().defaultBlockState().getValue(typedProperty);
        if (newValue == newValueDafault)
            return newState.setValue(typedProperty, value);
        return newState;
    }

    public static @NotNull List<BlockPos> getNearBlocks(@Nullable Level level, BlockPos pPos, @Nullable TagKey<Block> block, int iteration) {
        List<BlockPos> result = new ArrayList<>();
        if (iteration <= 0) {
            return result;
        }

        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(pPos);
        visited.add(pPos);

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        while (!queue.isEmpty() && iteration > 0) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                BlockPos current = queue.poll();
                result.add(current);
                if (current == null)
                    current = pPos;
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            mutablePos.set(current.getX() + x, current.getY() + y, current.getZ() + z);
                            if (!visited.contains(mutablePos) && (block == null || level == null || level.getBlockState(mutablePos).is(block))) {
                                queue.add(mutablePos.immutable());
                                visited.add(mutablePos.immutable());
                            }
                        }
                    }
                }
            }
            iteration--;
        }

        return result;
    }
    public static @NotNull List<BlockPos> getTouchingBlocks(@Nullable Level level, BlockPos pPos, @Nullable TagKey<Block> block, int iteration) {
        List<BlockPos> result = new ArrayList<>();
        if (iteration <= 0) {
            return result;
        }

        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(pPos);
        visited.add(pPos);

        while (!queue.isEmpty() && iteration > 0) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                BlockPos current = queue.poll();
                result.add(current);
                if (current == null)
                    current = pPos;

                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = current.relative(dir);
                    if (!visited.contains(neighbor) && (block == null || level == null || level.getBlockState(neighbor).is(block))) {
                        queue.add(neighbor);
                        visited.add(neighbor);
                    }
                }
            }
            iteration--;
        }

        return result;
    }

    public static @NotNull List<BlockPos> getNearBlocks(@Nullable Level level, BlockPos pos, @Nullable TagKey<Block> block){
        if (level != null && !level.isClientSide)
            return getNearBlocks(level, pos,block,1);
        return getNearBlocks(null, pos,null,1);
    }
    public static @NotNull List<BlockPos> getTouchingBlocks(@Nullable Level level, BlockPos pos, @Nullable TagKey<Block> block){
        if (level != null && !level.isClientSide)
            return getTouchingBlocks(level, pos,block,1);
        return getTouchingBlocks(null, pos,null,1);
    }
    public static @NotNull List<BlockPos> getNearBlocks(BlockPos pos, int iteration){
        return getNearBlocks(null, pos,null,iteration);
    }
    public static @NotNull List<BlockPos> getTouchingBlocks(BlockPos pos, int iteration){
        return getTouchingBlocks(null, pos,null,iteration);
    }
    public static @NotNull List<BlockPos> getNearBlocks(BlockPos pos){
        return getNearBlocks(null, pos,null);
    }
    public static @NotNull List<BlockPos> getTouchingBlocks(BlockPos pos){
        return getTouchingBlocks(null, pos,null);
    }
}
