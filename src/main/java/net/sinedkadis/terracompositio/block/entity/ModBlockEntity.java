package net.sinedkadis.terracompositio.block.entity;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ModBlockEntity extends BlockEntity {
    private AABB renderBoundingBox;
    private boolean virtualMode;
    //private final Map<BehaviourType<?>, BlockEntityBehaviour> behaviours = new HashMap<>();
    private boolean initialized = false;
    private boolean firstNbtRead = true;
    protected int lazyTickRate;
    protected int lazyTickCounter;
    private boolean chunkUnloaded;
    public ModBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void markVirtual() {
        virtualMode = true;
    }

    public boolean isVirtual() {
        return virtualMode;
    }
    @Override
    public CompoundTag getUpdateTag() {
        return writeClient(new CompoundTag());
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        readClient(tag);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        readClient(tag == null ? new CompoundTag() : tag);
    }

    // Special handling for client update packets
    public void readClient(CompoundTag tag) {
        load(tag);
    }

    // Special handling for client update packets
    public CompoundTag writeClient(CompoundTag tag) {
        saveAdditional(tag);
        return tag;
    }

    public void sendData() {
        if (level instanceof ServerLevel serverLevel)
            serverLevel.getChunkSource().blockChanged(getBlockPos());
    }

    public PacketDistributor.PacketTarget packetTarget() {
        return PacketDistributor.TRACKING_CHUNK.with(this::containedChunk);
    }

    public LevelChunk containedChunk() {
        return level.getChunkAt(worldPosition);
    }

    @SuppressWarnings("deprecation")
    public HolderGetter<Block> blockHolderGetter() {
        return (HolderGetter<Block>) (level != null ? level.holderLookup(Registries.BLOCK)
                : BuiltInRegistries.BLOCK.asLookup());
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        if (renderBoundingBox == null) {
            renderBoundingBox = createRenderBoundingBox();
        }
        return renderBoundingBox;
    }

    protected void invalidateRenderBoundingBox() {
        renderBoundingBox = null;
    }

    protected AABB createRenderBoundingBox() {
        return super.getRenderBoundingBox();
    }
    public void sendUpdate() {
        setChanged();

        if (this.level != null)
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }
}