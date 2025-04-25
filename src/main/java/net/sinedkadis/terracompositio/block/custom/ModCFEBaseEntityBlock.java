package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.sinedkadis.terracompositio.api.cfe.CFEEntityBlock;

import javax.annotation.Nullable;

public abstract class ModCFEBaseEntityBlock extends Block implements CFEEntityBlock {
    public ModCFEBaseEntityBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> pServerType, BlockEntityType<E> pClientType, BlockEntityTicker<? super E> pTicker) {
        return pClientType == pServerType ? (BlockEntityTicker<A>) pTicker : null;
    }
}
