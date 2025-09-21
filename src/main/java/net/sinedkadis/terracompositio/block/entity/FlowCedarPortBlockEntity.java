package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.recipe.FlowSaturationRecipe;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.screen.FlowBlockPortMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

import static net.sinedkadis.terracompositio.registries.TCBlockStateProperties.INFUSED;

@SuppressWarnings("DataFlowIssue")
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FlowCedarPortBlockEntity extends TCItemIOCFEBlockEntity implements MenuProvider {

    protected final ContainerData data;
    private int tickForSound = 0;

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    public FlowCedarPortBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(TCBlockEntities.FLOW_PORT_BE.get(),pPos,pBlockState,BlockMode.CONSUMER);
        this.data =new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex){
                    case 0 -> FlowCedarPortBlockEntity.this.progress;
                    case 1 -> FlowCedarPortBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex){
                    case 0 -> FlowCedarPortBlockEntity.this.progress = pValue;
                    case 1 -> FlowCedarPortBlockEntity.this.maxProgress = pValue;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        pTag.putInt("flow_port_progress", progress);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        progress = pTag.getInt("flow_port_progress");
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        //boolean hasRecipe = hasRecipe();
        //LOGGER.debug("Recipe found("+hasRecipe+")");
        super.tick(pLevel,pPos,pState);
        if (this.getBlockState().getValue(INFUSED)) {
            if (hasRecipe()) {
                tickForSound++;
                if (tickForSound == 5) {
                    tickForSound = 0;
                    pLevel.playSound(null, pPos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS);
                }
                increaseCraftingProgress();
                setChanged(pLevel, pPos, pState);
                if (hasProgressFinished()) {
                    ItemStack crafted = craftItem();
                    Player nearestPlayer = pLevel.getNearestPlayer(pPos.getX(), pPos.getY(), pPos.getZ(), 16, false);
                    if (nearestPlayer != null)
                        crafted.onCraftedBy(pLevel, nearestPlayer,crafted.getCount());
                    resetProgress();
                }
            } else {
                resetProgress();
            }
        }
        if (pLevel.getRandom().nextFloat() < 0.005f && itemHandler.getStackInSlot(SLOT_INPUT).isEmpty()) {
            pLevel.playSound(null, pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS,2,1);
        }
    }

    protected boolean hasRecipe() {
        Optional<FlowSaturationRecipe> recipe = getCurrentRecipe();
        if (recipe.isEmpty()){
            return false;
        }
        ItemStack result = recipe.get().getResultItem(null);
        boolean outputTest = enoughSpaceInOutput(result.getCount()) && sameItemInOutput(result.getItem());
        if (outputTest){
            maxProgress = 78;
        }
        return outputTest;
    }

    protected ItemStack craftItem() {
        Optional<FlowSaturationRecipe> recipe = getCurrentRecipe();
        if (recipe.isPresent()) {
            ItemStack result = recipe.get().getResultItem(null);
            this.itemHandler.forceExtractItem(SLOT_INPUT, 1, false);
            this.itemHandler.forceInsertItem(SLOT_OUTPUT, result,false);
            return result;
        }
        return ItemStack.EMPTY;
    }

    protected Optional<FlowSaturationRecipe> getCurrentRecipe() {
        SimpleContainer inventory = new SimpleContainer(this.itemHandler.getSlots());
        for(int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, this.itemHandler.getStackInSlot(i));
        }

        assert this.level != null;
        return this.level.getRecipeManager().getRecipeFor(FlowSaturationRecipe.Type.INSTANCE, inventory, level);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.terracompositio.flow_cedar_port");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
        return new FlowBlockPortMenu(pContainerId,pPlayerInventory,this,this.data);
    }


}
