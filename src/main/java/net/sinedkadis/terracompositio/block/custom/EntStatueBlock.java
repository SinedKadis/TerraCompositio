package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.sinedkadis.terracompositio.block.IFluidApplicable;
import net.sinedkadis.terracompositio.block.entity.EntStatueBlockEntity;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCFluids;
import net.sinedkadis.terracompositio.registries.TCItems;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EntStatueBlock extends TCBaseEntityBlock implements IFluidApplicable {


    public EntStatueBlock(Properties pProperties) {
        super(pProperties);
    }
    @Override
    protected BlockEntityType<? extends TCBlockEntity> getBlockEntityType() {
        return TCBlockEntities.ENT_STATUE_BE.get();
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Block.box(4,0,4,12,20,12);
    }

    @SuppressWarnings("deprecation")
    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public FluidApplyResult tryApply(Level level, BlockPos blockPos, ItemStack itemStack, IFluidHandlerItem iFluidHandlerItem, Player player) {
        EntStatueBlockEntity blockEntity = ((EntStatueBlockEntity) level.getBlockEntity(blockPos));
        FluidStack resource = new FluidStack(TCFluids.FLOW_FLUID.source.get(), 500);
        FluidStack result = iFluidHandlerItem.drain(resource, IFluidHandler.FluidAction.SIMULATE);
        if (blockEntity != null && result.getAmount() >= 500) {
            blockEntity.jojoReference();
            iFluidHandlerItem.drain(500, IFluidHandler.FluidAction.EXECUTE);
            return FluidApplyResult.SUCCESS;
        }
        return FluidApplyResult.SKIP;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pPlayer.getItemInHand(pHand).is(TCItems.FLUID_APPLIER.get())) return InteractionResult.PASS;
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }
}
