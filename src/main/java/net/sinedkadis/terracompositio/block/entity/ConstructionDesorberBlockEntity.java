package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemStackHandler;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetwork;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMember;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.util.helpers.ParticleHelperInternal;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = TerraCompositio.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ConstructionDesorberBlockEntity extends AbstractDesorberBlockEntity {

    private final ItemStackHandler renderStack = new ItemStackHandler();

    public ConstructionDesorberBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.CONSTRUCTION_DESORBER_BE.get(), pos, state);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        pTag.put("render", renderStack.serializeNBT());
        super.saveAdditional(pTag);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        renderStack.deserializeNBT(pTag.getCompound("render"));
    }

    public void setRenderStack(ItemStack itemStack) {
        this.renderStack.setStackInSlot(0,itemStack);
    }

    public ItemStack getRenderStack() {
        return this.renderStack.getStackInSlot(0);
    }

    @SubscribeEvent
    public static void onEntityPlaceEvent(BlockEvent.EntityPlaceEvent event){
        BlockPos pos = event.getPos();
        LevelAccessor level = event.getLevel();

        ECFNetwork network = TerraCompositioAPI.instance().getECFNetworkInstance();
        Set<ECFNetworkMember> members = network.getAllECFNetworkMembers((Level) level);
        List<ConstructionDesorberBlockEntity> constructors = members.stream()
                .filter(ecfSource -> Math.sqrt(ecfSource.getPos().distSqr(pos)) < ecfSource.getRange())
                .map(ECFNetworkMember::getPos)
                .map(ecfSourceBlockPos -> {
                    if (level.getBlockEntity(ecfSourceBlockPos) instanceof ConstructionDesorberBlockEntity blockEntity)
                        return blockEntity;
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        collectedList -> {
                            Collections.shuffle(collectedList);
                            return collectedList;
                        }
                ));
        int ECFToAdd = 5;
        for (ConstructionDesorberBlockEntity blockEntity : constructors){
            FluidTank fluidHandler1 = blockEntity.fluidHandler;
            if (!fluidHandler1.isEmpty() && fluidHandler1.getFluidAmount() >= ECFToAdd) {
                fluidHandler1.drain(ECFToAdd, IFluidHandler.FluidAction.EXECUTE);
                int added = blockEntity.ecfContainer().addECF(ECFToAdd, false);
                ECFToAdd -= added;
                blockEntity.setRenderStack(new ItemStack(event.getPlacedBlock().getBlock()));
                if (ECFToAdd == 0) {
                    if (!level.isClientSide()) {
                        BlockPos blockEntityBlockPos = blockEntity.getBlockPos();
                        level.playSound(null, blockEntityBlockPos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.1f, 1f);
                        ParticleHelperInternal.sendECFParticles((ServerLevel) level,
                                blockEntity.ecfContainer().getOffset().apply(blockEntityBlockPos.getCenter()),
                                pos.getCenter(),
                                added);
                    }
                    break;
                }
            }
        }
    }
    @SubscribeEvent
    public static void onBreakEvent(BlockEvent.BreakEvent event){
        BlockPos pos = event.getPos();
        LevelAccessor level = event.getLevel();
        ECFNetwork network = TerraCompositioAPI.instance().getECFNetworkInstance();
        Set<ECFNetworkMember> sources = network.getAllECFNetworkMembers((Level) level);
        List<ConstructionDesorberBlockEntity> constructors = sources.stream()
                .map(ECFNetworkMember::getPos)
                .filter(cfeSourceBlockPos -> Math.sqrt(cfeSourceBlockPos.distSqr(pos)) < 7)
                .map(cfeSourceBlockPos -> {
                    if (level.getBlockEntity(cfeSourceBlockPos) instanceof ConstructionDesorberBlockEntity blockEntity)
                        return blockEntity;
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        collectedList -> {
                            Collections.shuffle(collectedList);
                            return collectedList;
                        }
                ));
        int CFEToRemove = 5;
        for (ConstructionDesorberBlockEntity blockEntity : constructors){
            if (blockEntity != null) {
                FluidTank fluidHandler1 = blockEntity.fluidHandler;
                if (!fluidHandler1.isEmpty()) {
                    CFEToRemove -= fluidHandler1.drain(CFEToRemove*2, IFluidHandler.FluidAction.EXECUTE).getAmount()/2;
                    blockEntity.setRenderStack(new ItemStack(event.getState().getBlock()));
                    if (!level.isClientSide())
                        level.playSound(null, blockEntity.getBlockPos(), SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS,0.1f,1f);
                }
                if (CFEToRemove == 0) {
                    break;
                }
            }
        }
    }

}
