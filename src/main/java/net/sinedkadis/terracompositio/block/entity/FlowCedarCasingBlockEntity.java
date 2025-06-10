package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.sinedkadis.terracompositio.registries.ModBlockEntities;
import net.sinedkadis.terracompositio.registries.ModBlockStateProperties;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.stream.IntStream;

import static net.minecraft.world.level.block.entity.HopperBlockEntity.getContainerAt;
import static net.sinedkadis.terracompositio.block.custom.FlowCedarCasingBlock.*;
import static net.sinedkadis.terracompositio.registries.ModBlockStateProperties.INPUT_BUS;

public class FlowCedarCasingBlockEntity extends ModItemIOCFEBlockEntity{

    private int cooldownTime;

    public FlowCedarCasingBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLOW_CEDAR_CASING_BE.get(), pos, state,BlockMode.CONSUMER);
    }

    protected <T> @Nullable LazyOptional<T> getCap(@NotNull Capability<T> cap, @Nullable Direction side) {
        if ((side == null && cap == ForgeCapabilities.ITEM_HANDLER)
                || cap == ForgeCapabilities.ITEM_HANDLER
                && ((side.equals(Direction.UP) && this.getBlockState().getValue(INPUT_BUS))
                    || (side.equals(Direction.DOWN) && this.getBlockState().getValue(ModBlockStateProperties.OUTPUT_BUS)))) {
            return lazyItemHandler.cast();
        }
        return null;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }


    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        super.tick(pLevel,pPos,pState);
        --this.cooldownTime;
        if (this.notOnCooldown() && hasOutputBusConnection(pState)) {
            this.setCooldown(0);
            tryMoveItems(pLevel, pPos, pState);
        }
    }

    private boolean tryMoveItems(Level pLevel, BlockPos pPos, BlockState pState) {
        if (!pLevel.isClientSide) {
            if (this.notOnCooldown()) {
                boolean flag = false;
                if (!this.itemHandler.getStackInSlot(SLOT_OUTPUT).isEmpty()) {
                    flag = ejectItems(pLevel, pPos);
                }

                if (flag) {
                    this.setCooldown(8);
                    setChanged(pLevel, pPos, pState);
                    return true;
                }
            }

        }
        return false;
    }

    private  boolean ejectItems(Level level, BlockPos blockPos) {
        if (this.vanillaInsertHook()) {
            return true;
        } else {
            Container container = getAttachedContainer(level, blockPos);
            if (container != null) {
                Direction direction = Direction.DOWN.getOpposite();
                if (!isFullContainer(container, direction)) {
                    if (!this.itemHandler.getStackInSlot(1).isEmpty()) {
                        ItemStack itemstack = this.itemHandler.getStackInSlot(1).copy();
                        ItemStack itemstack1 = addItem(container, this.itemHandler.extractItem(1, 1, false), direction);
                        if (itemstack1.isEmpty()) {
                            container.setChanged();
                            return true;
                        }
                        this.itemHandler.setStackInSlot(1, itemstack);
                    }
                }
            }
            return false;
        }
    }

    private ItemStack addItem(Container pDestination, ItemStack pStack, @javax.annotation.Nullable Direction pDirection) {
        if (pDestination instanceof WorldlyContainer worldlycontainer) {
            if (pDirection != null) {
                int[] aint = worldlycontainer.getSlotsForFace(pDirection);

                for(int k = 0; k < aint.length && !pStack.isEmpty(); ++k) {
                    pStack = tryMoveInItem(pDestination, pStack, aint[k], pDirection);
                }

                return pStack;
            }
        }

        int i = pDestination.getContainerSize();

        for(int j = 0; j < i && !pStack.isEmpty(); ++j) {
            pStack = tryMoveInItem(pDestination, pStack, j, pDirection);
        }

        return pStack;
    }

    private ItemStack tryMoveInItem(Container pDestination, ItemStack pStack, int pSlot, @javax.annotation.Nullable Direction pDirection) {
        ItemStack itemstack = pDestination.getItem(pSlot);
        if (canPlaceItemInContainer(pDestination, pStack, pSlot, pDirection)) {
            boolean flag = false;
            if (itemstack.isEmpty()) {
                pDestination.setItem(pSlot, pStack);
                pStack = ItemStack.EMPTY;
                flag = true;
            } else if (canMergeItems(itemstack, pStack)) {
                int i = pStack.getMaxStackSize() - itemstack.getCount();
                int j = Math.min(pStack.getCount(), i);
                pStack.shrink(j);
                itemstack.grow(j);
                flag = j > 0;
            }

            if (flag) {
                pDestination.setChanged();
            }
        }

        return pStack;
    }

    private static boolean canMergeItems(ItemStack pStack1, ItemStack pStack2) {
        return pStack1.getCount() <= pStack1.getMaxStackSize() && ItemStack.isSameItemSameTags(pStack1, pStack2);
    }

    private static boolean canPlaceItemInContainer(Container pContainer, ItemStack pStack, int pSlot, @javax.annotation.Nullable Direction pDirection) {
        if (!pContainer.canPlaceItem(pSlot, pStack)) {
            return false;
        } else {
            if (pContainer instanceof WorldlyContainer worldlycontainer) {
                return worldlycontainer.canPlaceItemThroughFace(pSlot, pStack, pDirection);
            }

            return true;
        }
    }

    private static boolean isFullContainer(Container pContainer, Direction pDirection) {
        return getSlots(pContainer, pDirection).allMatch((p_59379_) -> {
            ItemStack itemstack = pContainer.getItem(p_59379_);
            return itemstack.getCount() >= itemstack.getMaxStackSize();
        });
    }

    private static IntStream getSlots(Container pContainer, Direction pDirection) {
        return pContainer instanceof WorldlyContainer ? IntStream.of(((WorldlyContainer)pContainer).getSlotsForFace(pDirection)) : IntStream.range(0, pContainer.getContainerSize());
    }

    @Nullable
    private static Container getAttachedContainer(Level pLevel, BlockPos pPos) {
        return getContainerAt(pLevel, pPos.relative(Direction.DOWN));
    }

    public boolean vanillaInsertHook() {
        return getItemHandler().map((destinationResult) -> {
            IItemHandler itemHandler = destinationResult.getKey();
            if (!isFull(itemHandler)) {
                if (!this.itemHandler.getStackInSlot(1).isEmpty()) {
                    ItemStack originalSlotContents = this.itemHandler.getStackInSlot(1).copy();
                    ItemStack insertStack = this.itemHandler.extractItem(1,1,false);
                    ItemStack remainder = putStackInInventoryAllSlots(itemHandler, insertStack);
                    if (remainder.isEmpty()) {
                        return true;
                    }
                    this.itemHandler.setStackInSlot(1, originalSlotContents);
                }
            }
            return false;
        }).orElse(false);
    }

    private static ItemStack putStackInInventoryAllSlots(IItemHandler destInventory, ItemStack stack) {
        for(int slot = 0; slot < destInventory.getSlots() && !stack.isEmpty(); ++slot) {
            stack = insertStack(destInventory, stack, slot);
        }
        return stack;
    }

    private static ItemStack insertStack(IItemHandler destInventory, ItemStack stack, int slot) {
        ItemStack itemstack = destInventory.getStackInSlot(slot);
        if (destInventory.insertItem(slot, stack, true).isEmpty()) {
            if (itemstack.isEmpty()) {
                destInventory.insertItem(slot, stack, false);
                stack = ItemStack.EMPTY;
            } else if (ItemHandlerHelper.canItemStacksStack(itemstack, stack)) {
                stack = destInventory.insertItem(slot, stack, false);
            }
        }

        return stack;
    }

    private static boolean isFull(IItemHandler itemHandler) {
        for(int slot = 0; slot < itemHandler.getSlots(); ++slot) {
            ItemStack stackInSlot = itemHandler.getStackInSlot(slot);
            if (stackInSlot.isEmpty() || stackInSlot.getCount() < itemHandler.getSlotLimit(slot)) {
                return false;
            }
        }

        return true;
    }

    public Optional<Pair<IItemHandler, Object>> getItemHandler() {
        BlockPos blockpos = this.getBlockPos();
        Level level = this.getLevel();
        if (level != null) {
            if (level.getBlockState(blockpos.below()).hasBlockEntity()) {
                BlockEntity blockEntity = level.getBlockEntity(blockpos);
                if (blockEntity != null) {
                    return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.DOWN).map((capability) -> ImmutablePair.of(capability, blockEntity));
                }
            }
        }
        return Optional.empty();
    }

    private boolean notOnCooldown() {
        return this.cooldownTime <= 0;
    }

    public void setCooldown(int pCooldownTime) {
        this.cooldownTime = pCooldownTime;
    }
}
