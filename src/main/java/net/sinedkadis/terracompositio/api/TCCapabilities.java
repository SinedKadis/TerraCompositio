package net.sinedkadis.terracompositio.api;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.sinedkadis.terracompositio.api.networks.cfe.IECFHandler;

@Mod.EventBusSubscriber(modid = TerraCompositioAPI.MOD_ID,bus = Mod.EventBusSubscriber.Bus.MOD)
public class TCCapabilities {
    public static final Capability<IECFHandler> ECF = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<IItemHandlerModifiable> ITEM_STATE_HOLDER = CapabilityManager.get(new CapabilityToken<>() {
    });

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IECFHandler.class);
    }
}
