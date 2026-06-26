package net.sinedkadis.terracompositio.api.dummies;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEECFBehaviour;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEItemWordlyContainerBehaviour;
import net.sinedkadis.terracompositio.api.networks.ecf.IECFHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DummyBehaviour implements IBEBehaviour, IBEItemWordlyContainerBehaviour, IBEECFBehaviour {
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
    public int getRange() {
        return 0;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public IECFHandler getMainHandler() {
        return DummyECFHandler.instance;
    }

    @Override
    public void updateIfScheduled() {

    }

    @Override
    public void scheduleMemberUpdate() {

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
}
