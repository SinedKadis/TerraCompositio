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
import net.sinedkadis.terracompositio.block.behaviours.ManySlotItemHandlerBehaviour;
import net.sinedkadis.terracompositio.block.entity.FlowCedarCasingBlockEntity;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.util.TCUtil;

import javax.annotation.ParametersAreNonnullByDefault;

import static net.sinedkadis.terracompositio.block.behaviours.ManySlotItemHandlerBehaviour.hasSpace;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MatterInfuserPortBlock extends MatterInfuserBaseEntityBlock {

    public MatterInfuserPortBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected BlockEntityType<? extends TCBlockEntity> getBlockEntityType() {
        return TCBlockEntities.MATTER_INFUSER_PORT_BE.get();
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        var itemInHand = pPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        BlockPos casingPos = pPos.relative(pState.getValue(FACING).getOpposite());
        FlowCedarCasingBlockEntity casingBE = (FlowCedarCasingBlockEntity) pLevel.getBlockEntity(casingPos);
        if (casingBE != null) {
            ManySlotItemHandlerBehaviour itemBehaviour = ((ManySlotItemHandlerBehaviour) casingBE.getItemBehaviour());
            if (itemBehaviour != null) {
                IItemHandlerModifiable itemHandler = itemBehaviour.getItemHandler();
                int i = FlowCedarCasingBlockEntity.INPUT_INVENTORY_SLOT;
                var slot = itemHandler.getStackInSlot(i);
                if (!slot.isEmpty()) {
                    itemBehaviour.ignoreRestrictions = true;
                    ItemStack extracted = itemHandler.extractItem(i, 64, false);
                    itemBehaviour.ignoreRestrictions = false;
                    TCUtil.addOrDropToPlayer(pPlayer, extracted,true);
                    pLevel.playSound(pPlayer, pPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS);
                    return InteractionResult.SUCCESS;
                }
                if (!itemInHand.isEmpty() && itemBehaviour.allowInsert(i, itemInHand, Direction.UP, true) && hasSpace(itemHandler, i)) {
                    itemBehaviour.ignoreRestrictions = true;
                    ItemStack left = itemHandler.insertItem(i, itemInHand.copy(), false);
                    itemBehaviour.ignoreRestrictions = false;
                    pPlayer.setItemInHand(pHand, left);
                    pLevel.playSound(pPlayer, pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }
}
