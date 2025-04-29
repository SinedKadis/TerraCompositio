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
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.cfe.CFESource;
import net.sinedkadis.terracompositio.registries.ModBlockEntities;
import net.sinedkadis.terracompositio.util.ModItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = TerraCompositio.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CultivationDesorberBlockEntity extends AbstractDesorberBlockEntity {

    private final ModItemStackHandler renderStack = new ModItemStackHandler(this);

    public CultivationDesorberBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CULTIVATION_DESORBER_BE.get(), pos, state);
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
        CFENetwork network = TerraCompositioAPI.instance().getCFENetworkInstance();
        List<CFESource> sources = network.getAllCFESources((Level) level);
        List<CultivationDesorberBlockEntity> cultivators = sources.stream()
                .map(CFESource::getCFESourceBlockPos)
                .filter(cfeSourceBlockPos -> Math.sqrt(cfeSourceBlockPos.distSqr(pos)) < 7)
                .map(cfeSourceBlockPos -> {
                    if (level.getBlockEntity(cfeSourceBlockPos) instanceof CultivationDesorberBlockEntity blockEntity)
                        return blockEntity;
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
        int CFEToAdd = 1;
        for (CultivationDesorberBlockEntity blockEntity : cultivators){
            FluidTank fluidHandler1 = blockEntity.fluidHandler;
            if (!fluidHandler1.isEmpty() && fluidHandler1.getFluidAmount() >= CFEToAdd) {
                fluidHandler1.drain(CFEToAdd, IFluidHandler.FluidAction.EXECUTE);
                CFEToAdd -= blockEntity.addCFE(CFEToAdd);
                if (!level.isClientSide())
                    level.playSound(null,pos, SoundEvents.AZALEA_LEAVES_STEP, SoundSource.BLOCKS);
                blockEntity.setRenderStack(new ItemStack(state.getBlock()
                        .getDrops(state,new LootParams.Builder((ServerLevel) level)
                                .withParameter(LootContextParams.ORIGIN,pos.getCenter())
                                .withParameter(LootContextParams.TOOL,ItemStack.EMPTY)).get(0).getItem().asItem()));
                if (CFEToAdd == 0) {
                    break;
                }
            }
        }
    }

}
