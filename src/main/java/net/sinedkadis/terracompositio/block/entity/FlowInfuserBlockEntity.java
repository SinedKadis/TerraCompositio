package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.sinedkadis.terracompositio.particle.ModParticles;
import net.sinedkadis.terracompositio.recipe.FlowInfusionRecipe;
import net.sinedkadis.terracompositio.recipe.FlowSaturationRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

import static net.sinedkadis.terracompositio.block.ModBlockStateProperties.INFUSED;

public class FlowInfuserBlockEntity extends ModCFEBlockEntity{
    private final ItemStackHandler itemHandler = new ItemStackHandler(2){
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };
    private static final int SLOT_INPUT = 0;
    private static final int SLOT_OUTPUT = 1;
    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress;
    private float tickCFECost;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public FlowInfuserBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.FLOW_INFUSER_BE.get(),pPos, pBlockState,100,5);
        this.data =new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex){
                    case 0 -> FlowInfuserBlockEntity.this.progress;
                    case 1 -> FlowInfuserBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex){
                    case 0 -> FlowInfuserBlockEntity.this.progress = pValue;
                    case 1 -> FlowInfuserBlockEntity.this.maxProgress = pValue;
                };
            }

            @Override
            public int getCount() {
                return 2;
            }
        };

    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER){
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap,side);
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
        Containers.dropContents(this.level, this.worldPosition,inventory);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.put("inventory", itemHandler.serializeNBT());
        pTag.putInt("flow_infuser_progress", progress);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        itemHandler.deserializeNBT(pTag.getCompound("inventory"));
        progress = pTag.getInt("flow_infuser_progress");
    }

    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (pState.getValue(INFUSED))
            super.tick(pLevel, pPos, pState);
        if(hasRecipe() && enoughCFE()){
            increaseCraftingProgress();
            CFE = (int) (CFE-tickCFECost);
            setChanged(pLevel, pPos, pState);
            if (!pLevel.isClientSide){
                ((ServerLevel) pLevel).sendParticles(ModParticles.FLOW_STILL_PARTICLE.get(),pPos.getX()+0.5D,pPos.getY()+0.5D,pPos.getZ()+0.5D,3,0,-0.1D,0,0.1D);
            }
            if(hasProgressFinished()){
                craftItem();
                resetProgress();
            }
        }else if(!hasRecipe()) {
            resetProgress();
        }
    }

    private boolean enoughCFE() {
        return CFE >= tickCFECost;
    }

    private void resetProgress() {
        progress = 0;
    }

    private void craftItem() {
        Optional<FlowInfusionRecipe> recipe = getCurrentRecipe();
        ItemStack result = recipe.get().getResultItem(null);
        this.itemHandler.extractItem(SLOT_INPUT,1,false);
        this.itemHandler.setStackInSlot(SLOT_OUTPUT, new ItemStack(result.getItem(),
                this.itemHandler.getStackInSlot(SLOT_OUTPUT).getCount()+result.getCount()));
    }

    private boolean hasProgressFinished() {
        return progress>=maxProgress;
    }

    private void increaseCraftingProgress() {
        progress++;
    }

    private boolean hasRecipe() {
        Optional<FlowInfusionRecipe> recipe = getCurrentRecipe();
        if (recipe.isEmpty()){
            return false;
        }
        ItemStack result = recipe.get().getResultItem(null);
        maxProgress = recipe.get().getTicks();
        tickCFECost = recipe.get().getCFETick();
        return enoughSpaceInOutput(result.getCount())&& sameItemInOutput(result.getItem());
    }

    private Optional<FlowInfusionRecipe> getCurrentRecipe() {
        SimpleContainer inventory = new SimpleContainer(this.itemHandler.getSlots());
        for(int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, this.itemHandler.getStackInSlot(i));
        }

        return this.level.getRecipeManager().getRecipeFor(FlowInfusionRecipe.Type.INSTANCE, inventory, level);
    }

    private boolean sameItemInOutput(Item item) {
        return this.itemHandler.getStackInSlot(SLOT_OUTPUT).isEmpty() || this.itemHandler.getStackInSlot(SLOT_OUTPUT).is(item);
    }

    private boolean enoughSpaceInOutput(int count) {
        return this.itemHandler.getStackInSlot(SLOT_OUTPUT).getCount() + count <=this.itemHandler.getStackInSlot(SLOT_OUTPUT).getMaxStackSize();
    }

    public ItemStack getInputSlot(){
        return this.itemHandler.getStackInSlot(SLOT_INPUT);
    }
    public ItemStack getOutputSlot(){
        return this.itemHandler.getStackInSlot(SLOT_OUTPUT);
    }

    public ItemStack addItemInSlot(int Slot, ItemStack item, int count){
        this.itemHandler.setStackInSlot(Slot,new ItemStack(item.getItem(),
                this.itemHandler.getStackInSlot(Slot).getCount()+count));
        item.setCount(item.getCount()-count);
        return new ItemStack(item.getItem(),item.getCount());


    }
    public void setSlotEmpty(int Slot){
        this.itemHandler.setStackInSlot(Slot,/*new ItemStack(ModItems.ITEM_PLACEHOLDER.get(),1)*/ItemStack.EMPTY);

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
