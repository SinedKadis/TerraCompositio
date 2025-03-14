package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.network.packets.SyncItemHandlerPacket;
import net.sinedkadis.terracompositio.recipe.FlowInfusionRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class ModItemIOCFEBlockEntity extends ModCFEBlockEntity{
    protected final ItemStackHandler itemHandler = new ItemStackHandler(2){
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide() && sendPacketOnContentChange()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                ItemStack itemStack = itemHandler.getStackInSlot(slot);
                TerraCompositio.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(getBlockPos())),
                        new SyncItemHandlerPacket(getBlockPos(), itemStack));
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    protected boolean sendPacketOnContentChange() {
        return false;
    }

    protected static final int SLOT_INPUT = 0;
    protected static final int SLOT_OUTPUT = 1;
    protected final ContainerData data;
    protected int progress = 0;
    protected int maxProgress;
    protected float tickCFECost;
    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public ModItemIOCFEBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int maxCFE, int connectRange) {
        super(type, pos, state, maxCFE, connectRange);
        this.data =new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex){
                    case 0 -> ModItemIOCFEBlockEntity.this.progress;
                    case 1 -> ModItemIOCFEBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex){
                    case 0 -> ModItemIOCFEBlockEntity.this.progress = pValue;
                    case 1 -> ModItemIOCFEBlockEntity.this.maxProgress = pValue;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }
    public ModItemIOCFEBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state){
        this(type,pos,state,0,0);
    }

    protected <T> @Nullable LazyOptional<T> getCap(@NotNull Capability<T> cap,@Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER){
            return lazyItemHandler.cast();
        }
        return null;
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
        assert this.level != null;
        Containers.dropContents(this.level, this.worldPosition,inventory);
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
        return CFE >= tickCFECost;
    }

    protected void resetProgress() {
        progress = 0;
    }

    protected void craftItem() {

    }

    protected boolean hasProgressFinished() {
        return progress>=maxProgress;
    }

    protected void increaseCraftingProgress() {
        progress++;
    }

    protected boolean hasRecipe() {
        Optional<FlowInfusionRecipe> recipe = getCurrentRecipe();
        if (recipe.isEmpty()){
            return false;
        }
        ItemStack result = recipe.get().getResultItem(null);
        maxProgress = recipe.get().getTicks();
        tickCFECost = recipe.get().getCFETick();
        return enoughSpaceInOutput(result.getCount())&& sameItemInOutput(result.getItem());
    }

    protected Optional<FlowInfusionRecipe> getCurrentRecipe() {
        SimpleContainer inventory = new SimpleContainer(this.itemHandler.getSlots());
        for(int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, this.itemHandler.getStackInSlot(i));
        }

        assert this.level != null;
        return this.level.getRecipeManager().getRecipeFor(FlowInfusionRecipe.Type.INSTANCE, inventory, level);
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

    public ItemStack insertItemStack(int slot, ItemStack item){
        return this.itemHandler.insertItem(slot,item, false);
    }
    public void setSlotEmpty(int slot){
        slot = Mth.clamp(0,itemHandler.getSlots()-1,slot);
        this.itemHandler.setStackInSlot(slot,/*new ItemStack(ModItems.ITEM_PLACEHOLDER.get(),1)*/ItemStack.EMPTY);

    }

    public ItemStack getRenderStack() {
        if(itemHandler.getStackInSlot(SLOT_INPUT).isEmpty()) {
            /*if(itemHandler.getStackInSlot(SLOT_INPUT).isEmpty()){
                return new ItemStack(Items.AIR,1);
            }*/
            return itemHandler.getStackInSlot(SLOT_OUTPUT);
        } else {
            return itemHandler.getStackInSlot(SLOT_INPUT);
        }
    }
}
