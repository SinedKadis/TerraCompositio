package net.sinedkadis.terracompositio.block.behaviours;

import lombok.Data;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEItemBehaviour;
import net.sinedkadis.terracompositio.block.entity.MatterInfuserIOBlockEntity;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

import javax.annotation.ParametersAreNonnullByDefault;

@Data
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TwoSlotItemHandlerBehaviour implements IBEItemBehaviour, WorldlyContainer {
    protected static final int INPUT = 0;
    protected static final int OUTPUT = 1;


    private final TCBlockEntity blockEntity;

    protected SlotSensitiveItemStackHandler itemHandler = new SlotSensitiveItemStackHandler(2);

    public int getLimitInSlot(int slot) {
        return 64;
    }

    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();


    public TwoSlotItemHandlerBehaviour(TCBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public void tick() {

    }

    @Override
    public void onChunkLoad() {
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public @Nullable LazyOptional<?> getCapability(Capability<?> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER){
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
        tag.put("itemHandler",itemHandler.serializeNBT()) ;
    }

    @Override
    public void onLoad(CompoundTag tag) {
        itemHandler.deserializeNBT(tag.getCompound("itemHandler"));
    }

    @Override
    public void onAppendServerData(CompoundTag compoundTag) {
        ItemStack inputSlot = getItemHandler().getStackInSlot(0);
        ItemStack outputSlot = getItemHandler().getStackInSlot(1);

        if (blockEntity instanceof MatterInfuserIOBlockEntity) {
            return;
        }
        compoundTag.putInt("input_c", inputSlot.getCount());
        compoundTag.putInt("input", Item.getId(inputSlot.getItem()));
        if (inputSlot != outputSlot) {
            compoundTag.putInt("output_c", outputSlot.getCount());
            compoundTag.putInt("output", Item.getId(outputSlot.getItem()));
        }
    }

    @Override
    public void onAppendTooltip(ITooltip iTooltip, CompoundTag serverData, IPluginConfig iPluginConfig) {
        if (serverData.contains("input_c")) {
            int inputCount = serverData.getInt("input_c");
            if (inputCount > 0) {
                //iTooltip.add(Component.empty());
                iTooltip.add(Component.translatable("block.terracompositio.item_io." + "input").append(" "));
                if (serverData.contains("input")) {
                    Item input = Item.byId(serverData.getInt("input"));
                    IElementHelper elements = IElementHelper.get();
                    IElement icon = elements.item(new ItemStack(input),0.7f)
                            .translate(new Vec2(-2,-4))
                            ;
                    iTooltip.add(icon);
                    iTooltip.append(Component.literal(inputCount +"x "));
                    iTooltip.append(Component.translatable(input.getDescriptionId()));
                }
                //iTooltip.add(Component.empty());
            }

        }
        if (serverData.contains("output_c")) {
            int outputCount = serverData.getInt("output_c");
            if (outputCount > 0) {
                iTooltip.add(Component.translatable("block.terracompositio.item_io." + "output").append(" "));
                if (serverData.contains("output")) {
                    Item output = Item.byId(serverData.getInt("output"));
                    IElementHelper elements = IElementHelper.get();
                    IElement icon = elements.item(new ItemStack(output), 0.7f)
                            .translate(new Vec2(-2,-4))
                            ;
                    iTooltip.add(icon);
                    iTooltip.append(Component.literal(outputCount +"x "));
                    iTooltip.append(Component.translatable(output.getDescriptionId()));
                }
                //iTooltip.add(Component.empty());
            }
        }
    }

    @Override
    public InteractionResult onUse(Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        Level level = blockEntity.getLevel();
        if (level == null) return InteractionResult.PASS;

        BlockPos blockPos = blockEntity.getBlockPos();

        ItemStack itemInHand = pPlayer.getItemInHand(pHand);

        ItemStackHandler itemHandler = getItemHandler();

        ItemStack outputSlot = itemHandler.getStackInSlot(OUTPUT);
        ItemStack inputSlot = itemHandler.getStackInSlot(INPUT);


        InteractionResult interactionResult = InteractionResult.SUCCESS;
        if (!outputSlot.isEmpty() && allowExtract(OUTPUT, outputSlot, pHit.getDirection(), true)) {
            ItemStack extracted = itemHandler.extractItem(OUTPUT, 64, false);
            TCUtil.addOrDropToPlayer(pPlayer, extracted);
            level.playSound(pPlayer, blockPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS);
            return interactionResult;
        }
        if (!itemInHand.isEmpty() && allowInsert(INPUT, itemInHand, pHit.getDirection(), true)) {
            ItemStack left = itemHandler.insertItem(INPUT, itemInHand.copy(), false);
            pPlayer.setItemInHand(pHand,left);
            level.playSound(pPlayer,blockPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS);
            return interactionResult;
        }
        if (!inputSlot.isEmpty() && allowExtract(INPUT, inputSlot, pHit.getDirection(), true)) {
            TCUtil.addOrDropToPlayer(pPlayer, inputSlot);
            itemHandler.setStackInSlot(INPUT, ItemStack.EMPTY);
            level.playSound(pPlayer, blockPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS);
            return interactionResult;
        }

        return InteractionResult.PASS;
    }


    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[]{0,1};
    }

    @Override
    public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
        if (getInputSlot().getCount() + getOutputSlot().getCount()>1) return false;
        if (pDirection != null) {
            return pIndex == INPUT && pDirection.equals(Direction.UP);
        }
        return pIndex == INPUT;
    }

    @Override
    public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
        return pIndex == OUTPUT && pDirection.equals(Direction.DOWN);
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return itemHandler.getStackInSlot(INPUT).isEmpty()
                && itemHandler.getStackInSlot(OUTPUT).isEmpty();
    }

    @Override
    public ItemStack getItem(int pSlot) {
        return itemHandler.getStackInSlot(pSlot);
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        return itemHandler.extractItem(pSlot,pAmount,false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        ItemStack stackInSlot = itemHandler.getStackInSlot(pSlot);
        itemHandler.setStackInSlot(pSlot,ItemStack.EMPTY);
        return stackInSlot;
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {
        itemHandler.setStackInSlot(pSlot,pStack);
    }

    @Override
    public void setChanged() {
        blockEntity.setChanged();
        Level level = blockEntity.getLevel();
        if (level != null) {
            level.sendBlockUpdated(blockEntity.getBlockPos(),blockEntity.getBlockState(),blockEntity.getBlockState(),3);
        }
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return Container.stillValidBlockEntity(blockEntity, pPlayer);
    }

    @Override
    public void clearContent() {
        itemHandler.setStackInSlot(INPUT,ItemStack.EMPTY);
        itemHandler.setStackInSlot(OUTPUT,ItemStack.EMPTY);
    }

    public ItemStack getInputSlot(){
        return this.itemHandler.getStackInSlot(INPUT);
    }
    public ItemStack getOutputSlot(){
        return this.itemHandler.getStackInSlot(OUTPUT);
    }

    @Override
    public boolean allowExtract(int pSlot, ItemStack pStack, @Nullable Direction pDirection, boolean manual) {
        return true;
    }

    @Override
    public boolean allowInsert(int pSlot, ItemStack pStack, @Nullable Direction pDirection, boolean manual) {
        return true;
    }

    public class SlotSensitiveItemStackHandler extends ItemStackHandler {


        public SlotSensitiveItemStackHandler(int size) {
            super(size);
        }

        @Override
        public int getSlotLimit(int slot) {
            return getLimitInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != INPUT) return stack;
            ItemStack itemStack = super.insertItem(slot, stack, simulate);
            setChanged();
            return itemStack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != OUTPUT) return ItemStack.EMPTY;
            return super.extractItem(slot, amount, simulate);
        }

        public ItemStack forceInsertItem(int slot, ItemStack stack, boolean simulate) {
            return super.insertItem(slot, stack, simulate);
        }

    }
}
