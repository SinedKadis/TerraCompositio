package net.sinedkadis.terracompositio.item.custom;


import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.minecraft.world.level.block.Block.dropResources;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.*;
import static net.sinedkadis.terracompositio.util.TCUtil.getNearBlocks;
import static net.sinedkadis.terracompositio.util.TCUtil.getTouchingBlocks;

public class WrenchAxeItem extends AxeItem {
    protected WrenchMode wrenchMode = WrenchMode.AXE;
    private static final TagKey<Block> LOGS_TAG = BlockTags.LOGS;
    public static final List<Property<?>> SURVIVAL_SAFE_PROPERTIES = List.of(
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


    public WrenchAxeItem(Tier pTier, float pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);
        if (pPlayer.isCrouching() && !isPlayerLookingAtBlock(pPlayer,pLevel)) {
            wrenchMode = wrenchMode.next(); // Переключаем режим
            if (!stack.hasTag()) {
                stack.setTag(new CompoundTag());
            }
            stack.getTag().putInt("WrenchMode", wrenchMode.ordinal());
            pPlayer.displayClientMessage(Component.translatable("message.terracompositio.wrench_mode", wrenchMode.getDisplayName()), true);
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }
    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        if (!stack.hasTag()) {
            stack.setTag(new CompoundTag());
        }
        stack.getTag().putInt("WrenchMode", wrenchMode.ordinal());
    }

    @Override
    public void verifyTagAfterLoad(CompoundTag tag) {
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

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
        if (!pLevel.isClientSide && pEntity instanceof Player) {
            wrenchMode = loadWrenchMode(pStack);
        }
    }

    @Override
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        if (!pLevel.isClientSide && wrenchMode == WrenchMode.WRENCH) {
            this.wrenchInteraction(pPlayer, pState, pLevel, pPos, false, pPlayer.getItemInHand(InteractionHand.MAIN_HAND));
            return false;
        }

