package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.SoundAction;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.entity.FlowExtractorBlockEntity;
import net.sinedkadis.terracompositio.block.entity.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

import static net.sinedkadis.terracompositio.TerraCompositio.GLOGGER;

public class FlowExtractorBlock extends BaseEntityBlock {
    public FlowExtractorBlock(Properties properties) {
        super(properties);
    }
    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }
    private static final String KEY_FILLED = TerraCompositio.makeDescriptionId("block", "tank.filled");
    private static final String KEY_DRAINED = TerraCompositio.makeDescriptionId("block", "tank.drained");

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Shapes.join(
                Shapes.join(
                Block.box(0,2,0,2,16,16),
                Block.box(2,2,14,16,16,16),
                BooleanOp.OR),
                Shapes.join(
                        Shapes.join(
                                Block.box(14,2,2,16,16,14),
                                Block.box(2,2,0,16,16,2),
                                BooleanOp.OR),
                        Block.box(2,2,2,14,4,14),
                        BooleanOp.OR),
                BooleanOp.OR);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return ModBlockEntities.FLOW_EXTRACTOR_BE.get().create(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }
        return createTickerHelper(pBlockEntityType, ModBlockEntities.FLOW_EXTRACTOR_BE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1,pPos,pState1));
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!world.isClientSide) {
            // Проверка на наличие IFluidHandlerItem у предмета
            itemstack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(fluidHandlerItem -> {
                GLOGGER.debug("Fluid Handler Item is present: {}",fluidHandlerItem);
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity != null) {
                    blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(fluidHandler -> {
                        GLOGGER.debug("Fluid Handler Block is present: {}",fluidHandler);
                        // Ваш код для взаимодействия контейнера с блоком
                        // Например, передача жидкости между контейнером и блоком
                        FluidStack fluidInItem = fluidHandlerItem.getFluidInTank(0);
                        FluidStack fluidInBlock = fluidHandler.getFluidInTank(0);
                        boolean fluidTransferred = false;

                        if (!fluidInItem.isEmpty() && fluidHandler.fill(fluidInItem, IFluidHandler.FluidAction.SIMULATE) > 0) {
                            fluidHandler.fill(fluidInItem, IFluidHandler.FluidAction.EXECUTE);
                            fluidHandlerItem.drain(fluidInItem, IFluidHandler.FluidAction.EXECUTE);
                            fluidTransferred = true;
                            playFillSound(world,pos,player,fluidInItem);
                            GLOGGER.debug("Fluid transferred to block");
                        } else if (!fluidInBlock.isEmpty() && fluidHandlerItem.fill(fluidInBlock, IFluidHandler.FluidAction.SIMULATE) > 0) {
                            fluidHandlerItem.fill(fluidInBlock, IFluidHandler.FluidAction.EXECUTE);
                            fluidHandler.drain(fluidInBlock, IFluidHandler.FluidAction.EXECUTE);
                            fluidTransferred = true;
                            playEmptySound(world,pos,player,fluidInBlock);
                            GLOGGER.debug("Fluid transferred to item");
                        }
                        if (fluidTransferred) {
                            // Обновление состояния предмета в руке игрока
                            player.setItemInHand(hand, fluidHandlerItem.getContainer());
                        }
                    });
                }
            });
        }

        return InteractionResult.sidedSuccess(world.isClientSide);
    }
    private static void playEmptySound(Level world, BlockPos pos, Player player, FluidStack transferred) {
        world.playSound(null, pos, getEmptySound(transferred), SoundSource.BLOCKS, 1.0F, 1.0F);
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable(KEY_FILLED, transferred.getAmount(), transferred.getDisplayName()), true);
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
}
