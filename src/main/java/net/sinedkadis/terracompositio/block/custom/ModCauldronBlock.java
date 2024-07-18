package net.sinedkadis.terracompositio.block.custom;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.material.Fluid;

import java.util.Map;
import java.util.function.Predicate;

public abstract class ModCauldronBlock extends LayeredCauldronBlock {
    public ModCauldronBlock(Properties pProperties, Predicate<Biome.Precipitation> pFillPredicate, Map<Item, CauldronInteraction> pInteractions) {
        super(pProperties, pFillPredicate, pInteractions);
    }
    public boolean canReceiveWedgeDrip(Fluid fluid){
        return false;
    }
    //public abstract void WedgeDripReceived();
}
