package net.sinedkadis.terracompositio.block.behaviours;

import lombok.Data;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEItemBehaviour;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@Data
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OneSlotItemHandlerBehaviour implements IBEItemBehaviour, WorldlyContainer {
    protected static final int SLOT = 0;


    private final TCBlockEntity blockEntity;

    protected ItemStackHandler itemHandler = new ItemStackHandler() {
        @Override
        public int getSlotLimit(int slot) {
            return getLimitInSlot(slot);
        }
    };

    public int getLimitInSlot(int slot) {
        return 64;
    }

    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public OneSlotItemHandlerBehaviour(TCBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public void tick() {

    }

    @Override
    public void onChunkLoad() {
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public @Nullable LazyOptional<?> getCapability(@NotNull Capability<?> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return null;
    }

    @Override
    public void onRemoved() {

    }

    @Override
    public void onInvalidateCaps() {
        lazyItemHandler.invalidate();
    }

    @Override
    public void onSave(CompoundTag tag) {
        tag.put("itemHandler", itemHandler.serializeNBT());
    }

    @Override
    public void onLoad(CompoundTag tag) {
        itemHandler.deserializeNBT(tag);
    }


    @Override
    public InteractionResult onUse(@NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
        Level level = blockEntity.getLevel();
        if (level == null) return InteractionResult.PASS;

        BlockPos blockPos = blockEntity.getBlockPos();

        ItemStack itemInHand = pPlayer.getItemInHand(pHand);

        ItemStackHandler itemHandler = getItemHandler();

        ItemStack slot = itemHandler.getStackInSlot(0);


        if (!slot.isEmpty()) {
            ItemStack extracted = itemHandler.extractItem(0, 64, false);
            TCUtil.addOrDropToPlayer(pPlayer, extracted);
            level.playSound(pPlayer, blockPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS);
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if(!itemInHand.isEmpty()) {
            ItemStack left = itemHandler.insertItem(0, itemInHand.copy(), false);
            pPlayer.setItemInHand(pHand,left);
            level.playSound(pPlayer,blockPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS);
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return InteractionResult.PASS;
    }


    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
        if (getInputSlot().getCount() > 1) return false;
        if (pDirection != null) {
            return pIndex == SLOT && pDirection.equals(Direction.UP);
        }
        return pIndex == SLOT;
    }

    @Override
    public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
        return pDirection.equals(Direction.DOWN);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return itemHandler.getStackInSlot(SLOT).isEmpty();
    }

    @Override
    public ItemStack getItem(int pSlot) {
        return itemHandler.getStackInSlot(pSlot);
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        return itemHandler.extractItem(pSlot, pAmount, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        ItemStack stackInSlot = itemHandler.getStackInSlot(pSlot);
        itemHandler.setStackInSlot(pSlot, ItemStack.EMPTY);
        return stackInSlot;
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {
        itemHandler.setStackInSlot(pSlot, pStack);
    }

    public void insertItem(int pSlot, ItemStack pStack) {
        itemHandler.insertItem(pSlot, pStack, false);
    }

    @Override
    public void setChanged() {
        blockEntity.setChanged();
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return Container.stillValidBlockEntity(blockEntity, pPlayer);
    }

    @Override
    public void clearContent() {
        itemHandler.setStackInSlot(SLOT, ItemStack.EMPTY);
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Level level = this.blockEntity.getLevel();
        BlockPos worldPosition = this.blockEntity.getBlockPos();
        if (level != null) {
            Containers.dropContents(level, worldPosition, inventory);
        }
    }

    public ItemStack getInputSlot() {
        return this.itemHandler.getStackInSlot(SLOT);
    }
}
