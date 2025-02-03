package net.sinedkadis.terracompositio.block.entity;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.sinedkadis.terracompositio.recipe.FlowSaturationRecipe;
import net.sinedkadis.terracompositio.screen.FlowBlockPortMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Optional;

import static net.sinedkadis.terracompositio.block.ModBlockStateProperties.INFUSED;

public class FlowPortBlockEntity extends BlockEntity implements MenuProvider {
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
    private int maxProgress = 78;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private int tickForSound = 0;


    public FlowPortBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.FLOW_PORT_BE.get(),pPos, pBlockState);
        this.data =new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex){
                    case 0 -> FlowPortBlockEntity.this.progress;
                    case 1 -> FlowPortBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex){
                    case 0 -> FlowPortBlockEntity.this.progress = pValue;
                    case 1 -> FlowPortBlockEntity.this.maxProgress = pValue;
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
        pTag.putInt("flow_port_progress", progress);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        itemHandler.deserializeNBT(pTag.getCompound("inventory"));
        progress = pTag.getInt("flow_port_progress");
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        //boolean hasRecipe = hasRecipe();
        //LOGGER.debug("Recipe found("+hasRecipe+")");
        if (pState.getValue(INFUSED)) {
            if (hasRecipe()) {
                tickForSound++;
                if (tickForSound == 5) {
                    tickForSound = 0;
                    pLevel.playSound(null, pPos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS);
                }
                increaseCraftingProgress();
                setChanged(pLevel, pPos, pState);
                if (hasProgressFinished()) {
                    craftItem();

                    resetProgress();
                }
            } else {
                resetProgress();
            }
        }
    }

    private void resetProgress() {
        progress = 0;
    }

    private void craftItem() {
        Optional<FlowSaturationRecipe> recipe = getCurrentRecipe();
        ItemStack result = recipe.get().getResultItem(null);
        //LOGGER.debug("Item crafted");
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
        Optional<FlowSaturationRecipe> recipe = getCurrentRecipe();
        if (recipe.isEmpty()){
            return false;
        }
        ItemStack result = recipe.get().getResultItem(null);
        return enoughSpaceInOutput(result.getCount())&& sameItemInOutput(result.getItem());
    }

    private Optional<FlowSaturationRecipe> getCurrentRecipe() {
        SimpleContainer inventory = new SimpleContainer(this.itemHandler.getSlots());
        for(int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, this.itemHandler.getStackInSlot(i));
        }

        return this.level.getRecipeManager().getRecipeFor(FlowSaturationRecipe.Type.INSTANCE, inventory, level);
    }

    private boolean sameItemInOutput(Item item) {
        return this.itemHandler.getStackInSlot(SLOT_OUTPUT).isEmpty() || this.itemHandler.getStackInSlot(SLOT_OUTPUT).is(item);
    }

    private boolean enoughSpaceInOutput(int count) {
        return this.itemHandler.getStackInSlot(SLOT_OUTPUT).getCount() + count <=this.itemHandler.getStackInSlot(SLOT_OUTPUT).getMaxStackSize();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.terracompositio.flow_port");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new FlowBlockPortMenu(pContainerId,pPlayerInventory,this,this.data);
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
        if(itemHandler.getStackInSlot(SLOT_OUTPUT).isEmpty()) {
            /*if(itemHandler.getStackInSlot(SLOT_INPUT).isEmpty()){
                return new ItemStack(Items.AIR,1);
            }*/
            return itemHandler.getStackInSlot(SLOT_INPUT);
        } else {
            return itemHandler.getStackInSlot(SLOT_OUTPUT);
        }
    }
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }
    //public void setSlotOutput(ItemStack item){this.itemHandler.setStackInSlot(SLOT_OUTPUT,item);}

    //public void setItem(ItemStack pStack) {this.setItem(pStack, true);}

    /*public void setItem(ItemStack pStack, boolean pUpdateNeighbours) {
        if (!pStack.isEmpty()) {
            pStack = pStack.copyWithCount(1);
        }

        //this.onItemChanged(pStack);
        this.itemHandler.insertItem(SLOT_INPUT,pStack,true);
        if (!pStack.isEmpty()) {
            level().playSound(,SoundEvents.ITEM_FRAME_ADD_ITEM);
        }

        if (pUpdateNeighbours && this.pos != null) {
            this.level().updateNeighbourForOutputSignal(this.pos, Blocks.AIR);
        }

    }*/

}
