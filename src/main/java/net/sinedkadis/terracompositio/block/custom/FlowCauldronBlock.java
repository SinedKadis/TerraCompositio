package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.sinedkadis.terracompositio.registries.TCFluids;
import net.sinedkadis.terracompositio.registries.TCItems;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.function.Predicate;

public class FlowCauldronBlock extends TCCauldronBlock {


    public FlowCauldronBlock(Properties pProperties, Predicate<Biome.Precipitation> pFillPredicate, Map<Item, CauldronInteraction> pInteractions) {
        super(pProperties, pFillPredicate, pInteractions);

    }

    @Override
    public boolean canReceiveWedgeDrip(Fluid fluid) {
        return fluid == TCFluids.FLOW_FLUID.source.get();
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void handleEntityOnFireInside(BlockState pState, Level pLevel, BlockPos pPos) {

    }

    @Override
    @ParametersAreNonnullByDefault
    public @NotNull InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack itemStack = pPlayer.getItemInHand(pHand);
        if (pState.getValue(LEVEL) == 3) {
            if (itemStack.getItem() == Items.BUCKET) {
                pLevel.setBlock(pPos, Blocks.CAULDRON.defaultBlockState(), 1);
                if (itemStack.getCount() > 1||pPlayer.isCreative()) {
                    if (!pPlayer.addItem(new ItemStack(TCFluids.FLOW_FLUID.bucket.get()))) {
                        pPlayer.drop(new ItemStack(TCFluids.FLOW_FLUID.bucket.get()), false);
                    }
                    if (!pPlayer.isCreative()) {
                        itemStack.setCount(itemStack.getCount() - 1);
                    }
                } else {
                    if (!pPlayer.isCreative()) {
                        pPlayer.setItemInHand(pHand, new ItemStack(TCFluids.FLOW_FLUID.bucket.get()));
                    }
                }
                pPlayer.playSound(SoundEvents.BUCKET_FILL);
                return InteractionResult.SUCCESS;
            }
        }
        if (itemStack.getItem() == Items.GLASS_BOTTLE) {
            if (pState.getValue(LEVEL) != 1) {
                pLevel.setBlock(pPos, pState.setValue(LEVEL, pState.getValue(LEVEL) - 1), 1);
                if (itemStack.getCount() > 1) {
                    if (!pPlayer.addItem(new ItemStack(TCItems.FLOW_BOTTLE.get()))) {
                        pPlayer.drop(new ItemStack(TCItems.FLOW_BOTTLE.get()), false);
                    }
                    if (!pPlayer.isCreative()) {
                        itemStack.setCount(itemStack.getCount() - 1);
                    }
                } else {
                    pPlayer.setItemInHand(pHand, new ItemStack(TCItems.FLOW_BOTTLE.get()));
                }
                pPlayer.playSound(SoundEvents.BOTTLE_FILL);
            } else {
                pLevel.setBlock(pPos, Blocks.CAULDRON.defaultBlockState(), 1);
                if (itemStack.getCount() > 1) {
                    if (!pPlayer.addItem(new ItemStack(TCItems.FLOW_BOTTLE.get()))) {
                        pPlayer.drop(new ItemStack(TCItems.FLOW_BOTTLE.get()), false);
                    }
                    if (!pPlayer.isCreative()) {
                        itemStack.setCount(itemStack.getCount() - 1);
                    }
                } else {
                    pPlayer.setItemInHand(pHand, new ItemStack(TCItems.FLOW_BOTTLE.get()));
                }
                pPlayer.playSound(SoundEvents.BOTTLE_FILL);
            }
            return InteractionResult.SUCCESS;
        }



        return InteractionResult.PASS;
    }
}
