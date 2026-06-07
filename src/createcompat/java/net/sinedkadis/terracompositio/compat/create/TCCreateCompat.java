package net.sinedkadis.terracompositio.compat.create;

import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.ModList;
import net.sinedkadis.terracompositio.compat.create.datagen.DataGenerators;
import net.sinedkadis.terracompositio.compat.create.registries.CreateBlockEntities;
import net.sinedkadis.terracompositio.compat.create.registries.CreateBlocks;
import net.sinedkadis.terracompositio.compat.soft_compat.ISoftCompat;
import net.sinedkadis.terracompositio.compat.soft_compat.ISoftDataGen;
import net.sinedkadis.terracompositio.config.TCServerConfigs;

import static com.simibubi.create.api.stress.BlockStressValues.CAPACITIES;
import static com.simibubi.create.api.stress.BlockStressValues.RPM;

public class TCCreateCompat implements ISoftCompat {
    public static final String MOD_ID = "terracompositio_cc";

    public CreateBlocks blocks;
    public CreateBlockEntities blockEntities;
    public DataGenerators dataGen;

    @Override
    public void init() {
        blocks = new CreateBlocks();
        blockEntities = new CreateBlockEntities();
        dataGen = new DataGenerators();
    }

    @Override
    public void commonInit() {
        assert blocks.CEDAR_GEARBOX != null;
        Block cedarGearBox = blocks.CEDAR_GEARBOX.get();
        CAPACITIES.register(cedarGearBox, () -> TCServerConfigs.STRESS_UNIT_GEN.get() / 8f);
        RPM.register(cedarGearBox, new BlockStressValues.GeneratedRpm(4096, true));
    }
    @Override
    public void clientInit() {
        assert blockEntities.CEDAR_GEARBOX_BE != null;
        SimpleBlockEntityVisualizer.builder(blockEntities.CEDAR_GEARBOX_BE.get())
                .factory(SingleAxisRotatingVisual::shaft)
                .skipVanillaRender(be -> true)
                .apply();
    }


    @Override
    public void registerCreateBER(EntityRenderersEvent.RegisterRenderers event) {
        if (ModList.get().isLoaded("create")) {
            assert blockEntities.CEDAR_GEARBOX_BE != null;
            event.registerBlockEntityRenderer(
                    blockEntities.CEDAR_GEARBOX_BE.get(),
                    KineticBlockEntityRenderer::new
            );
        }


    }

    @Override
    public void addCreativeTab(CreativeModeTab.Output output) {
        assert blocks.CEDAR_GEARBOX != null;
        output.accept(blocks.CEDAR_GEARBOX.get());
    }

    @Override
    public ISoftDataGen getDataGen() {
        return dataGen;
    }


}
