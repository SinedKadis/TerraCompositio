package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.sinedkadis.terracompositio.api.IHaveKnowledge;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEItemBehaviour;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEItemWordlyContainerBehaviour;
import net.sinedkadis.terracompositio.api.dummies.DummyBehaviour;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import net.sinedkadis.terracompositio.api.helpers.TooltipHelper;
import net.sinedkadis.terracompositio.config.TCCommonConfigs;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class TCCraftingBlockEntity extends TCBlockEntity implements WorldlyContainer, IHaveKnowledge {
    protected int progress = 0;
    protected int maxProgress;
    protected float tickCFECost;

    public TCCraftingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected void resetProgress() {
        progress = 0;
    }

    protected boolean hasProgressFinished() {
        return progress>=maxProgress;
    }

    private float partialCFE = 0;
    protected void consumeCFE() {
        int floorCFE = (int) Math.floor(tickCFECost);
        partialCFE += tickCFECost- floorCFE;
        int floorPart = (int) Math.floor(partialCFE);
        partialCFE = partialCFE - floorPart;
        this.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance).takeCFE(floorCFE+floorPart,false);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.putInt("flow_port_progress", progress);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        progress = pTag.getInt("flow_port_progress");
    }

    protected void increaseCraftingProgress() {
        progress++;
    }


    protected boolean sameItemInOutput(Item item) {
        return this.getItemHandler().getStackInSlot(getOutputSlotIndex()).isEmpty() || this.getItemHandler().getStackInSlot(getOutputSlotIndex()).is(item);
    }

    public int getOutputSlotIndex() {
        return getItemHandler().getSlots() - 1;
    }

    protected boolean enoughSpaceInOutput(int count) {
        return this.getItemHandler().getStackInSlot(getOutputSlotIndex()).getCount() + count
                <= Math.min(this.getItemHandler().getStackInSlot(getOutputSlotIndex()).getMaxStackSize(),
                getItemHandler().getSlotLimit(1));
    }

    protected void craftItem() {
    }
    protected Optional<?> getCurrentRecipe(){return Optional.empty();}
    protected boolean hasRecipe(){return false;}

    abstract protected IItemHandlerModifiable getItemHandler();

    public ItemStack getRenderStack() {
        for (int i = getItemHandler().getSlots() - 1; i >= 0; i--) {
            if (!getItemHandler().getStackInSlot(i).isEmpty()) {
                return getItemHandler().getStackInSlot(i);
            }
        }
        return ItemStack.EMPTY;
    }


    @Override
    public int[] getSlotsForFace(Direction pSide) {
        return getBehaviour().getSlotsForFace(pSide);
    }

    public IBEItemWordlyContainerBehaviour getBehaviour() {
        Set<IBEItemBehaviour> itemBehaviour = getItemBehaviours();
        return itemBehaviour.stream()
                .filter(IBEItemWordlyContainerBehaviour.class::isInstance)
                .map(IBEItemWordlyContainerBehaviour.class::cast)
                .findAny().orElse(DummyBehaviour.instance);
    }

    @Override
    public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
        return getBehaviour().canPlaceItemThroughFace(pIndex,pItemStack,pDirection);
    }

    @Override
    public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
        return getBehaviour().canTakeItemThroughFace(pIndex,pStack,pDirection);
    }

    @Override
    public int getContainerSize() {
        return getBehaviour().getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return getBehaviour().isEmpty();
    }

    @Override
    public ItemStack getItem(int pSlot) {
        return getBehaviour().getItem(pSlot);
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        return getBehaviour().removeItem(pSlot,pAmount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        return getBehaviour().removeItemNoUpdate(pSlot);
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {
        getBehaviour().setItem(pSlot,pStack);
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return getBehaviour().stillValid(pPlayer);
    }

    @Override
    public void clearContent() {
        getBehaviour().clearContent();
    }

    @Override
    public void collectKnowledgeData(CompoundTag data) {
        super.collectKnowledgeData(data);
        if (maxProgress == 0) return;

        float remaining = (maxProgress - progress) / 20f;
        data.putFloat(TooltipHelper.Keys.TIME_REMAINING.toData(), remaining);
        if (TCCommonConfigs.DEBUG.get()) {
            data.putInt(TooltipHelper.Keys.PROGRESS.toData(), progress);
            data.putInt(TooltipHelper.Keys.MAX_PROGRESS.toData(), maxProgress);
        }
        data.putFloat(TooltipHelper.Keys.CONSUME.toData(), tickCFECost * 20f);

    }

    @Override
    public void addTooltipLines(CompoundTag data, List<Component> tooltip, boolean isShifting) {//todo add fallback
        boolean added = false;
        TooltipHelper.addHeader(TooltipHelper.Headers.CRAFTING, tooltip);
        added |= TooltipHelper.addIfExist(TooltipHelper.Keys.TIME_REMAINING, TooltipHelper.Units.SECONDS, tooltip, data);
        added |= TooltipHelper.addIfExist(TooltipHelper.Keys.PROGRESS, TooltipHelper.Units.SECONDS, tooltip, data);
        added |= TooltipHelper.addIfExist(TooltipHelper.Keys.MAX_PROGRESS, TooltipHelper.Units.SECONDS, tooltip, data);
        added |= TooltipHelper.addIfExist(TooltipHelper.Keys.CONSUME, TooltipHelper.Units.CFE_SECOND, tooltip, data);
        if (!added) tooltip.remove(tooltip.size() - 1);
        super.addTooltipLines(data, tooltip, isShifting);
    }
}
