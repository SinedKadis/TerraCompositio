package net.sinedkadis.terracompositio.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.common.util.ForgeSoundType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.TerraCompositio;

import java.util.Objects;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, TerraCompositio.MOD_ID);

    public static final RegistryObject<SoundEvent> FLOW_LEAK = registerSoundEvents("flow_leak");

    public static final ForgeSoundType FLOWING_FLOW_CEDAR_LIKE_BLOCK_SOUNDS = new ForgeSoundType(1f,1f,
            () -> SoundEvents.BEACON_DEACTIVATE, () -> SoundEvents.WOOD_STEP,() -> SoundEvents.WOOD_PLACE,
            () -> SoundEvents.WOOD_HIT,() -> SoundEvents.WOOD_FALL);

    private static RegistryObject<SoundEvent> registerSoundEvents(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(Objects.requireNonNull(ResourceLocation.tryBuild(TerraCompositio.MOD_ID, name))));
    }

    public static void register(IEventBus eventBus){
        SOUND_EVENTS.register(eventBus);
    }
}
