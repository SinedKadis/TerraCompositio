package net.sinedkadis.terracompositio.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberBE;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.custom.FlowCedarLikeBlock;


import net.sinedkadis.terracompositio.particle.FluidParticleData;
import net.sinedkadis.terracompositio.registries.TCBlockStateProperties;
import net.sinedkadis.terracompositio.registries.TCGameRules;
import net.sinedkadis.terracompositio.registries.TCParticles;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static net.sinedkadis.terracompositio.registries.TCBlockStateProperties.INFUSED;

public class TCUtil {

    public static int tryCFETransfer(CFENetworkMemberBE target,CFENetworkMemberBE source,int maxTransfer){
        ICFEHandler targetHandler = CFENetwork.getCFEHandler(target).orElse(null);
        if (targetHandler != null){
            ICFEHandler sourceHandler = CFENetwork.getCFEHandler(source).orElse(null);
            if (sourceHandler != null){
                int taken = sourceHandler.takeCFE(maxTransfer,true);
                int added = targetHandler.addCFE(taken,source.getBlockPos(),true);
                if (added <= taken){
                    targetHandler.addCFE(added, source.getBlockPos(), false);
                    sourceHandler.takeCFE(added, false);
                    return added;
                }
            }
        }
        return 0;
    }

    public static void sendFluidParticles(ServerLevel level, BlockPos target, BlockPos source,
                                          int particleAmount, FluidStack fluidStack) {
        if (fluidStack.isEmpty() || particleAmount <= 0 || level == null) return;

        Vec3 targetCenter = Vec3.atCenterOf(target);
        Vec3 sourceCenter = Vec3.atCenterOf(source);

        // Создаем данные частицы
        FluidParticleData particleData = new FluidParticleData(TCParticles.FLUID_FLOW.get(), fluidStack);

        for (int i = 0; i < particleAmount; i++) {
            double offsetX = sourceCenter.x + (level.random.nextDouble() - 0.5) * 0.8;
            double offsetY = sourceCenter.y + (level.random.nextDouble() - 0.5) * 0.8;
            double offsetZ = sourceCenter.z + (level.random.nextDouble() - 0.5) * 0.8;

            // Отправляем частицу с данными о жидкости
            level.sendParticles(particleData,
                    offsetX, offsetY, offsetZ,
                    0, // count
                    targetCenter.x - offsetX, // xd (направление к цели)
                    targetCenter.y - offsetY, // yd
                    targetCenter.z - offsetZ,// zd
                    Math.sqrt(TCUtil.distSqr(target,source))); // speed
        }
    }

