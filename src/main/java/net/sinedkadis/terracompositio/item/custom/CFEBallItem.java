package net.sinedkadis.terracompositio.item.custom;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.sinedkadis.terracompositio.entity.custom.CFEBallProjectileEntity;

import javax.annotation.ParametersAreNonnullByDefault;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CFEBallItem extends Item {

    public static DispenseItemBehavior CFE_BALL_DISPENSER_BEHAVIOUR = (pSource, pStack) -> {
        ServerLevel level = pSource.getLevel();
        level.playSound(null, pSource.x(), pSource.y(), pSource.z(),
                SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
     //   if (!level.isClientSide) {
            CFEBallProjectileEntity cfeBall = new CFEBallProjectileEntity(pSource);
            cfeBall.setItem(pStack);
            Direction direction = pSource.getBlockState().getValue(BlockStateProperties.FACING);

            BlockPos target = BlockPos.ZERO.relative(direction);
            cfeBall.shoot(target.getX(), target.getY(), target.getZ(), 1, 0.01f);
            level.addFreshEntity(cfeBall);
      //  }
        pStack.shrink(1);
        return pStack;
    };

    public CFEBallItem(Properties pProperties) {
        super(pProperties);
        DispenserBlock.registerBehavior(this,CFE_BALL_DISPENSER_BEHAVIOUR);
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));

        if (!pLevel.isClientSide) {
            CFEBallProjectileEntity cfeBall = new CFEBallProjectileEntity(pLevel, pPlayer);
            cfeBall.setItem(itemstack);
            cfeBall.shootFromRotation(pPlayer, pPlayer.getXRot(), pPlayer.getYRot(), 0.0F, 1.0F, 1.0F);
            pLevel.addFreshEntity(cfeBall);
        }

        pPlayer.awardStat(Stats.ITEM_USED.get(this));
        if (!pPlayer.getAbilities().instabuild) {
            itemstack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
    }
}
