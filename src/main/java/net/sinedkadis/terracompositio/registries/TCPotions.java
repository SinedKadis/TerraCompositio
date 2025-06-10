package net.sinedkadis.terracompositio.registries;

import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.sinedkadis.terracompositio.TerraCompositio;

public class TCPotions {
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(ForgeRegistries.POTIONS, TerraCompositio.MOD_ID);

    //public static final RegistryObject<Potion> FLOW_BOTTLE = POTIONS.register("flow_bottle",
    //        () -> new FlowBottleItem(new Item.Properties()))

    public static void register(IEventBus eventBus){
        POTIONS.register(eventBus);
    }
}
