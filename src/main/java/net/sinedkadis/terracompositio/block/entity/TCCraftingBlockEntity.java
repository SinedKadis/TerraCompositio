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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.sinedkadis.terracompositio.api.IHaveKnowledge;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.dummies.DummyECFHandler;
import net.sinedkadis.terracompositio.api.helpers.TooltipHelper;
import net.sinedkadis.terracompositio.api.registries.TCCapabilities;
import net.sinedkadis.terracompositio.config.TCCommonConfigs;
import net.sinedkadis.terracompositio.util.behaviors.DummyBehaviour;
import net.sinedkadis.terracompositio.util.behaviors.blockentity.IBEItemBehaviour;
import net.sinedkadis.terracompositio.util.behaviors.blockentity.IBEItemWordlyContainerBehaviour;
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
    protected float tickECFCost;
    protected CraftException craftException = CraftException.EMPTY;

    public TCCraftingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected void resetProgress() {
        progress = 0;
    }

    protected boolean hasProgressFinished() {
        return progress>=maxProgress;
    }

    private float partialECF = 0;

    protected void consumeECF() {
        int floorECF = (int) Math.floor(tickECFCost);
        partialECF += tickECFCost - floorECF;
        int floorPart = (int) Math.floor(partialECF);
        partialECF = partialECF - floorPart;
        this.getCapability(TCCapabilities.ECF).orElse(DummyECFHandler.instance).takeECF(floorECF + floorPart, false);
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
        boolean toReturn = this.getItemHandler().getStackInSlot(getOutputSlotIndex()).isEmpty() || this.getItemHandler().getStackInSlot(getOutputSlotIndex()).is(item);
        checkCraftException(toReturn, CraftException.NO_SPACE);
        return toReturn;
    }

    public int getOutputSlotIndex() {
        return getItemHandler().getSlots() - 1;
    }

    protected boolean enoughSpaceInOutput(int count) {
        boolean toReturn = this.getItemHandler().getStackInSlot(getOutputSlotIndex()).getCount() + count
                <= Math.min(this.getItemHandler().getStackInSlot(getOutputSlotIndex()).getMaxStackSize(),
                getItemHandler().getSlotLimit(1));
        checkCraftException(toReturn, CraftException.NO_SPACE);
        return toReturn;
    }

    public void checkCraftException(boolean toReturn, CraftException exception) {
        if (!toReturn)
            craftException = exception;
        if (toReturn && craftException.equals(exception))
            craftException = CraftException.EMPTY;
    }

    protected void playSoundIfNeeded(Level level, BlockPos pos) {

    }

    protected void craftItem() {
    }
    protected Optional<?> getCurrentRecipe(){return Optional.empty();}

    public boolean enoughECF() {
        int ecf = getECF();
        boolean toReturn = ecf > tickECFCost;
        checkCraftException(toReturn, CraftException.NO_ECF);
        return toReturn;
    }

    abstract protected int getECF();

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
        if (!craftException.equals(CraftException.EMPTY))
            data.putString(TooltipHelper.Keys.CRAFT_EXCEPTION.toData(), craftException.name());
        if (maxProgress == 0) return;

        float remaining = (maxProgress - progress) / 20f;
        if (hasRecipe()) {
            data.putFloat(TooltipHelper.Keys.TIME_REMAINING.toData(), remaining);
            data.putFloat(TooltipHelper.Keys.CONSUME.toData(), tickECFCost * 20f);
        }

        if (TCCommonConfigs.DEBUG.get()) {
            data.putInt(TooltipHelper.Keys.PROGRESS.toData(), progress);
            data.putInt(TooltipHelper.Keys.MAX_PROGRESS.toData(), maxProgress);
        }

    }

    @Override
    public void addTooltipLines(CompoundTag data, List<Component> tooltip, boolean isShifting) {

        TooltipHelper.addWithHeader(TooltipHelper.Headers.CRAFTING, tooltip, t -> {
            TooltipHelper.addIfExist(TooltipHelper.Keys.TIME_REMAINING, TooltipHelper.Units.SECONDS, t, data);
            TooltipHelper.addIfExist(TooltipHelper.Keys.PROGRESS, TooltipHelper.Units.SECONDS, t, data);
            TooltipHelper.addIfExist(TooltipHelper.Keys.MAX_PROGRESS, TooltipHelper.Units.SECONDS, t, data);
            TooltipHelper.addIfExist(TooltipHelper.Keys.CONSUME, TooltipHelper.Units.ECF_SECOND, t, data);
            if (data.contains(TooltipHelper.Keys.CRAFT_EXCEPTION.toData())) {
                TooltipHelper.addWithNoArg(
                        TooltipHelper.Keys.CRAFT_EXCEPTION,
                        Enum.valueOf(CraftException.class, data.getString(TooltipHelper.Keys.CRAFT_EXCEPTION.toData())),
                        t
                );
            }
        });


        super.addTooltipLines(data, tooltip, isShifting);
    }

    public enum CraftException implements TooltipHelper.ICustomUnit {
        NO_ECF, NO_SPACE, EMPTY;

        @Override
        public String getModID() {
            return TerraCompositioAPI.MOD_ID;
        }
    }
}
