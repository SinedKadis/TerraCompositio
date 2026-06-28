package net.sinedkadis.terracompositio.api.helpers;

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

/**
 * The Class with cool methods, that my mod use, related to {@link BlockPos}.
 */
public class BlockPosHelper {
    /**
     * Get block poses of blocks, that match tag. Each iteration searches blocks is 3x3x3 cube with already found block in center
     *
     * @param level     the Minecraft World
     * @param pPos      the Starting position of search
     * @param block     the Tag, that used to filter searched blocks
     * @param iteration the Iteration. Each one like "runs" that method for already found block pos
     * @return the List of BlockPoses
     */
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

    /**
     * Get block poses of blocks, that match predicate. Each iteration searches blocks that touching already found block.
     *
     * @param level     the Minecraft World
     * @param pPos      the Starting position of search
     * @param predicate the Predicate, that used to filter searched blocks
     * @param iteration the Iteration. Each one like "runs" that method for already found block pos
     * @return the List of BlockPoses
     */
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

    /**
     * Get block poses of blocks, that match tag. Each iteration searches blocks that touching already found block.
     *
     * @param level     the Minecraft World
     * @param pPos      the Starting position of search
     * @param block     the Tag, that used to filter searched blocks
     * @param iteration the Iteration. Each one like "runs" that method for already found block pos
     * @return the List of BlockPoses
     */
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

    /**
     * Saves block pos to compound tag.
     *
     * @param blockPos the blockPos
     * @return saved blockPos or empty tag if blockPos is null
     */
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

    /**
     * Load block pos from tag.
     *
     * @param tag the tag
     * @return the blockPos or null if tag is not instance of {@link CompoundTag} or empty
     */
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
