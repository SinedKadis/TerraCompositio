package net.sinedkadis.terracompositio.compat.create;

import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.sinedkadis.terracompositio.compat.CompatUtils;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCBlocks;

import static com.simibubi.create.api.stress.BlockStressValues.CAPACITIES;

public class TCCreateCompat {
    public static void commonInit() {
        if (!CompatUtils.CREATE_EXISTENCE.get()) return;
        assert TCBlocks.CEDAR_GEARBOX != null;
        CAPACITIES.register(TCBlocks.CEDAR_GEARBOX.get(),() -> 256f);
    }
    public static void clientInit() {
        if (!CompatUtils.CREATE_EXISTENCE.get()) return;

        assert TCBlockEntities.CEDAR_GEARBOX_BE != null;
        SimpleBlockEntityVisualizer.builder(TCBlockEntities.CEDAR_GEARBOX_BE.get())
                .factory(SingleAxisRotatingVisual::shaft)
                .skipVanillaRender(be -> true)
                .apply();
    }

}
