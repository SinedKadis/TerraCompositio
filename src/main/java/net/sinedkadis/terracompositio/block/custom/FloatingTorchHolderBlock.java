package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.sinedkadis.terracompositio.block.entity.FloatingTorchHolderBlockEntity;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FloatingTorchHolderBlock extends TCBaseEntityBlock {
    public FloatingTorchHolderBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected BlockEntityType<? extends TCBlockEntity> getBlockEntityType() {
        return TCBlockEntities.FLOATING_TORCH_HOLDER_BE.get();
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
        if (blockEntity instanceof FloatingTorchHolderBlockEntity be) {
            blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
                ItemStack stackInSlot = iItemHandler.getStackInSlot(0);
                if (stackInSlot.isEmpty()) return;
                Block.byItem(stackInSlot.getItem()).animateTick(be.getHoldState(),be.getFakeLevel(),pPos,pRandom);
            });
        }
        super.animateTick(pState, pLevel, pPos, pRandom);
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        super.tick(pState, pLevel, pPos, pRandom);
        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
        if (blockEntity instanceof FloatingTorchHolderBlockEntity be) {
            blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
                ItemStack stackInSlot = iItemHandler.getStackInSlot(0);
                if (stackInSlot.isEmpty()) return;
                Block.byItem(stackInSlot.getItem()).tick(be.getHoldState(),be.getFakeLevel(),pPos,pRandom);
            });
        }

    }

    @Override
    public int getSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
        if (blockEntity instanceof FloatingTorchHolderBlockEntity be) {
            Optional<IItemHandler> capability = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
            if (capability.isPresent()) {
                IItemHandler iItemHandler = capability.get();
                ItemStack stackInSlot = iItemHandler.getStackInSlot(0);
                if (stackInSlot.isEmpty()) return 0;

                BlockState holdState = be.getHoldState();
                return holdState.getBlock().getSignal(holdState,be.getFakeLevel(),pPos,pDirection);
            }
        }
        return super.getSignal(pState, pLevel, pPos, pDirection);
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
        super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);
        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
        if (blockEntity instanceof FloatingTorchHolderBlockEntity be) {
            Optional<IItemHandler> capability = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
            if (capability.isPresent()) {
                IItemHandler iItemHandler = capability.get();
                ItemStack stackInSlot = iItemHandler.getStackInSlot(0);
                if (stackInSlot.isEmpty()) return;

                BlockState holdState = be.getHoldState();
                holdState.getBlock().neighborChanged(holdState,be.getFakeLevel(),pPos,pNeighborBlock,pNeighborPos,pMovedByPiston);
            }
        }
    }

    @Override
    public boolean isSignalSource(BlockState pState) {
        return true;
    }
}
