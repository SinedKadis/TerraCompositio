package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.registries.ModBlockEntities;
import net.sinedkadis.terracompositio.registries.ModBlockStateProperties;
import net.sinedkadis.terracompositio.registries.ModBlocks;
import net.sinedkadis.terracompositio.util.ModItemStackHandler;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ConstructionDesorberBlockEntity extends AbstractDesorberBlockEntity {

    private final ModItemStackHandler renderStack = new ModItemStackHandler(this);

    public ConstructionDesorberBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONSTRUCTION_DESORBER_BE.get(), pos, state);
    }

    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        super.tick(pLevel, pPos, pState);
        if (!fluidHandler.isEmpty() && !pState.getValue(ModBlockStateProperties.INFUSED)) {
            pLevel.setBlockAndUpdate(pPos, pState.setValue(ModBlockStateProperties.INFUSED, true));
        } else if (fluidHandler.isEmpty() && pState.getValue(ModBlockStateProperties.INFUSED)) {
            pLevel.setBlockAndUpdate(pPos, pState.setValue(ModBlockStateProperties.INFUSED, false));
        }
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

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Mod.EventBusSubscriber(modid = TerraCompositio.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    private static class EventHandler {
        @SubscribeEvent
        public static void onEntityPlaceEvent(BlockEvent.EntityPlaceEvent event){
            BlockPos pos = event.getPos();
            LevelAccessor level = event.getLevel();
            List<BlockPos> constructors = new ArrayList<>(TCUtil.getNearBlocks(pos, 7).stream().filter(pos1 -> level.getBlockState(pos1).is(ModBlocks.CONSTRUCTION_DESORBER.get())).toList());
            if (!constructors.isEmpty()) {
                Collections.shuffle(constructors);
                int CFEToAdd = 5;
                for (BlockPos constructor : constructors){
                    ConstructionDesorberBlockEntity blockEntity = (ConstructionDesorberBlockEntity) level.getBlockEntity(constructor);
                    if (blockEntity != null) {
                        FluidTank fluidHandler1 = blockEntity.fluidHandler;
                        if (!fluidHandler1.isEmpty() && fluidHandler1.getFluidAmount() > CFEToAdd) {
                            fluidHandler1.drain(CFEToAdd, IFluidHandler.FluidAction.EXECUTE);
                            CFEToAdd -= blockEntity.addCFE(CFEToAdd);
                            blockEntity.setRenderStack(new ItemStack(event.getPlacedBlock().getBlock()));
                            if (CFEToAdd == 0) {
                                if (!level.isClientSide())
                                    level.playSound(null,pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS,0.1f,1f);
                                break;
                            }
                        }
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onBreakEvent(BlockEvent.BreakEvent event){
            BlockPos pos = event.getPos();
            LevelAccessor level = event.getLevel();
            List<BlockPos> constructors = new ArrayList<>(TCUtil.getNearBlocks(pos, 7).stream().filter(pos1 -> level.getBlockState(pos1).is(ModBlocks.CONSTRUCTION_DESORBER.get())).toList());
            if (!constructors.isEmpty()) {
                Collections.shuffle(constructors);
                int CFEToRemove = 5;
                for (BlockPos constructor : constructors){
                    ConstructionDesorberBlockEntity blockEntity = (ConstructionDesorberBlockEntity) level.getBlockEntity(constructor);
                    if (blockEntity != null) {
                        FluidTank fluidHandler1 = blockEntity.fluidHandler;
                        if (!fluidHandler1.isEmpty()) {
                            CFEToRemove -= fluidHandler1.drain(CFEToRemove*2, IFluidHandler.FluidAction.EXECUTE).getAmount()/2;
                            blockEntity.setRenderStack(new ItemStack(event.getState().getBlock()));
                            if (!level.isClientSide())
                                level.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS,0.1f,1f);
                        }
                        if (CFEToRemove == 0) {
                            break;
                        }
                    }
                }
            }
        }
    }
}
