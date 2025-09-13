package net.sinedkadis.terracompositio.block.custom;

import lombok.Getter;
import lombok.NonNull;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
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
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCBlockStateProperties;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PathPointerBlock extends TCCFEBaseEntityBlock {

    protected static final EnumProperty<PPPart> BASE_PART;
    protected static final EnumProperty<PPPart> ADDITIONAL_PART;

    public PathPointerBlock(Properties pProperties, PPPart part) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(BASE_PART,part).setValue(ADDITIONAL_PART, PPPart.NONE));

    }

    @SuppressWarnings("deprecation")
    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(BASE_PART,ADDITIONAL_PART);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        PPPart base = pState.getValue(TCBlockStateProperties.BASE_PART);
        PPPart add = pState.getValue(TCBlockStateProperties.ADDITIONAL_PART);
        if (base.equals(PPPart.EMITTER)||add.equals(PPPart.EMITTER)){
            return Block.box(1, 2, 1, 15, 14, 15);
        }
        return Block.box(3, 3, 3, 13, 13, 13);

    }

    @Override
    public String getDescriptionId() {
        return super.getDescriptionId();
    }

    public boolean addPart(Level level, BlockPos pos, @NonNull PathPointerBlock.PPPart part){
        BlockState state = level.getBlockState(pos);
        if (part == PPPart.NONE) return false;
        if (!state.hasProperty(ADDITIONAL_PART)) return false;
        if (state.getValue(ADDITIONAL_PART) != PPPart.NONE) return false;

        assert state.hasProperty(BASE_PART);
        PPPart base = state.getValue(BASE_PART);

        if (base.isInput() != part.isInput()){
            level.setBlockAndUpdate(pos,state.setValue(ADDITIONAL_PART,part));
            return true;
        }
        return false;
    }

    public static PPPart[] getParts(BlockState state){
        if (state.hasProperty(BASE_PART)){
            assert state.hasProperty(ADDITIONAL_PART);
            PPPart base = state.getValue(BASE_PART);
            PPPart add = state.getValue(ADDITIONAL_PART);
            return new PPPart[]{base, add};
        }
        return new PPPart[]{};
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
        pp.updateContainer();

    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }
        return createTickerHelper(pBlockEntityType, TCBlockEntities.PATH_POINTER_BE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1,pPos,pState1));
    }



    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack hand = pPlayer.getItemInHand(pHand);

        if (!pPlayer.isCrouching()) {
            if (addPart(pLevel, pPos, getPart(hand))) {
                if (!pPlayer.isCreative()) {
                    hand.shrink(1);
                }
                PathPointerBlockEntity pp = (PathPointerBlockEntity) pLevel.getBlockEntity(pPos);
                if (pp != null) {
                    pp.updateContainer();
                }
                return InteractionResult.SUCCESS;
            }
        }

        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    private static @NonNull PathPointerBlock.PPPart getPart(ItemStack hand) {
        if (hand.getItem() instanceof BlockItem item && item.getBlock() instanceof PathPointerBlock block){
            return block.defaultBlockState().getValue(BASE_PART);
        }
        return PPPart.NONE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return TCBlockEntities.PATH_POINTER_BE.get().create(blockPos,blockState);
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
        List<ItemStack> drops = super.getDrops(pState, pParams);
        PPPart value = pState.getValue(ADDITIONAL_PART);
        Entity killer = pParams.getOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY);
        if (!value.equals(PPPart.NONE)) {
            boolean validKiller = !(killer instanceof Player)
                    || !((Player) killer).isCreative();
            if (validKiller) {
                for (RegistryObject<Block> block : TCBlocks.BLOCKS.getEntries().stream()
                        .filter(blockRegistryObject ->
                                blockRegistryObject.get() instanceof PathPointerBlock).toList()) {
                    BlockState blockState = block.get().defaultBlockState();
                    if (value.equals(blockState.getValue(BASE_PART))) {
                        drops.add(block.get().asItem().getDefaultInstance());
                    }
                }
            }
        }
        return drops;
    }




    public enum PPPart implements StringRepresentable {
        COLLECTOR(true, "collector"),
        RECEIVER(true, "receiver"),
        SENDER(false, "sender"),
        EMITTER(false, "emitter"),
        NONE(false, "none");

        @Getter
        private final boolean input;
        private final String name;

        PPPart(boolean isInput, String name) {
            this.input = isInput;
            this.name = name;
        }

        @Override
        public String toString() {
            return "PPParts{" +
                    "name='" + name + '\'' +
                    '}';
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }



    static {
        BASE_PART = TCBlockStateProperties.BASE_PART;
        ADDITIONAL_PART = TCBlockStateProperties.ADDITIONAL_PART;
    }
}
