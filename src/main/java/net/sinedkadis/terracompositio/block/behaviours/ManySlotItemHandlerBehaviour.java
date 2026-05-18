package net.sinedkadis.terracompositio.block.behaviours;

import lombok.Data;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.WorldlyContainer;
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
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@Data
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ManySlotItemHandlerBehaviour implements IBEItemBehaviour, WorldlyContainer {
    private final TCBlockEntity blockEntity;
    public boolean ignoreRestrictions = false;

    protected ItemStackHandler itemHandler = new ItemStackHandler() {
        @Override
        public int getSlotLimit(int slot) {
            return getLimitInSlot(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return allowInsert(slot, stack);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!allowInsert(slot, stack) && !ignoreRestrictions) return stack;
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!allowExtract(slot, getStackInSlot(slot)) && !ignoreRestrictions) return ItemStack.EMPTY;
            return super.extractItem(slot, amount, simulate);
        }
    };
    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    //One slot
    public ManySlotItemHandlerBehaviour(TCBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public ManySlotItemHandlerBehaviour(TCBlockEntity blockEntity, int slotCount) {
        this.blockEntity = blockEntity;
        itemHandler.setSize(slotCount);
    }

    public int getLimitInSlot(int slot) {
        return 64;
    }

    private boolean allowExtract(int pSlot, ItemStack pStack) {
        return allowExtract(pSlot, pStack, null);
    }

    @Override
    public boolean allowExtract(int pSlot, ItemStack pStack, @Nullable Direction pDirection) {
        return true;
    }

    private boolean allowInsert(int pSlot, ItemStack pStack) {
        return allowInsert(pSlot, pStack, null);
    }

    @Override
    public boolean allowInsert(int pSlot, ItemStack pStack, @Nullable Direction pDirection) {
        return true;
    }

    @Override
    public void tick() {

    }

    @Override
    public void onChunkLoad() {
        lazyItemHandler = LazyOptional.of(this::getItemHandler);
    }

    @Override
    public @Nullable LazyOptional<?> getCapability(Capability<?> cap, @Nullable Direction side) {
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
        tag.put("itemHandler", getItemHandler().serializeNBT());
    }

    private static boolean hasSpace(ItemStackHandler itemHandler, int i) {
        return itemHandler.getSlotLimit(i) - itemHandler.getStackInSlot(i).getCount() > 0;
    }

    @Override
    public void onLoad(CompoundTag tag) {
        getItemHandler().deserializeNBT(tag.getCompound("itemHandler"));
    }

    @Override
    public InteractionResult onUse(Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        Level level = blockEntity.getLevel();
        if (level == null) return InteractionResult.PASS;

        BlockPos blockPos = blockEntity.getBlockPos();

        ItemStack itemInHand = pPlayer.getItemInHand(pHand);

        ItemStackHandler itemHandler = getItemHandler();

        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack slot = itemHandler.getStackInSlot(i);

            if (!slot.isEmpty() && allowExtract(i, slot)) {
                ItemStack extracted = itemHandler.extractItem(i, 64, false);
                TCUtil.addOrDropToPlayer(pPlayer, extracted);
                level.playSound(pPlayer, blockPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS);
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
            if (!itemInHand.isEmpty() && allowInsert(i, itemInHand) && hasSpace(itemHandler, i)) {
                ItemStack left = itemHandler.insertItem(i, itemInHand.copy(), false);
                pPlayer.setItemInHand(pHand, left);
                level.playSound(pPlayer, blockPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS);
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }


        return InteractionResult.PASS;
    }


    @Override
    public int[] getSlotsForFace(Direction direction) {
        int slots = itemHandler.getSlots();
        int[] ints = new int[slots];
        for (int i = 0; i < slots; i++) {
            ints[i] = i;
        }
        return ints;
    }

    @Override
    public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
        return allowInsert(pIndex, pItemStack, pDirection);
    }

    @Override
    public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
        return allowExtract(pIndex, pStack, pDirection);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack slot = itemHandler.getStackInSlot(i);
            if (slot.isEmpty()) continue;
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int pSlot) {
        return getItemHandler().getStackInSlot(pSlot);
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        return getItemHandler().extractItem(pSlot, pAmount, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        ItemStack stackInSlot = getItemHandler().getStackInSlot(pSlot);
        getItemHandler().setStackInSlot(pSlot, ItemStack.EMPTY);
        return stackInSlot;
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {
        getItemHandler().setStackInSlot(pSlot, pStack);
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
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }


}