    public static @NotNull InteractionResult handleInWorldBlockCraft(BlockState oldState, BlockState newState, Level pLevel, BlockPos pPos, ItemStack item, int count, ParticleOptions type, SoundEvent event) {
        pLevel.setBlock(pPos, Blocks.STRUCTURE_VOID.defaultBlockState(),3);
        pLevel.setBlock(pPos,copyBlockStates(oldState,newState),3);
        if (count != 0)
            item.shrink(count);
        if (pLevel instanceof ServerLevel level ){
            level.playSound(null, pPos, event, SoundSource.BLOCKS);
            if (oldState.hasProperty(INFUSED) && oldState.getValue(INFUSED))
                level.sendParticles(type,
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
    public static @NotNull InteractionResult handleInWorldBlockCraft(BlockState oldState, BlockState newState, Level pLevel, BlockPos pPos, ItemStack item, int count) {
        return handleInWorldBlockCraft(oldState,newState,pLevel,pPos,item,count, TCParticles.CFE_PARTICLE.get(),SoundEvents.COPPER_PLACE);
    }


        public static void spawnParticlesIn(Level pLevel, BlockPos targetPos){
        if (pLevel instanceof ServerLevel level)
            level.sendParticles(TCParticles.CFE_PARTICLE.get(),
                targetPos.getX()+pLevel.getRandom().nextFloat(),
                targetPos.getY()+pLevel.getRandom().nextFloat(),
                targetPos.getZ()+pLevel.getRandom().nextFloat(),
                10,
                    targetPos.getX(),
                    targetPos.getY(),
                    targetPos.getZ(),
                0.5D);
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

    public static @NotNull List<BlockPos> getNearBlocks(@Nullable Level level, BlockPos pos, @Nullable TagKey<Block> block){
        if (level != null && !level.isClientSide)
            return getNearBlocks(level, pos,block,1);
        return getNearBlocks(null, pos, null,1);
    }
    public static @NotNull List<BlockPos> getTouchingBlocks(@Nullable Level level, BlockPos pos, @Nullable TagKey<Block> block){
        if (level != null && !level.isClientSide)
            return getTouchingBlocks(level, pos,block,1);
        return getTouchingBlocks(null, pos, (TagKey<Block>) null,1);
    }
    public static @NotNull List<BlockPos> getNearBlocks(BlockPos pos, int iteration){
        return getNearBlocks(null, pos, null,iteration);
    }
    public static @NotNull List<BlockPos> getTouchingBlocks(BlockPos pos, int iteration){
        return getTouchingBlocks(null, pos, (TagKey<Block>) null,iteration);
    }
    public static @NotNull List<BlockPos> getNearBlocks(BlockPos pos){
        return getNearBlocks(null, pos,null);
    }
    public static @NotNull List<BlockPos> getTouchingBlocks(BlockPos pos){
        return getTouchingBlocks(null, pos,null);
    }

    public static void sendCFEParticles(ServerLevel level,Vec3 target, BlockPos source,int particleAmount){
        if (particleAmount <= 0 || level == null) return;

        Vec3 sourceCenter = Vec3.atCenterOf(source);


        for (int i = 0; i < particleAmount; i++) {
            double offsetX = sourceCenter.x + (level.random.nextDouble() - 0.5) * 0.8;
            double offsetY = sourceCenter.y + (level.random.nextDouble() - 0.5) * 0.8;
            double offsetZ = sourceCenter.z + (level.random.nextDouble() - 0.5) * 0.8;

            // Отправляем частицу с данными о жидкости
            level.sendParticles(TCParticles.CFE_PARTICLE.get(),
                    offsetX, offsetY, offsetZ,
                    0, // count
                    target.x - offsetX, // xd (направление к цели)
                    target.y - offsetY, // yd
                    target.z - offsetZ,// zd
                    Math.sqrt(target.distanceToSqr(source.getCenter()))); // speed
        }
    }

//    public static void sendCFEParticles(ServerLevel level, BlockPos target, BlockPos source, int particleAmount){
//        if (particleAmount <= 0 || level == null) return;
//
//        Vec3 targetCenter = Vec3.atCenterOf(target);
//        Vec3 sourceCenter = Vec3.atCenterOf(source);
//
//
//        for (int i = 0; i < particleAmount; i++) {
//            double offsetX = sourceCenter.x + (level.random.nextDouble() - 0.5) * 0.8;
//            double offsetY = sourceCenter.y + (level.random.nextDouble() - 0.5) * 0.8;
//            double offsetZ = sourceCenter.z + (level.random.nextDouble() - 0.5) * 0.8;
//
//            level.sendParticles(ModParticles.CFE_PARTICLE.get(),
//                    offsetX, offsetY, offsetZ,
//                    0,
//                    targetCenter.x - offsetX,
//                    targetCenter.y - offsetY,
//                    targetCenter.z - offsetZ,
//                    Math.sqrt(TCUtil.distSqr(target,source)));
//        }
//
//    }

    public static List<ItemStack> getContainerContents(ItemStack containerStack) {
        if (containerStack.isEmpty()) {
            return List.of();
        }

        CompoundTag stackTag = containerStack.getTag();
        if (stackTag == null) {
            return List.of();
        }

        CompoundTag blockEntityTag = stackTag.getCompound("BlockEntityTag");
        if (blockEntityTag.contains("Items", CompoundTag.TAG_LIST)) {
            return getItemsFromNBT(blockEntityTag.getList("Items", CompoundTag.TAG_COMPOUND));
        }

        if (stackTag.contains("Items", CompoundTag.TAG_LIST)) {
            return getItemsFromNBT(stackTag.getList("Items", CompoundTag.TAG_COMPOUND));
        }

        if (stackTag.contains("Inventory", CompoundTag.TAG_LIST)) {
            return getItemsFromNBT(stackTag.getList("Inventory", CompoundTag.TAG_COMPOUND));
        }

        return List.of();
    }

    private static List<ItemStack> getItemsFromNBT(ListTag itemsTag) {
        List<ItemStack> items = new ArrayList<>(itemsTag.size());

        for (int i = 0; i < itemsTag.size(); i++) {
            CompoundTag itemTag = itemsTag.getCompound(i);
            ItemStack itemStack = ItemStack.of(itemTag);
            if (!itemStack.isEmpty()) {
                items.add(itemStack);
            }
        }

        return items;
    }

    public static boolean onRemoveHandlerBlacklist(BlockState state,Block... blocks){
        return Arrays.stream(blocks).map(block -> !state.is(block)).reduce((aBoolean, aBoolean2) -> aBoolean && aBoolean2).orElse(true);
    }

    public static void flowLeak(BlockState pState, Level pLevel, BlockPos pPos,boolean chained) {
        if ((!pState.hasProperty(FlowCedarLikeBlock.INFUSED) || pState.getValue(FlowCedarLikeBlock.INFUSED))
                && !pLevel.getGameRules().getBoolean(TCGameRules.DISABLE_FLOW_LEAKING)
                && (!pState.hasProperty(TCBlockStateProperties.WAXED) || !pState.getValue(TCBlockStateProperties.WAXED))) {

                BlockPos f_pos;
                BlockPos b_pos;
                if (pState.hasProperty(RotatedPillarBlock.AXIS)) {
                    f_pos = pPos.relative(pState.getValue(RotatedPillarBlock.AXIS), 1);
                    b_pos = pPos.relative(pState.getValue(RotatedPillarBlock.AXIS), -1);
                } else {
                    f_pos = pPos.relative(Direction.Axis.Y, 1);
                    b_pos = pPos.relative(Direction.Axis.Y, -1);
                }
                getNearBlocks(f_pos,chained ? 4 : 2).stream()
                        .filter(pos -> pos != pPos)
                        .filter(pos -> pLevel.getBlockState(pos).hasProperty(FlowCedarLikeBlock.INFUSED))
                        .forEach(pos -> pLevel.setBlockAndUpdate(pos, pLevel.getBlockState(pos).setValue(FlowCedarLikeBlock.INFUSED, false)));
                getNearBlocks(b_pos,chained ? 4 : 2).stream()
                        .filter(pos -> pos != pPos)
                        .filter(pos -> pLevel.getBlockState(pos).hasProperty(FlowCedarLikeBlock.INFUSED))
                        .forEach(pos -> pLevel.setBlockAndUpdate(pos, pLevel.getBlockState(pos).setValue(FlowCedarLikeBlock.INFUSED, false)));

        }
    }

    public static long distSqr(Vec3i a, Vec3i b) {
        //Vec3i#distSqr, while convenient, offsets the second argument by (0.5, 0.5, 0.5).
        //Longs are used because "dx * dx" overflows with distances longer than about 46,300 blocks when using integers.
        long dx = a.getX() - b.getX();
        long dy = a.getY() - b.getY();
        long dz = a.getZ() - b.getZ();
        return dx * dx + dy * dy + dz * dz;
    }
}
