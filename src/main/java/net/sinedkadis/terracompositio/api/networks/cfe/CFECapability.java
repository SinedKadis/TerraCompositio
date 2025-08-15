package net.sinedkadis.terracompositio.api.networks.cfe;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;

@Mod.EventBusSubscriber(modid = TerraCompositioAPI.MOD_ID,bus = Mod.EventBusSubscriber.Bus.MOD)
public class CFECapability {
    public static final Capability<ICFEHandler> CFE = CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(ICFEHandler.class);
    }
}
