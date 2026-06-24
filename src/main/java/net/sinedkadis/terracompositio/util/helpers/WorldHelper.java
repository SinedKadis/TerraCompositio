package net.sinedkadis.terracompositio.util.helpers;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.sinedkadis.terracompositio.block.custom.FlowCedarLikeBlock;
import net.sinedkadis.terracompositio.particle.ECFParticleData;
import net.sinedkadis.terracompositio.registries.TCBlockStateProperties;
import net.sinedkadis.terracompositio.registries.TCGameRules;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static net.sinedkadis.terracompositio.registries.TCBlockStateProperties.INFUSED;

public class WorldHelper {
    public static @NotNull InteractionResult handleInWorldBlockCraft(BlockState oldState, BlockState newState, Level pLevel, BlockPos pPos, ItemStack item, int count, ParticleOptions type, SoundEvent event) {
        pLevel.setBlock(pPos, Blocks.STRUCTURE_VOID.defaultBlockState(), 3);
        pLevel.setBlock(pPos, copyBlockStates(oldState, newState), 3);
        if (count != 0)
            item.shrink(count);
        if (pLevel instanceof ServerLevel level) {
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
        float speed = 1 / 20f;
        return handleInWorldBlockCraft(oldState, newState, pLevel, pPos, item, count, new ECFParticleData(speed), SoundEvents.COPPER_PLACE);
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

    public static boolean onRemoveHandlerBlacklist(BlockState state, Block... blocks) {
        return Arrays.stream(blocks)
                .map(block -> !state.is(block))
                .reduce((aBoolean, aBoolean2) -> aBoolean && aBoolean2)
                .orElse(true);
    }

    public static void flowLeak(BlockState pState, Level pLevel, BlockPos pPos, boolean chained) {
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
            BlockPosHelper.getNearBlocks(f_pos, chained ? 4 : 2).stream()
                    .filter(pos -> pos != pPos)
                    .filter(pos -> pLevel.getBlockState(pos).hasProperty(FlowCedarLikeBlock.INFUSED))
                    .forEach(pos -> pLevel.setBlockAndUpdate(pos, pLevel.getBlockState(pos).setValue(FlowCedarLikeBlock.INFUSED, false)));
            BlockPosHelper.getNearBlocks(b_pos, chained ? 4 : 2).stream()
                    .filter(pos -> pos != pPos)
                    .filter(pos -> pLevel.getBlockState(pos).hasProperty(FlowCedarLikeBlock.INFUSED))
                    .forEach(pos -> pLevel.setBlockAndUpdate(pos, pLevel.getBlockState(pos).setValue(FlowCedarLikeBlock.INFUSED, false)));

        }
    }

    public static int getLightLevel(Level level, BlockPos pos, @Nullable Direction facing) {
        int bLight;
        int sLight;
        if (facing == null) {
            bLight = level.getBrightness(LightLayer.BLOCK, pos);
            sLight = level.getBrightness(LightLayer.SKY, pos);
        } else {
            bLight = level.getBrightness(LightLayer.BLOCK, pos.relative(facing));
            sLight = level.getBrightness(LightLayer.SKY, pos.relative(facing));
        }

        return LightTexture.pack(bLight, sLight);
    }

    public static void destroyBlockNoUpdate(Level level, BlockPos pPos, Player player) {
        BlockState blockstate = level.getBlockState(pPos);

        if (blockstate.isAir()) return;

        FluidState fluidstate = level.getFluidState(pPos);

        BlockEntity blockentity = blockstate.hasBlockEntity() ? level.getBlockEntity(pPos) : null;
        Block.dropResources(blockstate, level, pPos, blockentity, player, ItemStack.EMPTY);

        boolean flag = level.setBlock(pPos, fluidstate.createLegacyBlock(), 3, 512);
        if (flag) {
            level.gameEvent(GameEvent.BLOCK_DESTROY, pPos, GameEvent.Context.of(player, blockstate));
        }

    }
}
