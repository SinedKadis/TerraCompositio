package net.sinedkadis.terracompositio.worldgen.tree;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.worldgen.tree.custom.FlowCedarFoliagePlacer;

public class ModFoliagePlacers {
    public static final DeferredRegister<FoliagePlacerType<?>> FOLIAGE_PLACERS =
            DeferredRegister.create(Registries.FOLIAGE_PLACER_TYPE, TerraCompositio.MOD_ID);

    public static final RegistryObject<FoliagePlacerType<FlowCedarFoliagePlacer>> FLOW_CEDAR_FOLIAGE_PLACER =
            FOLIAGE_PLACERS.register("big_flow_cedar_foliage_placer", () -> new FoliagePlacerType<>(FlowCedarFoliagePlacer.CODEC));

    public static void register(IEventBus eventBus) {
        FOLIAGE_PLACERS.register(eventBus);
    }
}