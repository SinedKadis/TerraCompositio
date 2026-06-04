package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.behaviours.CFEHandlerBehaviour;
import net.sinedkadis.terracompositio.block.behaviours.ItemHandlerBehaviour;
import net.sinedkadis.terracompositio.config.TCCommonConfigs;
import net.sinedkadis.terracompositio.config.TCInnerConfig;
import net.sinedkadis.terracompositio.particle.CFEParticleData;
import net.sinedkadis.terracompositio.recipe.FlowInfusionRecipe;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FlowInfuserBlockEntity extends TCCraftingBlockEntity {

    public FlowInfuserBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(TCBlockEntities.FLOW_INFUSER_BE.get(),pPos, pBlockState);
    }

    @Override
    public void addBEBehaviours(List<IBEBehaviour> list) {
        list.add(new CFEHandlerBehaviour(this){

            @Override
            public void onAppendServerData(CompoundTag compoundTag) {
                super.onAppendServerData(compoundTag);
                compoundTag.putFloat("cfe_tick",tickCFECost);
            }

            @Override
            public void onAppendTooltip(List<Component> iTooltip, CompoundTag serverData) {
                super.onAppendTooltip(iTooltip, serverData);
                if (serverData.contains("cfe_tick") && TCCommonConfigs.DEBUG.get()) {
                    iTooltip.add(Component.translatable("block.terracompositio." + "cfe_tick", serverData.getFloat("cfe_tick")));
                }
            }
        }.priority(TCInnerConfig.DEFAULT_CONSUMER_PRIORITY));
        list.add(new ItemHandlerBehaviour(this, 2) {
            @Override
            public boolean allowInsert(int pSlot, ItemStack pStack, @Nullable Direction pDirection, boolean manual) {
                return manual && pSlot == 0;
            }

            @Override
            public boolean allowExtract(int pSlot, ItemStack pStack, @Nullable Direction pDirection, boolean manual) {
                return manual;
            }

            @Override
            public int getLimitInSlot(int slot) {
                return 1;
            }
        });

    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        super.tick(pLevel, pPos, pState);
        if (!pLevel.isClientSide) {
            if (hasRecipe() && enoughCFE()) {
                increaseCraftingProgress();
                consumeCFE();
                setChanged(pLevel, pPos, pState);
                spawnParticles();
                if (hasProgressFinished()) {
                    craftItem();
                    resetProgress();
                }
            } else if (!hasRecipe()) {
                resetProgress();
            }
        }
    }

    public boolean enoughCFE() {
        int cfe = cfeContainer().getCFE();
        return cfe > tickCFECost;
    }

    public boolean hasRecipe() {
        Optional<FlowInfusionRecipe> recipe = getCurrentRecipe();
        if (recipe.isEmpty()){
            return false;
        }
        ItemStack result = recipe.get().getResultItem(null);
        boolean outputTest = enoughSpaceInOutput(result.getCount()) && sameItemInOutput(result.getItem());
        if (outputTest){
            maxProgress = recipe.get().getTicks();
            tickCFECost = recipe.get().getCFETick();
        }
        return outputTest;
    }

    protected Optional<FlowInfusionRecipe> getCurrentRecipe() {
        SimpleContainer inventory = new SimpleContainer(this.getItemHandler().getSlots());
        for (int i = 0; i < getItemHandler().getSlots(); i++) {
            inventory.setItem(i, this.getItemHandler().getStackInSlot(i));
        }

        assert this.level != null;
        return this.level.getRecipeManager().getRecipeFor(FlowInfusionRecipe.Type.INSTANCE, inventory, level);
    }

    @Override
    protected IItemHandlerModifiable getItemHandler() {
        return (IItemHandlerModifiable) getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(EmptyHandler.INSTANCE);
    }

    protected ICFEHandler cfeContainer() {
        return ((CFEHandlerBehaviour) behaviours.get(0)).getMainHandler();
    }


    private void spawnParticles() {
        if (level instanceof ServerLevel serverLevel) {
            BlockPos blockPos = getBlockPos();
            serverLevel.sendParticles(new CFEParticleData(1 / 20f),
                    blockPos.getX() + 0.5D,
                    blockPos.getY() + 0.5D,
                    blockPos.getZ() + 0.5D, 1, 0, -0.1D, 0, 0.1D);
        }
    }


    protected ItemStack craftItem() {
        Optional<FlowInfusionRecipe> recipe = getCurrentRecipe();
        if (recipe.isPresent()) {
            ItemStack result = recipe.get().getResultItem(null);
            this.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
                if (getItemBehaviour() instanceof ItemHandlerBehaviour itemHandlerBehaviour) {
                    itemHandlerBehaviour.ignoreRestrictions = true;
                    iItemHandler.extractItem(0, 1, false);
                    iItemHandler.insertItem(1, result.copy(), false);
                    itemHandlerBehaviour.ignoreRestrictions = false;
                    if (level != null) {
                        BlockState blockState = getBlockState();
                        level.sendBlockUpdated(worldPosition, blockState, blockState, 3);
                    }
                }
            });
            return result;
        }
        return ItemStack.EMPTY;
    }



}
