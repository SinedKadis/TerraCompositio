package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.block.IFluidApplicable;
import net.sinedkadis.terracompositio.block.behaviours.ItemHandlerBehaviour;
import net.sinedkadis.terracompositio.recipe.AltarTransformationRecipe;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.util.helpers.ParticleHelper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

import static net.sinedkadis.terracompositio.registries.TCBlockStateProperties.INFUSED;

@SuppressWarnings("DataFlowIssue")
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FlowCedarAltarBlockEntity extends TCCraftingBlockEntity implements IFluidApplicable {


    public FlowCedarAltarBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(TCBlockEntities.FLOW_ALTAR_BE.get(), pPos, pBlockState);
    }

    @Override
    public void addBEBehaviours(List<IBEBehaviour> list) {
        list.add(new ItemHandlerBehaviour(this, 3) {
            @Override
            public int getLimitInSlot(int slot) {
                return 1;
            }

            @Override
            public boolean allowInsert(int pSlot, ItemStack pStack, @Nullable Direction pDirection, boolean manual) {
                return !(pSlot == 2);
            }
        });
    }

    boolean wasCrafting = false;
    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        super.tick(pLevel,pPos,pState);
        if (this.getBlockState().getValue(INFUSED)
                && pLevel.getBlockState(pPos.below()).is(TCBlocks.FLOW_CEDAR_PEDESTAL.get())) {
            if (hasRecipe()) {
                if (!wasCrafting) {
                    pLevel.playSound(null, pPos, SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.BLOCKS);
                }
                ParticleHelper.spawnParticlesIn(pLevel, pPos);
                increaseCraftingProgress();
                if (hasProgressFinished()) {
                    craftItem();
                    resetProgress();
                    setChanged(pLevel, pPos, pState);
                    pLevel.sendBlockUpdated(pPos, pState, pState, 3);
                }
                wasCrafting = true;
            } else {
                wasCrafting = false;
                resetProgress();
            }
        }
    }

    protected IItemHandlerModifiable getItemHandler() {
        return (IItemHandlerModifiable) this.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(EmptyHandler.INSTANCE);
    }

    protected boolean hasRecipe() {
        Optional<AltarTransformationRecipe> recipe = getCurrentRecipe();
        if (recipe.isEmpty()){
            return false;
        }
        ItemStack result = recipe.get().getResultItem(null);
        boolean outputTest = enoughSpaceInOutput(result.getCount()) && sameItemInOutput(result.getItem());
        if (outputTest){
            maxProgress = 80;
        }
        return outputTest;
    }

    protected ItemStack craftItem() {
        Optional<AltarTransformationRecipe> recipe = getCurrentRecipe();
        if (recipe.isPresent()) {
            ItemStack result = recipe.get().getResultItem(null);
            this.getItemHandler().setStackInSlot(0, ItemStack.EMPTY);
            this.getItemHandler().setStackInSlot(1, ItemStack.EMPTY);
            this.getItemHandler().setStackInSlot(2, result);
            return result;
        }
        return ItemStack.EMPTY;
    }

    protected Optional<AltarTransformationRecipe> getCurrentRecipe() {
        SimpleContainer inventory = new SimpleContainer(this.getItemHandler().getSlots());
        for (int i = 0; i < getItemHandler().getSlots(); i++) {
            inventory.setItem(i, this.getItemHandler().getStackInSlot(i));
        }

        assert this.level != null;
        return this.level.getRecipeManager().getRecipeFor(AltarTransformationRecipe.Type.INSTANCE, inventory, level);
    }

    @Override
    public int defaultConsumeAmount() {
        return 500;
    }
}
