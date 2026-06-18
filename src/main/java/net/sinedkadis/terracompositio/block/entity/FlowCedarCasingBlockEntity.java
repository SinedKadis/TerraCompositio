package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.block.behaviours.ItemHandlerBehaviour;
import net.sinedkadis.terracompositio.block.behaviours.ItemStateHolderBehaviour;
import net.sinedkadis.terracompositio.block.custom.MatterInfuserBaseEntityBlock;
import net.sinedkadis.terracompositio.block.custom.MatterInfuserIOBlock;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCItems;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static net.minecraft.world.level.block.entity.HopperBlockEntity.getContainerAt;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FlowCedarCasingBlockEntity extends TCCraftingBlockEntity{

    public static final int INPUT_BUS_SLOT = 0;
    public static final int OUTPUT_BUS_SLOT = 1;
    public static final int UP_CONNECTION_SLOT = 2;
    public static final int DOWN_CONNECTION_SLOT = 3;
    public static final int INPUT_INVENTORY_SLOT = 0;
    public static final int OUTPUT_INVENTORY_SLOT = 1;


    private int cooldownTime;


    public FlowCedarCasingBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.FLOW_CEDAR_CASING_BE.get(), pos, state);
    }

    @Override
    public void addBEBehaviours(List<IBEBehaviour> list) {
        list.add(new ItemHandlerBehaviour(this, 2) {

            @Override
            public boolean allowExtract(int pSlot, ItemStack pStack, @Nullable Direction pDirection, boolean manual) {
                if (noOutputBus()) return false;
                return pSlot == OUTPUT_INVENTORY_SLOT && (pDirection == null || pDirection.equals(Direction.DOWN));
            }

            @Override
            public boolean allowInsert(int pSlot, ItemStack pStack, @Nullable Direction pDirection, boolean manual) {
                if (noInputBus()) return false;
                boolean noDir = pDirection == null;
                boolean isInputSlot = pSlot == INPUT_INVENTORY_SLOT;
                boolean directionIsUp = !noDir && pDirection.equals(Direction.UP);

                return isInputSlot && directionIsUp;
            }
        });
        list.add(new ItemStateHolderBehaviour(this, 4) {
            @Override
            public boolean allowInsert(int pSlot, ItemStack pStack, @Nullable Direction pDirection, boolean manualInsertion) {
                Level level = FlowCedarCasingBlockEntity.this.level;
                if (level == null) return false;

                Block blockRelative = level.getBlockState(worldPosition.relative(attachedDir())).getBlock();
                boolean isMIConnected = attachedDir().getAxis().isHorizontal()
                        && blockRelative instanceof MatterInfuserBaseEntityBlock;

                boolean isUpConnectionAllow = noInputBus() && isMIConnected && pSlot == UP_CONNECTION_SLOT;

                boolean isMIUNIT = blockRelative instanceof MatterInfuserIOBlock;
                boolean isDownConnectionAllow = !noOutputBus() && isMIConnected && isMIUNIT && pSlot == DOWN_CONNECTION_SLOT;
                boolean isBus = pSlot <= OUTPUT_BUS_SLOT;

                boolean allowUtility = isBus || isUpConnectionAllow || isDownConnectionAllow;
                boolean superAllow = super.allowInsert(pSlot, pStack, pDirection, manualInsertion);
                boolean allow = allowUtility && superAllow;


                allow &= switch (pSlot) {
                    case INPUT_BUS_SLOT -> pStack.is(TCItems.INPUT_BUS.get());
                    case OUTPUT_BUS_SLOT -> pStack.is(TCItems.OUTPUT_BUS.get());
                    case UP_CONNECTION_SLOT, DOWN_CONNECTION_SLOT -> pStack.is(TCItems.INFUSED_IRON_ROD.get());
                    default -> true;
                };


                return allow;
            }
        });

    }

    public boolean noInputBus() {
        return this.getCapability(TCCapabilities.ITEM_STATE_HOLDER).orElse(EmptyHandler.INSTANCE).getStackInSlot(INPUT_BUS_SLOT).isEmpty();
    }

    public boolean noOutputBus() {
        return this.getCapability(TCCapabilities.ITEM_STATE_HOLDER).orElse(EmptyHandler.INSTANCE).getStackInSlot(OUTPUT_BUS_SLOT).isEmpty();
    }

    public Direction attachedDir() {
        return getBlockState().getValue(BlockStateProperties.FACING);
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
        IItemHandlerModifiable iItemHandler = getItemHandler();
        return !iItemHandler.getStackInSlot(OUTPUT_BUS_SLOT).isEmpty()
                && !iItemHandler.getStackInSlot(DOWN_CONNECTION_SLOT).isEmpty();
    }

    protected IItemHandlerModifiable getItemHandler() {
        Optional<IItemHandler> capability = this.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
        if (capability.isPresent()) {
            return ((ItemStackHandler) capability.get());
        }
        return (IItemHandlerModifiable) EmptyHandler.INSTANCE;
        //throw new RuntimeException("Item handler not present: " + this);
    }

    private void tryMoveItems(Level pLevel, BlockPos pPos, BlockState pState) {
        if (!pLevel.isClientSide) {
            if (this.notOnCooldown()) {
                if (!this.getItemHandler().getStackInSlot(OUTPUT_INVENTORY_SLOT).isEmpty()) {
                    ejectItems(pLevel, pPos);
                }

                this.setCooldown(8);
                setChanged(pLevel, pPos, pState);
            }
        }
    }

    private void ejectItems(Level level, BlockPos blockPos) {
        Container container = getAttachedContainer(level, blockPos);
        if (container != null) {
            Direction direction = Direction.DOWN.getOpposite();
            if (!isFullContainer(container, direction)) {
                IItemHandlerModifiable itemStackHandler = this.getItemHandler();
                if (!itemStackHandler.getStackInSlot(1).isEmpty()) {
                    ItemStack itemstack = itemStackHandler.getStackInSlot(1).copy();
                    ItemStack itemstack1 = addItem(container, itemStackHandler.extractItem(1, 1, false), direction);
                    if (itemstack1.isEmpty()) {
                        container.setChanged();
                        return;
                    }
                    itemStackHandler.setStackInSlot(1, itemstack);
                }
            }
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

    @Override
    public ItemStack getRenderStack() {
        return getItemHandler().getStackInSlot(INPUT_INVENTORY_SLOT);
    }

    private boolean notOnCooldown() {
        return this.cooldownTime <= 0;
    }

    public void setCooldown(int pCooldownTime) {
        this.cooldownTime = pCooldownTime;
    }
}
