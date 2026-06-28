package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.sinedkadis.terracompositio.api.helpers.PlayerHelper;
import net.sinedkadis.terracompositio.block.behaviours.ItemHandlerBehaviour;
import net.sinedkadis.terracompositio.block.entity.FlowCedarCasingBlockEntity;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;

import javax.annotation.ParametersAreNonnullByDefault;

import static net.sinedkadis.terracompositio.block.behaviours.ItemHandlerBehaviour.hasSpace;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MatterInfuserPortBlock extends MatterInfuserBaseEntityBlock {

    public MatterInfuserPortBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public BlockEntityType<? extends TCBlockEntity> getBlockEntityType() {
        return TCBlockEntities.MATTER_INFUSER_PORT_BE.get();
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        var itemInHand = pPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        BlockPos casingPos = pPos.relative(pState.getValue(FACING).getOpposite());
        FlowCedarCasingBlockEntity casingBE = (FlowCedarCasingBlockEntity) pLevel.getBlockEntity(casingPos);
        if (casingBE != null) {
            ItemHandlerBehaviour itemBehaviour = casingBE.getItemBehaviours().stream()
                    .filter(ItemHandlerBehaviour.class::isInstance)
                    .map(ItemHandlerBehaviour.class::cast)
                    .findAny().orElse(null);
            if (itemBehaviour != null) {
                IItemHandlerModifiable itemHandler = itemBehaviour.getItemHandler();
                int i = FlowCedarCasingBlockEntity.INPUT_INVENTORY_SLOT;
                ItemStack stackInSlot = itemHandler.getStackInSlot(i);
                if (!stackInSlot.isEmpty()) {

                    itemHandler.setStackInSlot(i, ItemStack.EMPTY);

                    PlayerHelper.addOrDropToPlayer(pPlayer, stackInSlot, true);
                    pLevel.playSound(pPlayer, pPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS);
                    return InteractionResult.SUCCESS;
                }
                if (!itemInHand.isEmpty() && itemBehaviour.allowInsert(i, itemInHand, Direction.UP, true)
                        && hasSpace(itemHandler, i)
                        && (ItemStack.isSameItem(itemInHand, stackInSlot) || stackInSlot.isEmpty())) {


                    int count = itemInHand.getCount() + stackInSlot.getCount();
                    int left = count - 64;
                    ItemStack handCopy = itemInHand.copy();
                    ItemStack storageCopy = stackInSlot.copy();
                    if (left > 0) {
                        storageCopy.setCount(64);
                        handCopy.setCount(left);
                    } else {
                        if (stackInSlot.isEmpty()) storageCopy = handCopy;
                        storageCopy.setCount(count);
                        handCopy = ItemStack.EMPTY;
                    }
                    itemHandler.setStackInSlot(i, storageCopy);
                    pPlayer.setItemInHand(pHand, handCopy);
                    pLevel.playSound(pPlayer, pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }
}
