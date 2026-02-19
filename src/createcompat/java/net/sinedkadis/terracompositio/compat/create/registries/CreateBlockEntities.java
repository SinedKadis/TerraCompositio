package net.sinedkadis.terracompositio.compat.create.registries;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.compat.CompatUtils;
import net.sinedkadis.terracompositio.compat.create.TCCreateCompat;
import net.sinedkadis.terracompositio.compat.create.block.entity.CedarGearboxBlockEntity;

import static net.sinedkadis.terracompositio.registries.TCBlockEntities.registerBE;

public class CreateBlockEntities {
    @SuppressWarnings("unchecked")
    public final RegistryObject<BlockEntityType<CedarGearboxBlockEntity>> CEDAR_GEARBOX_BE =
            registerBE("cedar_gearbox_be",CedarGearboxBlockEntity::new, CompatUtils.CREATE_EXISTENCE, ((TCCreateCompat) TerraCompositio.createCompat).blocks.CEDAR_GEARBOX);
}
