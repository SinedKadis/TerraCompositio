package net.sinedkadis.terracompositio.block.entity;

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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.SoundAction;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.cfe.CFEContainer;
import net.sinedkadis.terracompositio.fluid.ModFluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static net.sinedkadis.terracompositio.TerraCompositio.GLOGGER;

public class FlowExtractorBlockEntity extends BlockEntity implements CFEContainer {
    private final FluidTank fluidTank = new FluidTank(1000) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            FlowExtractorBlockEntity.this.sendUpdate();
        }
    };

    private final LazyOptional<FluidTank> fluidOptional = LazyOptional.of(() -> this.fluidTank);
    private int CFE;
    public float prevScale;
    private static final String KEY_FILLED = TerraCompositio.makeDescriptionId("block", "tank.filled");
    private static final String KEY_DRAINED = TerraCompositio.makeDescriptionId("block", "tank.drained");


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
        pTag.putFloat("prevScale",this.prevScale);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        this.fluidTank.readFromNBT(pTag.getCompound("FluidTank"));
        this.CFE = pTag.getInt("CFE");
        this.prevScale = pTag.getInt("prevScale");
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if(this.fluidTank.getFluidAmount() >= this.fluidTank.getCapacity())
            return;
        if (this.fluidTank.getFluid().getFluid() == ModFluids.FLOW_FLUID.source.get().getSource()){
            this.fluidTank.drain(1, IFluidHandler.FluidAction.EXECUTE);
            this.CFE++;
        }
        float scale = fluidTank.getCapacity() == 0 ? 0 : (float) fluidTank.getFluidAmount() /fluidTank.getCapacity();
        //GLOGGER.debug(scale);
        if (scale != prevScale) {
            prevScale = scale;
        }

    }
    public InteractionResult interact(Level level,Player player, InteractionHand hand) {
        if (level.isClientSide) {
            return InteractionResult.PASS;
        }
        return interactWithFluidItem(level,player, hand) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }
    private boolean interactWithFluidItem(Level pLevel,Player pPlayer, InteractionHand pHand) {
        FluidStack currentFluid = fluidTank.getFluid();
        ItemStack held = pPlayer.getItemInHand(pHand);
        LazyOptional<IFluidHandlerItem> fluidHandler = pPlayer.getItemInHand(pHand).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
        IFluidHandlerItem handlerItem = fluidHandler.orElse(null);
        if (!fluidHandler.isPresent()) {
            GLOGGER.debug("fluid handler is not present --- {}, {}", currentFluid.getDisplayName(),currentFluid.getAmount());
            if (held.getItem() instanceof BucketItem bucketItem) {
                if (bucketItem.getFluid() == currentFluid.getFluid()
                        ||currentFluid.isEmpty()){
                    playFillSound(pLevel,worldPosition,pPlayer,currentFluid);
                    int e = fluidTank.fill(new FluidStack(bucketItem.getFluid(),1000), IFluidHandler.FluidAction.EXECUTE);
                    pPlayer.setItemInHand(pHand,new ItemStack(Items.BUCKET));
                    GLOGGER.debug("{}tank filled with {} mB of {} --- {}, {}", currentFluid.isEmpty() ? "Fluid tank is empty, " : "Fluid in tank is equal to fluid in bucket, ", e, bucketItem.getFluid(), currentFluid.getFluid(),currentFluid.getAmount());
                    return true;
                }
            }
            return false;
        } else if (fluidHandler.isPresent()) {
            GLOGGER.debug("Fluid handler is present - {} --- {}, {}", handlerItem, currentFluid.getDisplayName(),currentFluid.getAmount());
            /*
            if (handlerItem.getContainer().is(Items.BUCKET)){
                GLOGGER.debug(FluidUtil.getFilledBucket(currentFluid));
                pPlayer.setItemInHand(pHand,FluidUtil.getFilledBucket(currentFluid));
                fluidTank.drain(currentFluid, IFluidHandler.FluidAction.EXECUTE);
                playEmptySound(pLevel,worldPosition,pPlayer,currentFluid);
                return true;
            }
            */
            FluidStack transferred = new FluidStack(FluidStack.EMPTY,0);
            if (handlerItem.getFluidInTank(0).getAmount() <= 0){
                GLOGGER.debug("Bucket is empty --- {}, {}", currentFluid.getFluid(),currentFluid.getAmount());
                transferred = tryTransfer(fluidTank, handlerItem, Integer.MAX_VALUE);
            }
            if (!transferred.isEmpty()) {
                GLOGGER.debug("Tank -> bucket --- {}, {}", currentFluid.getFluid(),currentFluid.getAmount());
                playEmptySound(pLevel, worldPosition, pPlayer, transferred);
                return true;
            } else if (fluidTank.isEmpty()
                    && handlerItem.getFluidInTank(0).getAmount() > 0) {
                GLOGGER.debug("Tank is empty --- {}, {}", currentFluid.getFluid(),currentFluid.getAmount());
                // if that failed, try filling the item handler from the TE
                transferred = tryTransfer(handlerItem, fluidTank, Integer.MAX_VALUE);
                if (!transferred.isEmpty()) {
                    GLOGGER.debug("Bucket -> Tank --- {}, {}", currentFluid.getFluid(),currentFluid.getAmount());
                    playFillSound(pLevel, worldPosition, pPlayer, transferred);
                    return true;
                } else return false;
            }
        }

        return false;
    }
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
                        GLOGGER.error("Lost {} fluid during transfer", drainedFluid.getAmount() - actualFill);
                    }
                }
                return drainedFluid;
            }
        }
        return FluidStack.EMPTY;
    }

    private static void playEmptySound(Level world, BlockPos pos, Player player, FluidStack transferred) {
        world.playSound(null, pos, getEmptySound(transferred), SoundSource.BLOCKS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable(KEY_FILLED,
                transferred.getAmount(),
                transferred.getDisplayName()),
                true);
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

    public static float getScale(float prevScale, IFluidTank tank) {
        return getScale(prevScale, tank.getFluidAmount(), tank.getCapacity(), tank.getFluid().isEmpty());
    }
    public static float getScale(float prevScale, int stored, int capacity, boolean empty) {
        return getScale(prevScale, capacity == 0 ? 0 : stored / (float) capacity, empty, stored == capacity);
    }
    public static float getScale(float prevScale, float targetScale, boolean empty, boolean full) {
        float difference = Math.abs(prevScale - targetScale);
        if (difference > 0.01) {
            //GLOGGER.debug("RENDER: "+"difference > 0.01");
            return (9 * prevScale + targetScale) / 10;
        } else if (!empty && full && difference > 0) {
            GLOGGER.debug("RENDER: "+"!empty && full && difference > 0");
            //If we are full but our difference is less than 0.01, but we want to get our scale all the way up to the target
            // instead of leaving it at a value just under. Note: We also check that we are not empty as we technically may
            // be both empty and full if the current capacity is zero
            return targetScale;
        } else if (!empty && prevScale == 0) {
            GLOGGER.debug("RENDER: "+"!empty && prevScale == 0");
            //If we have any contents make sure we end up rendering it
            return targetScale;
        }
        if (empty && prevScale < 0.01) {
            //GLOGGER.debug("RENDER: "+"empty && prevScale < 0.01");
            //If we are empty and have a very small amount just round it down to empty
            return 0;
        }
        return prevScale;
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

    public LazyOptional<FluidTank> getFluidOptional() {
        return this.fluidOptional;
    }

    public FluidTank getFluidTank() {
        return this.fluidTank;
    }
    public FluidStack getFluidStack() {
        return new FluidStack(
                this.fluidTank.getFluid().getFluid(),
                this.fluidTank.getFluidAmount()
        );
    }
}
