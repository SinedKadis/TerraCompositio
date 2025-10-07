package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.sinedkadis.terracompositio.block.IFluidApplicable;
import net.sinedkadis.terracompositio.block.entity.EntStatueBlockEntity;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCFluids;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EntStatueBlock extends TCIOBaseEntityBlock implements IFluidApplicable {


    public EntStatueBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return TCBlockEntities.ENT_STATUE_BE.get().create(pPos,pState);
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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }
        return createTickerHelper(pBlockEntityType, TCBlockEntities.ENT_STATUE_BE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1,pPos,pState1));
    }

    @Override
    public FluidApplyResult tryApply(Level level, BlockPos blockPos, ItemStack itemStack,IFluidHandlerItem iFluidHandlerItem) {
        EntStatueBlockEntity blockEntity = ((EntStatueBlockEntity) level.getBlockEntity(blockPos));
        FluidStack resource = new FluidStack(TCFluids.FLOW_FLUID.source.get(), 500);
        FluidStack result = iFluidHandlerItem.drain(resource, IFluidHandler.FluidAction.SIMULATE);
        if (blockEntity != null && result.getAmount() == 500) {
            blockEntity.jojoReference();
            iFluidHandlerItem.drain(resource, IFluidHandler.FluidAction.EXECUTE);
            return FluidApplyResult.SUCCESS;
        }
        return FluidApplyResult.SKIP;
    }
}
