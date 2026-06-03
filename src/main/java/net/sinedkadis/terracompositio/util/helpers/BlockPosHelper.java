package net.sinedkadis.terracompositio.util.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class BlockPosHelper {
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

    public static @NotNull List<BlockPos> getTouchingBlocks(@Nullable Level level, BlockPos pPos, @Nullable Predicate<BlockState> predicate, int iteration) {
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
                for (Direction dir : Direction.values()) {
                    mutablePos.set(current.relative(dir));
                    if (!visited.contains(mutablePos) && (predicate == null || level == null || predicate.test(level.getBlockState(mutablePos)))) {
                        queue.add(mutablePos.immutable());
                        visited.add(mutablePos.immutable());
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

    public static @NotNull List<BlockPos> getNearBlocks(@Nullable Level level, BlockPos pos, @Nullable TagKey<Block> block) {
        if (level != null && !level.isClientSide)
            return getNearBlocks(level, pos, block, 1);
        return getNearBlocks(null, pos, null, 1);
    }

    public static @NotNull List<BlockPos> getTouchingBlocks(@Nullable Level level, BlockPos pos, @Nullable TagKey<Block> block) {
        if (level != null && !level.isClientSide)
            return getTouchingBlocks(level, pos, block, 1);
        return getTouchingBlocks(null, pos, (TagKey<Block>) null, 2);
    }

    public static @NotNull List<BlockPos> getNearBlocks(BlockPos pos, int iteration) {
        return getNearBlocks(null, pos, null, iteration);
    }

    public static @NotNull List<BlockPos> getTouchingBlocks(BlockPos pos, int iteration) {
        return getTouchingBlocks(null, pos, (TagKey<Block>) null, iteration);
    }

    public static @NotNull List<BlockPos> getNearBlocks(BlockPos pos) {
        return getNearBlocks(null, pos, null);
    }

    public static @NotNull List<BlockPos> getTouchingBlocks(BlockPos pos) {
        return getTouchingBlocks(null, pos, null);
    }

    public static CompoundTag saveBlockPos(BlockPos blockPos) {
        CompoundTag tag = new CompoundTag();
        if (blockPos == null) return tag;

        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();

        tag.putInt("x", x);
        tag.putInt("y", y);
        tag.putInt("z", z);
        return tag;
    }

    public static @Nullable BlockPos loadBlockPos(Tag tag) {
        CompoundTag compoundTag;
        if (tag instanceof CompoundTag) {
            compoundTag = ((CompoundTag) tag);
        } else return null;
        if (compoundTag.isEmpty()) return null;
        int x = compoundTag.getInt("x");
        int y = compoundTag.getInt("y");
        int z = compoundTag.getInt("z");
        return new BlockPos(x, y, z);
    }
}
