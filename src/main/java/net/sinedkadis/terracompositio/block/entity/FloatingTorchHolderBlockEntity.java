package net.sinedkadis.terracompositio.block.entity;

import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemStackHandler;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.block.behaviours.OneSlotItemHandlerBehaviour;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCTags;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;


@Getter
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FloatingTorchHolderBlockEntity extends TCBlockEntity{
    protected static final VoxelShape AABB = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 10.0D, 10.0D);


    protected BlockState holdState = Blocks.AIR.defaultBlockState();
    protected FakeLevel fakeLevel = new FakeLevel();

    public FloatingTorchHolderBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.FLOATING_TORCH_HOLDER_BE.get(), pos, state);

    }

    @Override
    void addBEBehaviours(List<IBEBehaviour> behaviourList) {
        behaviourList.add(new OneSlotItemHandlerBehaviour(this){
            @Override
            public boolean isItemAllowed(int pSlot, ItemStack pStack) {
                return pStack.is(TCTags.Items.TORCHES);
            }

            @Override
            public int getLimitInSlot(int slot) {
                return 1;
            }

            @Override
            public ItemStackHandler getItemHandler() {
                return new ItemStackHandler() {
                    @Override
                    public int getSlotLimit(int slot) {
                        return getLimitInSlot(slot);
                    }

                    @Override
                    public boolean isItemValid(int slot, ItemStack stack) {
                        return isItemAllowed(slot,stack);
                    }

                    @Override
                    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                        ItemStack itemStack = super.insertItem(slot, stack, simulate);
                        getBlockEntity().getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
                            ItemStack stackInSlot = iItemHandler.getStackInSlot(0);
                            if (stackInSlot.isEmpty()) return;
                            Block block = Block.byItem(stackInSlot.getItem());
                            holdState = block.defaultBlockState();

                            //noinspection deprecation
                            block.onPlace(holdState,fakeLevel,getBlockPos(),Blocks.AIR.defaultBlockState(),false);

                        });
                        return itemStack;
                    }

                    @Override
                    public ItemStack extractItem(int slot, int amount, boolean simulate) {
                        getBlockEntity().getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
                            ItemStack stackInSlot = iItemHandler.getStackInSlot(0);
                            if (stackInSlot.isEmpty()) return;
                            Block block = Block.byItem(stackInSlot.getItem());

                            //noinspection deprecation
                            block.onRemove(holdState,fakeLevel,getBlockPos(), Blocks.AIR.defaultBlockState(),false);

                            holdState = Blocks.AIR.defaultBlockState();
                        });
                        return super.extractItem(slot,amount,simulate);
                    }
                };
            }
        });
    }

    protected class FakeLevel extends ServerLevel {

        public FakeLevel() {
            //noinspection DataFlowIssue
            super(null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    false,
                    0,
                    null,
                    false,
                    null);
        }

        @Override
        public BlockState getBlockState(BlockPos pPos) {
            return holdState;
        }

        @Override
        public boolean setBlock(BlockPos pPos, BlockState pNewState, int pFlags) {
            holdState = pNewState;
            return true;
        }

        @Override
        public void scheduleTick(BlockPos pPos, Block pBlock, int pDelay) {
            if (FloatingTorchHolderBlockEntity.this.getLevel() != null) {
                FloatingTorchHolderBlockEntity.this.getLevel().scheduleTick(pPos,pBlock,pDelay);
            }
        }

        @Override
        public void updateNeighborsAt(BlockPos pPos, Block pBlock) {
            if (FloatingTorchHolderBlockEntity.this.getLevel() != null) {
                FloatingTorchHolderBlockEntity.this.getLevel().updateNeighborsAt(pPos,pBlock);
            }
        }

        @Override
        public boolean hasSignal(BlockPos pPos, Direction pDirection) {
            if (FloatingTorchHolderBlockEntity.this.getLevel() != null) {
                return FloatingTorchHolderBlockEntity.this.getLevel().hasSignal(pPos,pDirection);
            }
            return false;
        }

        @Override
        public void levelEvent(int pType, BlockPos pPos, int pData) {
            if (FloatingTorchHolderBlockEntity.this.getLevel() != null) {
                FloatingTorchHolderBlockEntity.this.getLevel().levelEvent(pType,pPos,pData);
            }
        }

        @Override
        public LevelTicks<Block> getBlockTicks() {
            if (FloatingTorchHolderBlockEntity.this.getLevel() instanceof ServerLevel serverLevel) {
                return serverLevel.getBlockTicks();
            }
            //noinspection DataFlowIssue
            return null;
        }

        @Override
        public void addParticle(ParticleOptions pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            if (FloatingTorchHolderBlockEntity.this.getLevel() instanceof ServerLevel serverLevel) {
                serverLevel.addParticle(pParticleData,pX,pY,pZ,pXSpeed,pYSpeed,pZSpeed);
            }
        }
    }
}
