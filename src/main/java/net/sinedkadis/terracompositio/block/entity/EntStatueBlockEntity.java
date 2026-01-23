package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetwork;
import net.sinedkadis.terracompositio.api.networks.fluid.FluidNetworkMemberBE;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.block.behaviours.OneSlotItemHandlerBehaviour;
import net.sinedkadis.terracompositio.entity.custom.FlowCedarEntEntity;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCEntities;
import net.sinedkadis.terracompositio.registries.TCFluids;
import net.sinedkadis.terracompositio.registries.TCItems;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class EntStatueBlockEntity extends TCBlockEntity implements FluidNetworkMemberBE {
    public EntStatueBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ENT_STATUE_BE.get(), pos, state);
    }



    int cd = 20;

    @Override
    public void addBEBehaviours(@NotNull List<IBEBehaviour> list) {
        list.add(new OneSlotItemHandlerBehaviour(this){
            @Override
            public boolean isItemAllowed(int pSlot, @NotNull ItemStack pStack) {
                return pStack.is(TCItems.TECHNETIUM_CROWN.get());
            }
        });
    }

    @Override
    public void tick(@NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState) {
        super.tick(pLevel, pPos, pState);
        FluidNetwork fluidNetworkInstance = TerraCompositioAPI.INSTANCE.getFluidNetworkInstance();
        boolean inNetwork = fluidNetworkInstance.isIn(level, this);
        if (!inNetwork && !this.isRemoved()) {
            fluidNetworkInstance.fireFluidNetworkEvent(this, NetworkAction.ADD);
        }
        if (cd<= 0) {
            fluidNetworkInstance.fireFluidNetworkEvent(this, NetworkAction.UPDATE);
            cd = 20;
        }
        cd--;
        updateIfScheduled();
    }

    public void jojoReference() {
        if (level instanceof ServerLevel) {
            FlowCedarEntEntity pEntity = TCEntities.FLOW_CEDAR_ENT.get().create(level);
            if (pEntity != null) {
                level.addFreshEntity(pEntity);
                pEntity.getInnerCFEOptional().ifPresent(icfeHandler -> icfeHandler.setCFE(30));
                ItemStack crown = this.itemHandler().getStackInSlot(0);
                if (crown.is(TCItems.TECHNETIUM_CROWN.get())) {
                    pEntity.setItemSlot(EquipmentSlot.HEAD,crown.copyAndClear());
                }
                BlockPos blockPos = getBlockPos();
                pEntity.setPos(blockPos.getCenter());
                level.destroyBlock(blockPos,false);
                TerraCompositioAPI.instance().getFluidNetworkInstance()
                        .fireFluidNetworkEvent(this,NetworkAction.REMOVE);
                //this.setRemoved();
            }
        }
    }

    private IItemHandler itemHandler() {
        return ((OneSlotItemHandlerBehaviour) (behaviours.get(0))).getItemHandler();
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public int getLimit() {
        return 5;
    }

    @Override
    public void onFluidNetworkMemberUpdate() {
        FluidNetwork fluidNetworkInstance = TerraCompositioAPI.INSTANCE.getFluidNetworkInstance();
        BlockPos pos = this.getBlockPos();
        if (getPriority() > 0) {
            FluidNetworkMemberBE source = fluidNetworkInstance
                    .getClosestFluidHandlerWithMatchingContent(pos, level, TCFluids.FLOW_FLUID.source.get(), 10, getPriority());

            if (source != null) {
                Optional<IFluidHandler> fluidHandlerOptional = source.getEntity().getCapability(ForgeCapabilities.FLUID_HANDLER).resolve();
                if (fluidHandlerOptional.isPresent() && fluidHandlerOptional.get() instanceof FluidTank sourceTank) {

                    FluidStack amount = sourceTank.drain(500, IFluidHandler.FluidAction.SIMULATE);
                    if (amount.getAmount() == 500){
                        sourceTank.drain(500, IFluidHandler.FluidAction.EXECUTE);
                        TCUtil.sendFluidParticles((ServerLevel) level,pos,source.getBlockPos(), amount.getAmount() /10,amount);
                        jojoReference();
                    }
                }
            }
        }
    }
}