        return true;
    }

    @Override
    public float getDestroySpeed(ItemStack pStack, BlockState pState) {
        return 255;
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        Direction face = context.getHorizontalDirection();
        InteractionHand hand = context.getHand();
        ItemStack stack = context.getItemInHand();
        if (player instanceof FakePlayer) return InteractionResult.PASS;
        switch (wrenchMode) {
            case WRENCH_AXE -> {
                if (!level.isClientSide && (player == null || (level.mayInteract(player, pos)))) {
                    if (!this.wrenchAxeInteraction(level, pos, face, player, stack, hand)) {
                        return InteractionResult.FAIL;
                    }
                }
                return InteractionResult.PASS;
            }
            case WRENCH -> {
                if (!level.isClientSide && (player == null || (level.mayInteract(player, pos)))) {
                    if (!this.wrenchInteraction(player, level.getBlockState(pos), level, pos, true, context.getItemInHand())) {
                        return InteractionResult.FAIL;
                    }
                }//todo: Mod compatibility, create wrench behavior
                return InteractionResult.PASS;
            }
            case AXE -> {
                return super.useOn(context);
            }
            default -> {
                return InteractionResult.PASS;
            }
        }
    }

    private boolean wrenchAxeInteraction(Level level, BlockPos pos, Direction face, Player player, ItemStack stack, InteractionHand hand) {
        BlockPos anchor = getAnchor(level, pos,LOGS_TAG, face);
        List<BlockPos> tree = getNearBlocks(level,anchor, LOGS_TAG,10);
        if (tree.size()>32 && player != null) {
            player.displayClientMessage(Component.translatable("item.terracompositio.flow_rotating_axe.too_massive_tree").withStyle(ChatFormatting.BOLD), true);
            return false;
        }
        List<BlockPos> peaks = tree.stream().filter(blockPos -> !level.getBlockState(blockPos.above()).is(BlockTags.LOGS)).toList();
        List<BlockPos> allLeaves = new ArrayList<>();
        peaks.forEach(blockPos -> allLeaves.addAll(getTouchingBlocks(level,blockPos,BlockTags.LEAVES,6)));
        allLeaves.forEach(blockPos -> {
            BlockState state = level.getBlockState(blockPos);
            if (state.is(BlockTags.LEAVES)){
                dropResources(state, level, blockPos);
                level.destroyBlock(blockPos, false, player,3);
                level.sendBlockUpdated(blockPos,state,state, Block.UPDATE_ALL);
            }
        });
        final int[] addHeight = {0};
        tree.forEach(blockPos -> {
            BlockPos.MutableBlockPos newPos = rotateTreeBlock(blockPos, face, anchor).mutable();
            BlockState newState = rotateBlockState(level.getBlockState(blockPos), face);
            level.destroyBlock(blockPos, false, player,3);
            level.sendBlockUpdated(blockPos, level.getBlockState(blockPos), level.getBlockState(blockPos), Block.UPDATE_CLIENTS);
            addHeight[0] = tryToPlace(level,newPos.immutable().above(addHeight[0]),newState) ? addHeight[0]++ : addHeight[0];
            if (player != null)
                stack.hurtAndBreak(1, player, player1 -> player1.broadcastBreakEvent(hand));
        });
        return true;
    }

    private boolean tryToPlace(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockState(pos).isAir()){
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
        // Получаем текущую ось блока
        Direction.Axis currentAxis = state.getValue(AXIS);

        // Определяем новую ось в зависимости от направления игрока и текущей оси
        Direction.Axis newAxis = switch (pushDirection) {
            case NORTH, SOUTH -> {
                if (currentAxis == Direction.Axis.Y) {
                    yield Direction.Axis.Z; // Если блок был вертикальным, ложим его на ось X
                } else if (currentAxis == Direction.Axis.Z) {
                    yield Direction.Axis.Y; // Если блок лежал на оси X, ставим его вертикально
                } else {
                    yield currentAxis; // Иначе оставляем как есть
                }
            }
            case WEST, EAST -> {
                if (currentAxis == Direction.Axis.Y) {
                    yield Direction.Axis.X; // Если блок был вертикальным, ложим его на ось Z
                } else if (currentAxis == Direction.Axis.X) {
                    yield Direction.Axis.Y; // Если блок лежал на оси Z, ставим его вертикально
                } else {
                    yield currentAxis; // Иначе оставляем как есть
                }
            }
            default -> currentAxis; // Для других направлений оставляем ось без изменений
        };

        // Обновляем состояние блока с новой осью
        return state.setValue(AXIS, newAxis);
    }

    private static BlockPos rotateTreeBlock(BlockPos pos, Direction direction, BlockPos anchor) {
        int x = pos.getX() - anchor.getX();
        int y = pos.getY() - anchor.getY();
        int z = pos.getZ() - anchor.getZ();

        // Поворот на 90 градусов в зависимости от направления
        return switch (direction) {
            case NORTH -> new BlockPos(anchor.getX() + x, anchor.getY() + z - 1, anchor.getZ() - y); // Вращение вокруг X
            case SOUTH -> new BlockPos(anchor.getX() + x, anchor.getY() - z - 1, anchor.getZ() + y); // Вращение вокруг X
            case WEST -> new BlockPos(anchor.getX() - y, anchor.getY() + x - 1, anchor.getZ() + z);  // Вращение вокруг Z
            case EAST -> new BlockPos(anchor.getX() + y, anchor.getY() - x - 1, anchor.getZ() + z);  // Вращение вокруг Z
            default -> pos; // Без изменений
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

    private BlockPos getAnchor(Level level, BlockPos origin, TagKey<Block> tag, Direction face) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos().set(origin);
        int maxDistance = 64; // Максимальное расстояние для поиска
        while (maxDistance-- > 0 && level.getBlockState(mutablePos.move(face)).is(tag)) {
            // Продолжаем двигаться
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
        if (stack.hasTag() && stack.getTag().contains("WrenchMode")) {
            return WrenchMode.values()[stack.getTag().getInt("WrenchMode")];
        }
        return WrenchMode.AXE; // Default mode
    }

    private boolean wrenchInteraction(@Nullable Player pPlayer, BlockState pStateClicked, LevelAccessor pAccessor, BlockPos pPos, boolean pShouldCycleState, ItemStack pDebugStack) {
        Block block = pStateClicked.getBlock();
        StateDefinition<Block, BlockState> blockStateDefinition = block.getStateDefinition();
        Collection<Property<?>> properties = blockStateDefinition.getProperties().stream().filter(SURVIVAL_SAFE_PROPERTIES::contains).toList();
        String name = BuiltInRegistries.BLOCK.getKey(block).toString();
        if (properties.isEmpty()) {
            if (pPlayer != null)
                message(pPlayer, Component.translatable("item.terracompositio.flow_rotating_axe.no_change").withStyle(ChatFormatting.BOLD));
            return false;
        } else {
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
                } else {
                    newState = cycleState(pStateClicked, blockStateDefinitionProperty, false);
                }
                pAccessor.setBlock(pPos, newState, 18);
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
        ((ServerPlayer)pPlayer).sendSystemMessage(pMessageComponent, true);
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
}

