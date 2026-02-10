package net.sinedkadis.terracompositio.api.behaviors.blockentity;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public interface IBEBehaviour {
    default void init(){}
    void tick();
    void onChunkLoad();
    @Nullable LazyOptional<?> getCapability(@NotNull Capability<?> cap, @Nullable Direction side);
    void onRemoved();
    void onInvalidateCaps();
    //Serialisation
    void onSave(CompoundTag compoundTag);
    void onLoad(CompoundTag compoundTag);
    //Jade
    default void onAppendServerData(CompoundTag compoundTag){}
    default void onAppendTooltip(ITooltip iTooltip, CompoundTag serverData, IPluginConfig iPluginConfig){}
    //Block events
    default InteractionResult onUse(@NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit){
        return InteractionResult.PASS;
    }
    //misc
    default void onUpdate(){}
    default void parseBehaviourData(){}

    <T extends BlockEntity> T getBlockEntity();

}
