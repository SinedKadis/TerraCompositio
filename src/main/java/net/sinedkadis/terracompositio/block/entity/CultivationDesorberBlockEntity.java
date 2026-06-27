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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
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
public class CultivationDesorberBlockEntity extends AbstractDesorberBlockEntity {

    private final ItemStackHandler renderStack = new ItemStackHandler();

    public CultivationDesorberBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.CULTIVATION_DESORBER_BE.get(), pos, state);
    }

    @Override
    protected int getMaxCFE() {
        return 32;
    }

    public void setRenderStack(ItemStack itemStack) {
        this.renderStack.setStackInSlot(0,itemStack);
    }

    public ItemStack getRenderStack() {
        return this.renderStack.getStackInSlot(0);
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



    @SubscribeEvent
    public static void onCropGrowEvent(BlockEvent.CropGrowEvent event){
        BlockPos pos = event.getPos();
        LevelAccessor level = event.getLevel();
        BlockState state = event.getState();
        ECFNetwork network = TerraCompositioAPI.instance().getECFNetworkInstance();
        Set<ECFNetworkMember> sources = network.getAllECFNetworkMembers((Level) level);
        List<CultivationDesorberBlockEntity> cultivators = sources.stream()
                .map(ECFNetworkMember::getPos)
                .filter(ecfSourceBlockPose -> Math.sqrt(ecfSourceBlockPose.distSqr(pos)) < 7)
                .map(ecfSourceBlockPose -> {
                    if (level.getBlockEntity(ecfSourceBlockPose) instanceof CultivationDesorberBlockEntity blockEntity)
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
        int ECFToAdd = 1;
        for (CultivationDesorberBlockEntity blockEntity : cultivators){
            FluidTank fluidHandler1 = blockEntity.fluidHandler;
            if (!fluidHandler1.isEmpty() && fluidHandler1.getFluidAmount() >= ECFToAdd) {
                fluidHandler1.drain(ECFToAdd, IFluidHandler.FluidAction.EXECUTE);
                int added = blockEntity.ecfContainer().addECF(ECFToAdd, false);
                ECFToAdd -= added;
                if (!level.isClientSide())
                    level.playSound(null,pos, SoundEvents.AZALEA_LEAVES_STEP, SoundSource.BLOCKS);
                ParticleHelperInternal.sendECFParticles((ServerLevel) level,
                        blockEntity.ecfContainer().getOffset().apply(blockEntity.getBlockPos().getCenter()),
                        pos.getCenter(),
                        added);
                //noinspection deprecation
                blockEntity.setRenderStack(new ItemStack(state.getBlock()
                        .getDrops(state,new LootParams.Builder((ServerLevel) level)
                                .withParameter(LootContextParams.ORIGIN,pos.getCenter())
                                .withParameter(LootContextParams.TOOL,ItemStack.EMPTY)).get(0).getItem().asItem()));
                if (ECFToAdd == 0) {
                    break;
                }
            }
        }
    }
}
