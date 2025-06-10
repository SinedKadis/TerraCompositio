package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.sinedkadis.terracompositio.util.ModItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

import java.util.List;

import static net.sinedkadis.terracompositio.block.custom.FlowCedarCasingBlock.isPortAttached;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ModItemIOCFEBlockEntity extends ModCFEBlockEntity implements WorldlyContainer {

    protected final ModItemStackHandler itemHandler = new ModItemStackHandler(2,this);

    protected static final int SLOT_INPUT = 0;
    protected static final int SLOT_OUTPUT = 1;
    protected int progress = 0;
    protected int maxProgress;
    protected float tickCFECost;
    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public ModItemIOCFEBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int maxCFE, int connectRange,BlockMode blockMode) {
        super(type, pos, state, maxCFE, connectRange,blockMode);
    }
    public ModItemIOCFEBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,BlockMode blockMode){
        this(type,pos,state,0,0,blockMode);
    }

    protected <T> @Nullable LazyOptional<T> getCap(@NotNull Capability<T> cap,@Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER && side != null){
            return lazyItemHandler.cast();
        }
        return null;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[]{0,1};
    }

    @Override
    public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
        if (direction != null) {
            return i == SLOT_INPUT && direction.equals(Direction.UP);
        }
        return i == SLOT_INPUT;
    }

    @Override
    public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
        return i == SLOT_OUTPUT && direction.equals(Direction.DOWN);
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return itemHandler.getStackInSlot(SLOT_INPUT).isEmpty()
                && itemHandler.getStackInSlot(SLOT_OUTPUT).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        if (-1 < slot && slot < itemHandler.getSlots())
            return itemHandler.getStackInSlot(slot);
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        return ContainerHelper.removeItem(List.of(
                itemHandler.getStackInSlot(SLOT_INPUT),
                itemHandler.getStackInSlot(SLOT_OUTPUT)), slot, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(List.of(
                itemHandler.getStackInSlot(SLOT_INPUT),
                itemHandler.getStackInSlot(SLOT_OUTPUT)), slot);
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        itemHandler.setStackInSlot(slot,itemStack);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        itemHandler.setStackInSlot(SLOT_INPUT,ItemStack.EMPTY);
        itemHandler.setStackInSlot(SLOT_OUTPUT,ItemStack.EMPTY);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(()-> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    public void drops(){
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++){
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        if (this.level != null) {
            Containers.dropContents(this.level, this.worldPosition,inventory);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        pTag.put("inventory", itemHandler.serializeNBT());
        pTag.putInt("progress", progress);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        itemHandler.deserializeNBT(pTag.getCompound("inventory"));
        progress = pTag.getInt("progress");
    }

    protected boolean enoughCFE() {
        return this.cfeContainer.getCFE() >= tickCFECost;
    }

    protected void resetProgress() {
        progress = 0;
    }

    protected boolean hasProgressFinished() {
        return progress>=maxProgress;
    }

    protected void consumeCFE() {
        this.cfeContainer.setCFE((int) (this.cfeContainer.getCFE()-tickCFECost));
    }

    protected void increaseCraftingProgress() {
        progress++;
    }


    protected boolean sameItemInOutput(Item item) {
        return this.itemHandler.getStackInSlot(SLOT_OUTPUT).isEmpty() || this.itemHandler.getStackInSlot(SLOT_OUTPUT).is(item);
    }

    protected boolean enoughSpaceInOutput(int count) {
        return this.itemHandler.getStackInSlot(SLOT_OUTPUT).getCount() + count <=this.itemHandler.getStackInSlot(SLOT_OUTPUT).getMaxStackSize();
    }

    public ItemStack getFirstSlot(){
        return this.itemHandler.getStackInSlot(SLOT_INPUT);
    }
    public ItemStack getLastSlot(){
        return this.itemHandler.getStackInSlot(itemHandler.getSlots()-1);
    }
    public void setSlotCount(int count){
        this.itemHandler.setSize(count);
    }

    public ItemStack insertItemStack(int slot, ItemStack stack){
        ItemStack itemStack;
        if (stack.getCount() == 1)
            itemStack = stack.copyWithCount(2);
        else
            itemStack = stack.copy();
        itemStack = this.itemHandler.insertItem(slot,itemStack,false);
        if (stack.getCount() == 1) {
            stack.shrink(1);
            return stack;
        }
        return itemStack;
        //return super.insertItem(slot,stack,false);
        //return this.itemHandler.insertItem(slot,stack,false);
    }
    public void setSlotEmpty(int slot){
        slot = Mth.clamp(0,itemHandler.getSlots()-1,slot);
        this.itemHandler.setStackInSlot(slot,ItemStack.EMPTY);

    }

    public ItemStack getRenderStack() {
        for (int i = itemHandler.getSlots()-1; i>=0; i--){
            if (!itemHandler.getStackInSlot(i).isEmpty()){
                return itemHandler.getStackInSlot(i);
            }
        }
        return ItemStack.EMPTY;
    }

    public ItemStack forceInsertItemStack(int slot, ItemStack stack) {
        return this.itemHandler.forceInsertItem(slot, this.itemHandler.forceInsertItem(slot, stack, false), false);
    }

    public int getSlotLimit(int slot) {
        return isPortAttached(this.getLevel(),this.getBlockState(),this.getBlockPos()) ? 1 : 64;
    }
}
