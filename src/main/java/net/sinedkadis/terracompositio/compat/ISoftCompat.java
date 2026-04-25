package net.sinedkadis.terracompositio.compat;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.client.event.EntityRenderersEvent;

public interface ISoftCompat {
    void init();

    void commonInit();

    void clientInit();

    void registerCreateBER(EntityRenderersEvent.RegisterRenderers event);

    void addCreativeTab(CreativeModeTab.Output output);

    ISoftDataGen getDataGen();
}
