package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.sinedkadis.terracompositio.fluid.ModFluids;
import net.sinedkadis.terracompositio.item.ModItems;

import java.util.Map;
import java.util.function.Predicate;

import static net.sinedkadis.terracompositio.TerraCompositio.GLOGGER;

public class BirchJuiceCauldronBlock extends ModCauldronBlock {


    public BirchJuiceCauldronBlock(Properties pProperties, Predicate<Biome.Precipitation> pFillPredicate, Map<Item, CauldronInteraction> pInteractions) {
        super(pProperties, pFillPredicate, pInteractions);

    }

    @Override
    public boolean canReceiveWedgeDrip(Fluid fluid) {
        return fluid == ModFluids.BIRCH_JUICE_FLUID.source.get();
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack itemStack = pPlayer.getItemInHand(pHand);
        //GLOGGER.debug("Use called, {}, {}", pLevel,pLevel.getBlockEntity(pPos));
        if (pState.getValue(LEVEL) == 3) {
            if (itemStack.getItem() == Items.BUCKET) {
                pLevel.setBlock(pPos, Blocks.CAULDRON.defaultBlockState(), 1);
                if (itemStack.getCount() > 1||pPlayer.isCreative()) {
                    if (!pPlayer.addItem(new ItemStack(ModFluids.BIRCH_JUICE_FLUID.bucket.get()))) {
                        pPlayer.drop(new ItemStack(ModFluids.BIRCH_JUICE_FLUID.bucket.get()), false);
                    }
                    if (!pPlayer.isCreative()) {
                        itemStack.setCount(itemStack.getCount() - 1);
                    }
                } else {
                    if (!pPlayer.isCreative()) {
                        pPlayer.setItemInHand(pHand, new ItemStack(ModFluids.BIRCH_JUICE_FLUID.bucket.get()));
                    }
                }
                pPlayer.playSound(SoundEvents.BUCKET_FILL);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
