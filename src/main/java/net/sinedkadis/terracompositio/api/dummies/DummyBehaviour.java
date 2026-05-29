package net.sinedkadis.terracompositio.api.dummies;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBECFEBehaviour;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEItemBehaviour;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DummyBehaviour implements IBEBehaviour, IBEItemBehaviour, IBECFEBehaviour {
    public static final DummyBehaviour instance = new DummyBehaviour();

    @Override
    public void tick() {

    }

    @Override
    public void onChunkLoad() {

    }

    @Override
    public @Nullable LazyOptional<?> getCapability(Capability<?> cap, @Nullable Direction side) {
        return null;
    }

    @Override
    public void onRemoved() {

    }

    @Override
    public void onInvalidateCaps() {

    }

    @Override
    public void onSave(CompoundTag compoundTag) {

    }

    @Override
    public void onLoad(CompoundTag compoundTag) {

    }

    @Override
    public <T extends BlockEntity> @Nullable T getBlockEntity() {
        return null;
    }

    @Override
    public IItemHandlerModifiable getItemHandler() {
        return (IItemHandlerModifiable) EmptyHandler.INSTANCE;
    }

    @Override
    public boolean allowExtract(int pSlot, ItemStack pStack, @Nullable Direction pDirection, boolean manualExtraction) {
        return false;
    }

    @Override
    public boolean allowInsert(int pSlot, ItemStack pStack, @Nullable Direction pDirection, boolean manualInsertion) {
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction pSide) {
        return new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
        return false;
    }

    @Override
    public int getContainerSize() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getItem(int pSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {

    }

    @Override
    public void setChanged() {

    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return false;
    }

    @Override
    public void clearContent() {

    }

    @Override
    public int getRange() {
        return 0;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public ICFEHandler getMainHandler() {
        return DummyCFEHandler.instance;
    }

    @Override
    public void updateIfScheduled() {

    }

    @Override
    public void scheduleMemberUpdate() {

    }
}
