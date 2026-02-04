package net.sinedkadis.terracompositio.api.behaviors.blockentity;

import net.minecraft.nbt.CompoundTag;

public interface IPPBEBehaviour extends IBEBehaviour {
    default void onTagUpdate(CompoundTag compoundTag){}
}
