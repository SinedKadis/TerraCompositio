package net.sinedkadis.terracompositio.item.custom;


import mekanism.api.annotations.ParametersAreNotNullByDefault;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.registries.ForgeRegistries;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.cfe.CFESource;
import net.sinedkadis.terracompositio.block.custom.ConstructionDesorberBlock;
import net.sinedkadis.terracompositio.registries.ModItems;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.minecraft.world.level.block.Block.dropResources;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.*;
import static net.sinedkadis.terracompositio.registries.ModBlockStateProperties.INFUSED;
import static net.sinedkadis.terracompositio.util.TCUtil.getNearBlocks;
import static net.sinedkadis.terracompositio.util.TCUtil.getTouchingBlocks;

public class WrenchAxeItem extends AxeItem {
    private static final TagKey<Block> LOGS_TAG = BlockTags.LOGS;
    public static final List<Property<?>> SURVIVAL_SAFE_PROPERTIES;


    public WrenchAxeItem(Tier pTier, float pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack pToRepair, ItemStack pRepair) {
        return pRepair.is(ModItems.INFUSED_IRON_INGOT.get()) || pRepair.is(ModItems.WRENCH_AXE.get());
    }

    @Override
    @ParametersAreNotNullByDefault
    public @NotNull InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);
        if (pPlayer.isCrouching() && !isPlayerLookingAtBlock(pPlayer,pLevel)) {
            setWrenchMode(stack,getWrenchMode(stack).next()); // Переключаем режим
            if (!stack.hasTag()) {
                stack.setTag(new CompoundTag());
            }
            if (stack.getTag() != null) {
                stack.getTag().putInt("WrenchMode", getWrenchMode(stack).ordinal());
            }
            pPlayer.displayClientMessage(Component.translatable("message.terracompositio.wrench_mode", getWrenchMode(stack).getDisplayName()), true);
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }
    @Override
    @ParametersAreNotNullByDefault
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        if (!stack.hasTag()) {
            stack.setTag(new CompoundTag());
        }
        if (stack.getTag() != null) {
            stack.getTag().putInt("WrenchMode", getWrenchMode(stack).ordinal());
        }
    }

    @Override
    public void verifyTagAfterLoad(@NotNull CompoundTag tag) {
        super.verifyTagAfterLoad(tag);
        if (!tag.contains("WrenchMode")) {
            tag.putInt("WrenchMode", WrenchMode.AXE.ordinal());
        }

        int modeOrdinal = tag.getInt("WrenchMode");
        if (modeOrdinal < 0 || modeOrdinal >= WrenchMode.values().length) {
            tag.putInt("WrenchMode", WrenchMode.AXE.ordinal());
        }
    }

    private WrenchMode loadWrenchMode(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("WrenchMode")) {
            return WrenchMode.fromOrdinal(tag.getInt("WrenchMode"));
        }
        return WrenchMode.AXE;
    }

    public WrenchMode getWrenchMode(ItemStack stack) {
        if (stack.getItem() instanceof WrenchAxeItem) {
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.contains("WrenchMode")) {
                return WrenchMode.fromOrdinal(tag.getInt("WrenchMode"));
            }
        }
        return WrenchMode.AXE;
    }

    // Метод для установки режима в NBT
    public void setWrenchMode(ItemStack stack, WrenchMode mode) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("WrenchMode", mode.ordinal());
    }

    @Override
    @ParametersAreNotNullByDefault
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
        if (!pLevel.isClientSide && pEntity instanceof Player) {
            setWrenchMode(pStack,loadWrenchMode(pStack));
        }
    }

    @Override
    @ParametersAreNotNullByDefault
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        if (getWrenchMode(pPlayer.getItemInHand(InteractionHand.MAIN_HAND)) == WrenchMode.WRENCH) {
            this.wrenchInteraction(pPlayer, pState, pLevel, pPos, false, pPlayer.getItemInHand(InteractionHand.MAIN_HAND));
            return false;
        }

        return true;
    }

    @Override
    @ParametersAreNotNullByDefault
    public float getDestroySpeed(ItemStack pStack, BlockState pState) {
        return this.getWrenchMode(pStack).equals(WrenchMode.WRENCH) ? 255 : super.getDestroySpeed(pStack,pState);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack pStack) {
        return switch (getWrenchMode(pStack)){
            case WRENCH_AXE,AXE -> 72000;
            default -> super.getUseDuration(pStack);
        };
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack pStack) {
        return switch (getWrenchMode(pStack)){
            case WRENCH_AXE -> UseAnim.BOW;
            case AXE -> UseAnim.CROSSBOW;
            default -> UseAnim.NONE;
        };
    }


    @Override
    @ParametersAreNotNullByDefault
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        super.releaseUsing(pStack, pLevel, pLivingEntity, pTimeCharged);
        if (pLivingEntity instanceof Player player) {
            if (player instanceof FakePlayer) return;
            BlockPos pos = getEntityLookingBlockPos(pLivingEntity, pLevel);
            Direction face = player.getDirection();
            if (pos == null) {
                return;
            }
            switch (getWrenchMode(pStack)) {
                case WRENCH_AXE -> this.wrenchAxeInteraction(pLevel, pos, face, player, pStack, Math.min(player.getTicksUsingItem(), 60));
                case AXE -> this.doubleAxeInteraction(pLevel, pos, face, player, pStack, Math.min(player.getTicksUsingItem(), 60));
            }
        }
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        ItemStack stack = context.getItemInHand();
        BlockState blockState = level.getBlockState(pos);
        if (player instanceof FakePlayer) return InteractionResult.PASS;
        if (hand.equals(InteractionHand.OFF_HAND)) return InteractionResult.PASS;
        switch (getWrenchMode(stack)) {
            case WRENCH_AXE -> {
                if (player != null && !level.isClientSide && level.mayInteract(player, pos) && blockState.is(BlockTags.LOGS)) {
                    player.startUsingItem(hand);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.PASS;
            }
            case WRENCH -> {
                if (!level.isClientSide && (player == null || (level.mayInteract(player, pos)))) {
                    if (!this.wrenchInteraction(player, blockState, level, pos, true, stack)) {
                        return InteractionResult.FAIL;
                    }
                    return InteractionResult.SUCCESS;
                }//todo: Mod compatibility, create wrench behavior
                return InteractionResult.PASS;
            }
            case AXE -> {
                if (player != null) {
                    ItemStack itemInOffHand = player.getItemInHand(InteractionHand.OFF_HAND);
                    if (itemInOffHand.is(ItemTags.AXES)
                            && level.mayInteract(player, pos)) {
                        player.startUsingItem(InteractionHand.MAIN_HAND);
                        return InteractionResult.SUCCESS;
                    }
                }
                return super.useOn(context);
            }
            default -> {
                return InteractionResult.PASS;
            }
        }
    }

    private void doubleAxeInteraction(Level level, BlockPos pos, Direction face, Player player, ItemStack stack, int usedTicks) {
        BlockPos anchor = getAnchor(level, pos,LOGS_TAG, face);
        List<BlockPos> tree = new ArrayList<>(getNearBlocks(level, anchor, LOGS_TAG, 32).stream().filter(blockPos -> blockPos.getY() >= anchor.getY()).toList());
        Collections.reverse(tree);
        List<BlockPos> peaks = tree.stream().filter(blockPos -> !level.getBlockState(blockPos.above()).is(BlockTags.LOGS)).toList();
        Set<BlockPos> allLeaves = new HashSet<>();
        peaks.forEach(blockPos -> allLeaves.addAll(getTouchingBlocks(level,blockPos,BlockTags.LEAVES,6)));
        allLeaves.forEach(blockPos -> {
            if (usedTicks > 20){
                BlockState state = level.getBlockState(blockPos);
                if (state.is(BlockTags.LEAVES)) {
                    dropResources(state, level, blockPos);
                    level.destroyBlock(blockPos, false, player, 3);
                    level.sendBlockUpdated(blockPos, state, state, Block.UPDATE_ALL);
                }
            }
        });
        int blocksToRemove = Mth.lerpInt((float) usedTicks / 60, 1, 64);
        int foodTakeCount = 6;
        for (BlockPos blockPos : tree) {
            if (usedTicks > 20 && blocksToRemove > 0) {
                BlockState state = level.getBlockState(blockPos);
                if (state.is(BlockTags.LOGS)) {
                    level.destroyBlock(blockPos, true, player, 3);
                    level.sendBlockUpdated(blockPos, state, state, Block.UPDATE_CLIENTS);
                    blocksToRemove--;
                    foodTakeCount--;
                    if (player != null) {
                        stack.hurtAndBreak(1, player, player1 -> player1.broadcastBreakEvent(InteractionHand.MAIN_HAND));
                        player.getItemInHand(InteractionHand.OFF_HAND)
                                .hurtAndBreak(1, player, player1 -> player1.broadcastBreakEvent(InteractionHand.OFF_HAND));
                        if (foodTakeCount == 0) {
                            foodTakeCount = 6;
                            if (!(player.getFoodData().getFoodLevel() == 0 && player.getFoodData().getSaturationLevel() == 0)) {
                                player.causeFoodExhaustion(5);
                            } else {
                                DamageSource treeChopDamageSource = level.damageSources().playerAttack(player);
                                player.hurt(treeChopDamageSource, 5.0F);
                            }
                        }
                    }
                }
            } else {
                break;
            }
        }
    }

    private void wrenchAxeInteraction(Level level, BlockPos pos, Direction face, Player player, ItemStack stack, int usedTicks) {
        if (usedTicks > 20){
            BlockPos anchor = getAnchor(level, pos, LOGS_TAG, face);
            BlockState anchorState = level.getBlockState(anchor);
            List<BlockPos> tree = getNearBlocks(level, anchor, LOGS_TAG, 10).stream().filter(blockPos -> blockPos.getY() >= anchor.getY()).toList();
            if (tree.size() > 32 && player != null) {
                player.displayClientMessage(Component.translatable("item.terracompositio.flow_rotating_axe.too_massive_tree").withStyle(ChatFormatting.BOLD), true);
                return;
            }
            List<BlockPos> peaks = tree.stream().filter(blockPos -> !level.getBlockState(blockPos.above()).is(BlockTags.LOGS)).toList();
            Set<BlockPos> allLeaves = new HashSet<>();
            peaks.forEach(blockPos -> allLeaves.addAll(getTouchingBlocks(level, blockPos, BlockTags.LEAVES, 6)));
            allLeaves.forEach(blockPos -> {
                BlockState state = level.getBlockState(blockPos);
                if (state.is(BlockTags.LEAVES)) {
                    dropResources(state, level, blockPos);
                    level.destroyBlock(blockPos, false, player, 3);
                    level.sendBlockUpdated(blockPos, state, state, Block.UPDATE_ALL);
                }
            });
            final int[] addHeight = {0};
            final boolean isAnchorInfused = anchorState.hasProperty(INFUSED) ? anchorState.getValue(INFUSED) : false;
            int foodTakeCount = 6;
            for (BlockPos blockPos : tree) {
                BlockPos newPos = rotateTreeBlock(blockPos, face, anchor);
                BlockState oldState = level.getBlockState(blockPos);
                BlockState newState = rotateBlockState(oldState, face);
                if (oldState.hasProperty(INFUSED)) {
                    newState = newState.setValue(INFUSED, oldState.getValue(INFUSED));
                }
                level.setBlock(blockPos, Blocks.STRUCTURE_VOID.defaultBlockState(), 3);
                level.destroyBlock(blockPos, false, player, 3);
                level.sendBlockUpdated(blockPos, oldState, oldState, Block.UPDATE_CLIENTS);
                foodTakeCount++;
                addHeight[0] = tryToPlace(level, newPos.above(addHeight[0]), newState, isAnchorInfused) ? addHeight[0]++ : addHeight[0];
                if (player != null) {
                    stack.hurtAndBreak(1, player, player1 -> player1.broadcastBreakEvent(InteractionHand.MAIN_HAND));
                    if (foodTakeCount == 0) {
                        foodTakeCount = 6;
                        if (!(player.getFoodData().getFoodLevel() == 0 && player.getFoodData().getSaturationLevel() == 0)) {
                            player.causeFoodExhaustion(5);
                        } else {
                            DamageSource treeChopDamageSource = level.damageSources().playerAttack(player);
                            player.hurt(treeChopDamageSource, 5.0F);
                        }
                    }
                }
            }
        }
    }

    private boolean tryToPlace(Level level, BlockPos pos, BlockState state,boolean forceInfused) {
        state = forceInfused ? state.setValue(INFUSED, true) : state;
        if (level.getBlockState(pos).isAir() || level.getBlockState(pos).getPistonPushReaction().equals(PushReaction.DESTROY)){
            level.setBlockAndUpdate(pos, state);
            level.sendBlockUpdated(pos,state,state, Block.UPDATE_CLIENTS);
            return false;
        }
        if (!level.getBlockState(pos.above()).isAir()) {
            if (state.getDestroySpeed(level, pos) < 0) {
                return false;
            }
            level.destroyBlock(pos.above(), true);
        }
        level.setBlockAndUpdate(pos.above(), state);
        level.sendBlockUpdated(pos.above(),state,state, Block.UPDATE_CLIENTS);
        return true;
    }

    private BlockState rotateBlockState(BlockState state, Direction pushDirection) {
        if (!state.hasProperty(AXIS))
            return state;

        Direction.Axis currentAxis = state.getValue(AXIS);

        Direction.Axis newAxis = switch (pushDirection) {
            case NORTH, SOUTH -> {
                if (currentAxis == Direction.Axis.Y) {
                    yield Direction.Axis.Z;
                } else if (currentAxis == Direction.Axis.Z) {
                    yield Direction.Axis.Y;
                } else {
                    yield currentAxis;
                }
            }
            case WEST, EAST -> {
                if (currentAxis == Direction.Axis.Y) {
                    yield Direction.Axis.X;
                } else if (currentAxis == Direction.Axis.X) {
                    yield Direction.Axis.Y;
                } else {
                    yield currentAxis;
                }
            }
            default -> currentAxis;
        };


        BlockState newState = state.setValue(AXIS, newAxis);
        if (state.hasProperty(INFUSED)) {
            newState = newState.setValue(INFUSED, state.getValue(INFUSED));
        }
        return newState;
    }

    private static BlockPos rotateTreeBlock(BlockPos pos, Direction direction, BlockPos anchor) {
        int x = pos.getX() - anchor.getX();
        int y = pos.getY() - anchor.getY();
        int z = pos.getZ() - anchor.getZ();


        return switch (direction) {
            case NORTH -> new BlockPos(anchor.getX() + x, anchor.getY() + z - 1, anchor.getZ() - y);
            case SOUTH -> new BlockPos(anchor.getX() + x, anchor.getY() - z - 1, anchor.getZ() + y);
            case WEST -> new BlockPos(anchor.getX() - y, anchor.getY() + x - 1, anchor.getZ() + z);
            case EAST -> new BlockPos(anchor.getX() + y, anchor.getY() - x - 1, anchor.getZ() + z);
            default -> pos;
        };
    }

    public static boolean isPlayerLookingAtBlock(Player player, Level level) {
        double reachDistance = player.getBlockReach();

        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 lookVector = player.getLookAngle();
        Vec3 endPosition = eyePosition.add(lookVector.scale(reachDistance));

        ClipContext context = new ClipContext(eyePosition, endPosition, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
        BlockHitResult hitResult = level.clip(context);

        return hitResult.getType() == HitResult.Type.BLOCK;
    }

    @Nullable
    public static BlockPos getEntityLookingBlockPos(LivingEntity entity, Level level){
        double reachDistance;
        Vec3 eyePosition;
        Vec3 lookVector;
        Vec3 endPosition;
        if (entity instanceof Player player) {
            reachDistance = player.getBlockReach();
        } else {
            reachDistance = 3.0D;
        }
        eyePosition = entity.getEyePosition(1.0F);
        lookVector = entity.getLookAngle();
        endPosition = eyePosition.add(lookVector.scale(reachDistance));
        ClipContext context = new ClipContext(eyePosition, endPosition, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity);
        BlockHitResult hitResult = level.clip(context);

        return hitResult.getType() == HitResult.Type.BLOCK ? hitResult.getBlockPos() : null;
    }

    private BlockPos getAnchor(Level level, BlockPos origin, TagKey<Block> tag, Direction face) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos().set(origin);
        int maxDistance = 8;
        while (true) {
            if (maxDistance-- <= 0 || !level.getBlockState(mutablePos.move(face)).is(tag))
                break;
        }
        return mutablePos.immutable();
    }
    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @Nullable Level pLevel, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        WrenchMode mode = WrenchMode.fromOrdinal(pStack.getOrCreateTag().getInt("WrenchMode"));
        pTooltipComponents.add(Component.translatable("item.terracompositio.flow_rotating_axe.mode", mode.getDisplayName()).withStyle(ChatFormatting.GRAY));
    }

    public static WrenchMode getMode(ItemStack stack) {
        if (stack.getTag() != null && stack.hasTag() && stack.getTag().contains("WrenchMode")) {
            return WrenchMode.values()[stack.getTag().getInt("WrenchMode")];
        }
        return WrenchMode.AXE; // Default mode
    }

    private boolean wrenchInteraction(@Nullable Player pPlayer, BlockState pStateClicked, LevelAccessor level, BlockPos pos, boolean pShouldCycleState, ItemStack pDebugStack) {
        Block block = pStateClicked.getBlock();
        StateDefinition<Block, BlockState> blockStateDefinition = block.getStateDefinition();
        Collection<Property<?>> properties = blockStateDefinition.getProperties().stream().filter(SURVIVAL_SAFE_PROPERTIES::contains).toList();
        String name = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).toString();
        if (properties.isEmpty()) {
            if (pPlayer != null)
                message(pPlayer, Component.translatable("item.terracompositio.flow_rotating_axe.no_change").withStyle(ChatFormatting.BOLD));
            return false;
        } else {
            CFENetwork network = TerraCompositioAPI.instance().getCFENetworkInstance();
            List<CFESource> sources = network.getAllCFESources((Level) level);
            List<ConstructionDesorberBlock> constructors = sources.stream()
                    .map(CFESource::getCFESourceBlockPos)
                    .filter(cfeSourceBlockPos -> Math.sqrt(cfeSourceBlockPos.distSqr(pos)) < 7)
                    .map(cfeSourceBlockPos -> {
                        if (level.getBlockState(cfeSourceBlockPos).getBlock() instanceof ConstructionDesorberBlock blockEntity)
                            return blockEntity;
                        return null;
                    })
                    .filter(Objects::nonNull).toList();
            if (!constructors.isEmpty()) {
                if (pPlayer != null) {
                    message(pPlayer, Component.translatable("item.terracompositio.flow_rotating_axe.constructor_near").withStyle(ChatFormatting.BOLD));
                    pDebugStack.hurtAndBreak(10,pPlayer,player1 -> player1.broadcastBreakEvent(InteractionHand.MAIN_HAND));
                }
            }
            CompoundTag debugProperty = pDebugStack.getOrCreateTagElement("DebugProperty");
            String debugPropertyName = debugProperty.getString(name);
            Property<?> blockStateDefinitionProperty = blockStateDefinition.getProperty(debugPropertyName);
            if (pShouldCycleState) {
                if (blockStateDefinitionProperty == null) {
                    blockStateDefinitionProperty = properties.iterator().next();
                }
                BlockState newState;
                if (pPlayer != null) {
                    newState = cycleState(pStateClicked, blockStateDefinitionProperty, pPlayer.isSecondaryUseActive());
                    pDebugStack.hurtAndBreak(1,pPlayer,player1 -> player1.broadcastBreakEvent(InteractionHand.MAIN_HAND));
                } else {
                    newState = cycleState(pStateClicked, blockStateDefinitionProperty, false);
                    if (pDebugStack.hurt(1,level.getRandom(),null)){
                        pDebugStack.shrink(1);
                        pDebugStack.setDamageValue(0);
                    }
                }
                level.setBlock(pos, newState, 18);
                message(pPlayer, Component.translatable(Items.DEBUG_STICK.getDescriptionId() + ".update", blockStateDefinitionProperty.getName(), getNameHelper(newState, blockStateDefinitionProperty)).withStyle(ChatFormatting.BOLD));
            } else {
                if (pPlayer != null) {
                    blockStateDefinitionProperty = getRelative(properties, blockStateDefinitionProperty, pPlayer.isSecondaryUseActive());
                } else {
                    blockStateDefinitionProperty = getRelative(properties, blockStateDefinitionProperty, false);
                }
                String $$14 = blockStateDefinitionProperty.getName();
                debugProperty.putString(name, $$14);
                if (pPlayer != null)
                    message(pPlayer, Component.translatable(Items.DEBUG_STICK.getDescriptionId() + ".select", $$14, getNameHelper(pStateClicked, blockStateDefinitionProperty)).withStyle(ChatFormatting.BOLD));
            }
            return true;
        }
    }
    private static <T extends Comparable<T>> BlockState cycleState(BlockState pState, Property<T> pProperty, boolean pBackwards) {
        return pState.setValue(pProperty, getRelative(pProperty.getPossibleValues(), pState.getValue(pProperty), pBackwards));
    }
    private static <T> T getRelative(Iterable<T> pAllowedValues, @javax.annotation.Nullable T pCurrentValue, boolean pBackwards) {
        return pBackwards ? Util.findPreviousInIterable(pAllowedValues, pCurrentValue) : Util.findNextInIterable(pAllowedValues, pCurrentValue);
    }

    private static void message(Player pPlayer, Component pMessageComponent) {
        if (pPlayer instanceof ServerPlayer player)
            player.sendSystemMessage(pMessageComponent, true);
    }
    private static <T extends Comparable<T>> String getNameHelper(BlockState pState, Property<T> pProperty) {
        return pProperty.getName(pState.getValue(pProperty));
    }

    public enum WrenchMode {
        AXE("axe"),
        WRENCH_AXE("wrench_axe"),
        WRENCH("wrench");

        private final String name;

        WrenchMode(String name) {
            this.name = name;
        }

        public WrenchMode next() {
            // Переход к следующему режиму по кругу
            return values()[(this.ordinal() + 1) % values().length];
        }

        public Component getDisplayName() {
            return Component.translatable("item.terracompositio.flow_rotating_axe." + name);
        }

        public static WrenchMode fromOrdinal(int ordinal) {
            return values()[ordinal % values().length];
        }
    }

    static {
        SURVIVAL_SAFE_PROPERTIES = List.of(
                AXIS,
                FACING,
                FACING_HOPPER,
                HORIZONTAL_AXIS,
                HORIZONTAL_FACING,
                IN_WALL,
                POWERED,
                ROTATION_16,
                RAIL_SHAPE,
                RAIL_SHAPE_STRAIGHT,
                HALF,
                NOTEBLOCK_INSTRUMENT,
                VERTICAL_DIRECTION,
                WEST,
                WEST_REDSTONE,
                WEST_WALL,
                NORTH,
                NORTH_REDSTONE,
                NORTH_WALL,
                EAST,
                EAST_REDSTONE,
                EAST_WALL,
                SOUTH,
                SOUTH_REDSTONE,
                SOUTH_WALL
        );
    }
}

