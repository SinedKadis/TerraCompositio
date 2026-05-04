package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemStackHandler;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.behaviours.CFEHandlerBehaviour;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.block.behaviours.TwoSlotItemHandlerBehaviour;
import net.sinedkadis.terracompositio.compat.jade.JadeTerraCompositioPlugin;
import net.sinedkadis.terracompositio.config.TCInnerConfig;
import net.sinedkadis.terracompositio.particle.CFEParticleData;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.recipe.FlowInfusionRecipe;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

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
            public void onAppendTooltip(ITooltip iTooltip, CompoundTag serverData, IPluginConfig iPluginConfig) {
                super.onAppendTooltip(iTooltip, serverData, iPluginConfig);
                if (serverData.contains("cfe_tick")  && iPluginConfig.get(JadeTerraCompositioPlugin.debugConfigRL())) {
                    iTooltip.add(Component.translatable("block.terracompositio." + "cfe_tick", serverData.getFloat("cfe_tick")));
                }
            }
        }.priority(TCInnerConfig.DEFAULT_CONSUMER_PRIORITY));
        list.add(new TwoSlotItemHandlerBehaviour(this){
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
        SimpleContainer inventory = new SimpleContainer(this.itemHandler().getSlots());
        for(int i = 0; i < itemHandler().getSlots(); i++) {
            inventory.setItem(i, this.itemHandler().getStackInSlot(i));
        }

        assert this.level != null;
        return this.level.getRecipeManager().getRecipeFor(FlowInfusionRecipe.Type.INSTANCE, inventory, level);
    }

    @Override
    protected ItemStackHandler itemHandler() {
        return ((TwoSlotItemHandlerBehaviour) behaviours.get(1)).getItemHandler();
    }

    protected ICFEHandler cfeContainer() {
        return ((CFEHandlerBehaviour) behaviours.get(0)).getMainHandler();
    }


    private void spawnParticles() {
        if (level instanceof ServerLevel serverLevel) {
            float speed = cfeContainer().getCfeTravelSpeed();
            BlockPos blockPos = getBlockPos();
            serverLevel.sendParticles(new CFEParticleData(speed),
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
                if (iItemHandler instanceof TwoSlotItemHandlerBehaviour.SlotSensitiveItemStackHandler slotSensitiveItemStackHandler) {
                    ItemStack left = slotSensitiveItemStackHandler.forceInsertItem(0, result.copy(), false);
                    itemHandler().setStackInSlot(0,left);
                }
            });
            return result;
        }
        return ItemStack.EMPTY;
    }



}
