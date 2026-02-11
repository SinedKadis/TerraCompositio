package net.sinedkadis.terracompositio.util;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.block.behaviours.pp.ReceiverBehaviour;
import net.sinedkadis.terracompositio.block.behaviours.pp.SenderBehaviour;

@Mod.EventBusSubscriber(modid = TerraCompositioAPI.MOD_ID,bus = Mod.EventBusSubscriber.Bus.MOD)
public class BehaviourCapabilities {
    public static final Capability<SenderBehaviour> SENDER = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<ReceiverBehaviour> RECEIVER = CapabilityManager.get(new CapabilityToken<>() {});


    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(SenderBehaviour.class);
        event.register(ReceiverBehaviour.class);
    }

}
