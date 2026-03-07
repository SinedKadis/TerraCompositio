package net.sinedkadis.terracompositio.compat.create;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.sinedkadis.terracompositio.compat.CompatUtils;
import net.sinedkadis.terracompositio.compat.ISoftCompat;
import net.sinedkadis.terracompositio.compat.create.registries.CreateBlockEntities;
import net.sinedkadis.terracompositio.compat.create.registries.CreateBlocks;

import static com.simibubi.create.api.stress.BlockStressValues.CAPACITIES;

public class TCCreateCompat implements ISoftCompat {
    public CreateBlocks blocks;
    public CreateBlockEntities blockEntities;

    @Override
    public void init() {
        blocks = new CreateBlocks();
        blockEntities = new CreateBlockEntities();
    }

    @Override
    public void commonInit() {
        assert blocks.CEDAR_GEARBOX != null;
        CAPACITIES.register(blocks.CEDAR_GEARBOX.get(),() -> 256f);
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
        if (CompatUtils.CREATE_EXISTENCE.get()) {
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


}
