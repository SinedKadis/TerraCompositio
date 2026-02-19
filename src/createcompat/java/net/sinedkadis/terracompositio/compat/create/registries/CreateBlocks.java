package net.sinedkadis.terracompositio.compat.create.registries;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.compat.CompatUtils;
import net.sinedkadis.terracompositio.compat.create.block.custom.CedarGearboxBlock;
import net.sinedkadis.terracompositio.registries.TCBlocks;

public class CreateBlocks {
    //Compat
    public final RegistryObject<Block> CEDAR_GEARBOX = TCBlocks.registerBlock("cedar_gearbox",
            () -> new CedarGearboxBlock(BlockBehaviour.Properties.copy(TCBlocks.FLOW_CEDAR_LOG.get())), CompatUtils.CREATE_EXISTENCE);
}
