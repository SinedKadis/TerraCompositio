package net.sinedkadis.terracompositio.block.behaviours;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.sinedkadis.terracompositio.api.helpers.ItemHelper;
import net.sinedkadis.terracompositio.api.helpers.PlayerHelper;
import net.sinedkadis.terracompositio.api.registries.TCCapabilities;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.util.behaviors.blockentity.IBEItemBehaviour;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

import static net.sinedkadis.terracompositio.block.behaviours.ItemHandlerBehaviour.hasSpace;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemStateHolderBehaviour implements IBEItemBehaviour {
    private final TCBlockEntity blockEntity;
    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    public ItemStateHolderBehaviour(TCBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }    protected ItemStackHandler itemHandler = new ItemStackHandler() {
        @Override
        public int getSlotLimit(int slot) {
            return getLimitInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!allowInsert(slot, stack, null, true) && !simulate) return stack;
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!allowExtract(slot, getStackInSlot(slot), null, true) && !simulate) return ItemStack.EMPTY;
            return super.extractItem(slot, amount, simulate);
        }
    };

    public ItemStateHolderBehaviour(TCBlockEntity blockEntity, int slotCount) {
        this.blockEntity = blockEntity;
        this.itemHandler.setSize(slotCount);
    }

    @Override
    public InteractionResult onUse(Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        Level level = blockEntity.getLevel();
        if (level == null) return InteractionResult.PASS;

        BlockPos blockPos = blockEntity.getBlockPos();

        ItemStack itemInHand = pPlayer.getItemInHand(pHand);

        IItemHandlerModifiable itemHandler = getItemHandler();

        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack slot = itemHandler.getStackInSlot(i);

            if (!slot.isEmpty() && allowExtract(i, slot, pHit.getDirection(), true)) {
                ItemStack extracted = itemHandler.extractItem(i, 64, false);
                PlayerHelper.addOrDropToPlayer(pPlayer, extracted);
                level.playSound(pPlayer, blockPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS);
                return InteractionResult.SUCCESS;
            }
            if (!itemInHand.isEmpty() && allowInsert(i, itemInHand, pHit.getDirection(), true) && hasSpace(itemHandler, i)) {
                ItemStack left = itemHandler.insertItem(i, itemInHand.copy(), false);
                pPlayer.setItemInHand(pHand, left);
                level.playSound(pPlayer, blockPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS);
                return InteractionResult.SUCCESS;
            }
        }


        return InteractionResult.PASS;
    }

    @Override
    public IItemHandlerModifiable getItemHandler() {
        return itemHandler;
    }

    @Override
    public boolean allowExtract(int pSlot, ItemStack pStack, @Nullable Direction pDirection, boolean manualExtraction) {
        return false;
    }

    @Override
    public boolean allowInsert(int pSlot, ItemStack pStack, @Nullable Direction pDirection, boolean manualInsertion) {
        return itemHandler.getStackInSlot(pSlot).isEmpty();
    }

    public int getLimitInSlot(int slot) {
        return 64;
    }

    @Override
    public void tick() {

    }

    @Override
    public void onChunkLoad() {
        lazyItemHandler = LazyOptional.of(this::getItemHandler);
    }

    @Override
    public @Nullable LazyOptional<?> getCapability(Capability<?> cap, @Nullable Direction side) {
        if (cap == TCCapabilities.ITEM_STATE_HOLDER) return lazyItemHandler.cast();
        return null;
    }

    @Override
    public void onRemoved() {
        ItemHelper.dropContents(blockEntity, TCCapabilities.ITEM_STATE_HOLDER);
    }

    @Override
    public void onInvalidateCaps() {
        lazyItemHandler.invalidate();
    }

    @Override
    public void onSave(CompoundTag compoundTag) {
        compoundTag.put("item_state_holder", itemHandler.serializeNBT());
    }

    @Override
    public void onLoad(CompoundTag compoundTag) {
        itemHandler.deserializeNBT(compoundTag.getCompound("item_state_holder"));
    }

    @Override
    public <T extends BlockEntity> T getBlockEntity() {
        //noinspection unchecked
        return (T) blockEntity;
    }




}
