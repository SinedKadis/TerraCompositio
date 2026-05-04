package net.sinedkadis.terracompositio.block.custom;

import lombok.NonNull;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.block.entity.TCBlockEntity;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PathPointerBlock extends TCBaseEntityBlock {


    private final PathPointerBlockEntity.PPPart basePart;

    public PathPointerBlock(Properties pProperties, PathPointerBlockEntity.PPPart part) {
        super(pProperties);
        basePart = part;

    }

    @SuppressWarnings("deprecation")
    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        PathPointerBlockEntity blockEntity = (PathPointerBlockEntity) pLevel.getBlockEntity(pPos);

        if (blockEntity != null && blockEntity.parts.contains(PathPointerBlockEntity.PPPart.EMITTER)){
            return Block.box(1, 2, 1, 15, 14, 15);
        }
        return Block.box(3, 3, 3, 13, 13, 13);

    }

    @Override
    public String getDescriptionId() {
        return super.getDescriptionId();
    }


    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Direction orientation = placer == null ? Direction.WEST : Direction.orderedByNearest(placer)[0].getOpposite();
        PathPointerBlockEntity pp = (PathPointerBlockEntity) world.getBlockEntity(pos);
        if (pp == null) return;

        switch (orientation) {
            case DOWN -> pp.rotationPitch = -90F;
            case UP -> pp.rotationPitch = 90F;
            case NORTH -> pp.rotationYaw = 270F;
            case SOUTH -> pp.rotationYaw = 90F;
            case WEST -> {}
            case EAST -> pp.rotationYaw = 180F;
        }


    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack hand = pPlayer.getItemInHand(pHand);
        PathPointerBlockEntity pp = (PathPointerBlockEntity) pLevel.getBlockEntity(pPos);
        PathPointerBlockEntity.PPPart newPart = getPart(hand);
        if (!pPlayer.isCrouching()
                && pp != null) {
            int replacePart = pp.parts.indexOf(PathPointerBlockEntity.PPPart.NONE);
            if (replacePart >= 0) {
                PathPointerBlockEntity.PPPart oPart = pp.parts.get(replacePart == 0 ? 1 : 0);
                if (oPart.isInput() != newPart.isInput()) {
                    pp.parts.set(replacePart, newPart);
                    PathPointerBlockEntity.update(pp);
                    if (!pPlayer.isCreative()) {
                        hand.shrink(1);
                    }
                    pLevel.playSound(null, pPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS);
                    return InteractionResult.SUCCESS;

                }
            }
        }

        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    private static PathPointerBlockEntity.PPPart getPart(ItemStack hand) {
        if (hand.getItem() instanceof BlockItem item && item.getBlock() instanceof PathPointerBlock block){
            return block.getBasePart();
        }
        return PathPointerBlockEntity.PPPart.NONE;
    }

    public PathPointerBlockEntity.@NonNull PPPart getBasePart() {
        return this.basePart;
    }


    @Override
    protected BlockEntityType<? extends TCBlockEntity> getBlockEntityType() {
        return TCBlockEntities.PATH_POINTER_BE.get();
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
        List<ItemStack> drops = super.getDrops(pState, pParams);

        Entity entity = pParams.getOptionalParameter(LootContextParams.KILLER_ENTITY);
        if (!(entity instanceof Player player) || !player.isCreative()) {
            PathPointerBlockEntity blockEntity = (PathPointerBlockEntity) pParams.getParameter(LootContextParams.BLOCK_ENTITY);
            ItemStack toDrop = new ItemStack(switch (blockEntity.parts.get(1)) {
                case COLLECTOR -> TCBlocks.PP_COLLECTOR.get();
                case SENDER -> TCBlocks.PP_SENDER.get();
                case EMITTER -> TCBlocks.PP_EMITTER.get();
                case RECEIVER -> TCBlocks.PP_RECEIVER.get();
                case EXTRACTOR -> TCBlocks.PP_EXTRACTOR.get();
                case INFUSER -> TCBlocks.PP_INFUSER.get();
                default -> Blocks.AIR;
            });
            drops.add(toDrop);
        }

        return drops;
    }

}
