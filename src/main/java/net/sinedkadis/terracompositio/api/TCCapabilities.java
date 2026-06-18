package net.sinedkadis.terracompositio.api;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;

@Mod.EventBusSubscriber(modid = TerraCompositioAPI.MOD_ID,bus = Mod.EventBusSubscriber.Bus.MOD)
public class TCCapabilities {
    public static final Capability<ICFEHandler> CFE = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<IItemHandler> ITEM_STATE_HOLDER = CapabilityManager.get(new CapabilityToken<>() {
    });

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(ICFEHandler.class);
    }
}
