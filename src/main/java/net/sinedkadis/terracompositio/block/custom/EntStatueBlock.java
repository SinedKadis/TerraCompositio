package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.sinedkadis.terracompositio.registries.TCItems;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EntStatueBlock extends Block {
    public static final BooleanProperty CROWN_EQUIPPED = BooleanProperty.create("crown_equipped");


    public EntStatueBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(defaultBlockState()
                .setValue(CROWN_EQUIPPED,false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(CROWN_EQUIPPED);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack hand = pPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        //todo NBT saving
        if (hand.isEmpty() && pState.getValue(CROWN_EQUIPPED)) {
            pPlayer.setItemInHand(InteractionHand.MAIN_HAND,TCItems.TECHNETIUM_CROWN.get().getDefaultInstance());
            pLevel.setBlockAndUpdate(pPos,pState.setValue(CROWN_EQUIPPED,false)) ;
            return InteractionResult.SUCCESS;
        }

        if (hand.is(TCItems.TECHNETIUM_CROWN.get()) && !pState.getValue(CROWN_EQUIPPED)) {
            hand.shrink(1);
            pLevel.setBlockAndUpdate(pPos,pState.setValue(CROWN_EQUIPPED,true));
            return InteractionResult.SUCCESS;
        }


        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }
}
