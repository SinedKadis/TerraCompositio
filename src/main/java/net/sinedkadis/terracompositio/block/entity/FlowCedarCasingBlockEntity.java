package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.block.behaviours.ManySlotItemHandlerBehaviour;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCItems;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static net.minecraft.world.level.block.entity.HopperBlockEntity.getContainerAt;

@ParametersAreNonnullByDefault
public class FlowCedarCasingBlockEntity extends TCCraftingBlockEntity{

    public static final int INPUT_BUS_SLOT = 0;
    public static final int OUTPUT_BUS_SLOT = 1;
    public static final int UP_CONNECTION_SLOT = 2;
    public static final int DOWN_CONNECTION_SLOT = 3;
    public static final int INPUT_INVENTORY_SLOT = 4;
    public static final int OUTPUT_INVENTORY_SLOT = 5;


    private int cooldownTime;


    public FlowCedarCasingBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.FLOW_CEDAR_CASING_BE.get(), pos, state);
    }

    private static boolean isInventorySlot(int slot) {
        return slot > DOWN_CONNECTION_SLOT;
    }

    @Override
    public void addBEBehaviours(List<IBEBehaviour> list) {
        list.add(new ManySlotItemHandlerBehaviour(this, 6) {
            @Override
            public boolean isItemAllowed(int pSlot, ItemStack pStack) {
                return switch (pSlot) {
                    case INPUT_BUS_SLOT -> pStack.is(TCItems.INPUT_BUS.get());
                    case OUTPUT_BUS_SLOT -> pStack.is(TCItems.OUTPUT_BUS.get());
                    case UP_CONNECTION_SLOT, DOWN_CONNECTION_SLOT -> pStack.is(TCItems.TECHNETIUM_ROD.get());
                    default -> true;
                };
            }

            @Override
            public int getLimitInSlot(int slot) {
                if (isInventorySlot(slot)) {
                    return 64;
                }
                return 1;
            }

            @Override
            public boolean allowExtract(int pSlot, ItemStack pStack, @Nullable Direction pDirection) {
                return pSlot == OUTPUT_INVENTORY_SLOT && (pDirection == null || pDirection.equals(Direction.DOWN));
            }

            @Override
            public boolean allowInsert(int pSlot, ItemStack pStack, @Nullable Direction pDirection) {
                return pDirection == null || (pSlot == INPUT_INVENTORY_SLOT && pDirection.equals(Direction.UP));
            }
        });
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        super.tick(pLevel,pPos,pState);
        --this.cooldownTime;
        if (this.notOnCooldown() && hasOutputBusConnection()) {
            this.setCooldown(0);
            tryMoveItems(pLevel, pPos, pState);
        }
    }

    private boolean hasOutputBusConnection() {
        ItemStackHandler iItemHandler = getItemHandler();
        return !iItemHandler.getStackInSlot(OUTPUT_BUS_SLOT).isEmpty()
                && !iItemHandler.getStackInSlot(DOWN_CONNECTION_SLOT).isEmpty();
    }

    private boolean hasInputBusConnection() {
        ItemStackHandler iItemHandler = getItemHandler();
        return !iItemHandler.getStackInSlot(INPUT_BUS_SLOT).isEmpty()
                && !iItemHandler.getStackInSlot(UP_CONNECTION_SLOT).isEmpty();
    }

    protected ItemStackHandler getItemHandler() {
        Optional<IItemHandler> capability = this.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
        if (capability.isPresent()) {
            return ((ItemStackHandler) capability.get());
        }
        throw new RuntimeException("Item handler not present: " + this);
    }

    private void tryMoveItems(Level pLevel, BlockPos pPos, BlockState pState) {
        if (!pLevel.isClientSide) {
            if (this.notOnCooldown()) {
                boolean flag = false;
                if (!this.getItemHandler().getStackInSlot(1).isEmpty()) {
                    flag = ejectItems(pLevel, pPos);
                }

                if (flag) {
                    this.setCooldown(8);
                    setChanged(pLevel, pPos, pState);
                }
            }

        }
    }

    private  boolean ejectItems(Level level, BlockPos blockPos) {
        if (this.vanillaInsertHook()) {
            return true;
        } else {
            Container container = getAttachedContainer(level, blockPos);
            if (container != null) {
                Direction direction = Direction.DOWN.getOpposite();
                if (!isFullContainer(container, direction)) {
                    ItemStackHandler itemStackHandler = this.getItemHandler();
                    if (!itemStackHandler.getStackInSlot(1).isEmpty()) {
                        ItemStack itemstack = itemStackHandler.getStackInSlot(1).copy();
                        ItemStack itemstack1 = addItem(container, itemStackHandler.extractItem(1, 1, false), direction);
                        if (itemstack1.isEmpty()) {
                            container.setChanged();
                            return true;
                        }
                        itemStackHandler.setStackInSlot(1, itemstack);
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
        ItemStackHandler itemStackHandler = this.getItemHandler();
        if (!isFull(itemStackHandler)) {
            if (!itemStackHandler.getStackInSlot(1).isEmpty()) {
                ItemStack originalSlotContents = itemStackHandler.getStackInSlot(1).copy();
                ItemStack insertStack = itemStackHandler.extractItem(1, 1, false);
                ItemStack remainder = putStackInInventoryAllSlots(itemStackHandler, insertStack);
                if (remainder.isEmpty()) {
                    return true;
                }
                itemStackHandler.setStackInSlot(1, originalSlotContents);
            }
        }
        return false;
        
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

    private boolean notOnCooldown() {
        return this.cooldownTime <= 0;
    }

    public void setCooldown(int pCooldownTime) {
        this.cooldownTime = pCooldownTime;
    }
}
