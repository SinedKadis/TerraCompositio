package net.sinedkadis.terracompositio.events;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.entity.custom.FlowCedarEntEntity;
import net.sinedkadis.terracompositio.registries.TCEntities;
import net.sinedkadis.terracompositio.particle.custom.*;

@Mod.EventBusSubscriber(modid = TerraCompositio.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TCEventBusEvents {


    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event){
        event.put(TCEntities.FLOW_CEDAR_ENT.get(), FlowCedarEntEntity.createAttributes().build());
    }
}
