package net.sinedkadis.terracompositio.block.entity;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.SoundAction;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemHandlerHelper;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.cfe.CFEContainer;
import net.sinedkadis.terracompositio.fluid.ModFluids;
import net.sinedkadis.terracompositio.mantle.IFluidContainerTransfer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class FlowExtractorBlockEntity extends BlockEntity implements CFEContainer {
    @Getter
    private final FluidTank fluidTank = new FluidTank(5000) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            FlowExtractorBlockEntity.this.sendUpdate();
        }
    };

    @Getter
    private final LazyOptional<FluidTank> fluidOptional = LazyOptional.of(() -> this.fluidTank);
    private int CFE;


    public FlowExtractorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.FLOW_EXTRACTOR_BE.get(),pPos, pBlockState);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.FLUID_HANDLER)
            return this.fluidOptional.cast();

        return super.getCapability(cap);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.fluidOptional.invalidate();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.put("FluidTank", this.fluidTank.writeToNBT(new CompoundTag()));
        pTag.putInt("CFE",this.CFE);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        this.fluidTank.readFromNBT(pTag.getCompound("FluidTank"));
        this.CFE = pTag.getInt("CFE");
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if(this.fluidTank.getFluidAmount() >= this.fluidTank.getCapacity())
            return;
        if (this.fluidTank.getFluid().getFluid() == ModFluids.FLOW_FLUID.source.get().getSource()){
            
            this.fluidTank.drain(1, IFluidHandler.FluidAction.EXECUTE);
            this.CFE++;
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        saveAdditional(nbt);
        return nbt;
    }

    @Override
    public int getCFE() {
        return CFE;
    }

    @Override
    public void setCFE(int count) {
        CFE = count;
    }

    private void sendUpdate() {
        setChanged();

        if (this.level != null)
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public void interact(Player player, InteractionHand hand) {
        // skip client side, and skip if the recipe already started
        if (level == null || level.isClientSide) {
            return;
        }
        // first try interacting with the table as a tank. If that fails, run normal item swap logic
        // normal item swap logic should only run if we lack a fluid though
        ItemStack held = player.getItemInHand(hand);
        interactWithFluidItem(player, hand, held);
    }
    /** Interacts with a fluid item held by the player */
    private boolean interactWithFluidItem(Player player, InteractionHand hand, ItemStack stack) {
        if (level == null) {
            return false;
        }
        // fallback to JSON based transfer
        if (mayHaveTransfer(stack)) {
            // only actually transfer on the serverside, client just has items
            FluidStack currentFluid = fluidTank.getFluid();
            IFluidContainerTransfer transfer = getTransfer(stack, currentFluid);
            if (transfer != null) {
                IFluidContainerTransfer.TransferResult result = transfer.transfer(stack, currentFluid, fluidTank);
                if (result != null) {
                    if (result.didFill()) {
                        playFillSound(level, worldPosition, player, result.fluid());
                    } else {
                        playEmptySound(level, worldPosition, player, result.fluid());
                    }
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, result.stack()));
                    return true;
                }
            }
            // consistency with tanks: don't try fluid handler if we had JSON override for this item type
            return false;
        }

        // if the item has a capability, do a direct transfer
        ItemStack copy = ItemHandlerHelper.copyStackWithSize(stack, 1);
        LazyOptional<IFluidHandlerItem> itemCapability = copy.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
        if (itemCapability.isPresent()) {
            IFluidHandlerItem itemHandler = itemCapability.resolve().orElseThrow();
            // first, try filling the TE from the item
            FluidStack transferred = tryTransfer(itemHandler, fluidTank, Integer.MAX_VALUE);
            if (!transferred.isEmpty()) {
                playEmptySound(level, worldPosition, player, transferred);
            } else if (!fluidTank.isEmpty()) {
                // if that failed, try filling the item handler from the TE
                transferred = tryTransfer(fluidTank, itemHandler, Integer.MAX_VALUE);
                if (!transferred.isEmpty()) {
                    playFillSound(level, worldPosition, player, transferred);
                }
            }
            // if either worked, update the player's inventory
            if (!transferred.isEmpty()) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, itemHandler.getContainer()));
                return true;
            }
        }
        return false;
    }
    /** Gets the transfer for the given item and fluid, or null if its not a valid item and fluid */
    @javax.annotation.Nullable
    public IFluidContainerTransfer getTransfer(ItemStack stack, FluidStack fluid) {
        for (IFluidContainerTransfer transfer : transfers) {
            if (transfer.matches(stack, fluid)) {
                return transfer;
            }
        }
        return null;
    }
    public boolean mayHaveTransfer(ItemStack stack) {
        return getContainerItems().contains(stack.getItem());
    }
    /** Lazily initializes the set of container items */
    protected Set<Item> getContainerItems() {
        if (this.containerItems == null) {
            ImmutableSet.Builder<Item> builder = ImmutableSet.builder();
            Consumer<Item> consumer = builder::add;
            for (IFluidContainerTransfer transfer : transfers) {
                transfer.addRepresentativeItems(consumer);
            }
            this.containerItems = builder.build();
        }
        return this.containerItems;
    }
    @Setter
    @javax.annotation.Nullable
    private Set<Item> containerItems = Collections.emptySet();
    private List<IFluidContainerTransfer> transfers = Collections.emptyList();

    private static final String KEY_FILLED = TerraCompositio.makeDescriptionId("block", "tank.filled");
    private static final String KEY_DRAINED = TerraCompositio.makeDescriptionId("block", "tank.drained");

    private static void playEmptySound(Level world, BlockPos pos, Player player, FluidStack transferred) {
        world.playSound(null, pos, getEmptySound(transferred), SoundSource.BLOCKS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable(KEY_FILLED, transferred.getAmount(), transferred.getDisplayName()), true);
    }

    private static void playFillSound(Level world, BlockPos pos, Player player, FluidStack transferred) {
        world.playSound(null, pos, getFillSound(transferred), SoundSource.BLOCKS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable(KEY_DRAINED, transferred.getAmount(), transferred.getDisplayName()), true);
    }
    public static SoundEvent getEmptySound(FluidStack fluid) {
        return getSound(fluid, SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY);
    }
    /** Gets the given sound from the fluid */
    public static SoundEvent getSound(FluidStack fluid, SoundAction action, SoundEvent fallback) {
        SoundEvent event = fluid.getFluid().getFluidType().getSound(fluid, action);
        if (event == null) {
            return fallback;
        }
        return event;
    }
    /** Gets the fill sound for a fluid */
    public static SoundEvent getFillSound(FluidStack fluid) {
        return getSound(fluid, SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL);
    }
    /**
     * Attempts to transfer fluid
     * @param input    Fluid source
     * @param output   Fluid destination
     * @param maxFill  Maximum to transfer
     * @return  True if transfer succeeded
     */
    public static FluidStack tryTransfer(IFluidHandler input, IFluidHandler output, int maxFill) {
        // first, figure out how much we can drain
        FluidStack simulated = input.drain(maxFill, IFluidHandler.FluidAction.SIMULATE);
        if (!simulated.isEmpty()) {
            // next, find out how much we can fill
            int simulatedFill = output.fill(simulated, IFluidHandler.FluidAction.SIMULATE);
            if (simulatedFill > 0) {
                // actually drain
                FluidStack drainedFluid = input.drain(simulatedFill, IFluidHandler.FluidAction.EXECUTE);
                if (!drainedFluid.isEmpty()) {
                    // acutally fill
                    int actualFill = output.fill(drainedFluid.copy(), IFluidHandler.FluidAction.EXECUTE);
                    if (actualFill != drainedFluid.getAmount()) {
                        TerraCompositio.logger.error("Lost {} fluid during transfer", drainedFluid.getAmount() - actualFill);
                    }
                }
                return drainedFluid;
            }
        }
        return FluidStack.EMPTY;
    }
}
