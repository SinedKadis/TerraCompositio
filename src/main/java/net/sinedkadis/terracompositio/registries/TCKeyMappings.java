package net.sinedkadis.terracompositio.registries;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.Getter;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public enum TCKeyMappings {
    BOOT_HEIGHT(new KeyMapping("item.terracompositio.technetium_boots.height",
            new BootKeyConflictContext(),
            KeyModifier.ALT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_CONTROL,
            "key.terracompositio.categories.main")),

    BOOT_RAISE(new KeyMapping("item.terracompositio.technetium_boots.raise",
            new BootKeyConflictContext(),
            KeyModifier.ALT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_SPACE,
            "key.terracompositio.categories.main")),
    BOOT_REDUCE(new KeyMapping("item.terracompositio.technetium_boots.reduce",
            new BootKeyConflictContext(),
            KeyModifier.ALT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_SHIFT,
            "key.terracompositio.categories.main"));
    @Getter
    private final KeyMapping keyMapping;

    TCKeyMappings(KeyMapping keyMapping) {
        this.keyMapping = keyMapping;
    }

    public static void register(RegisterKeyMappingsEvent event) {
        for (TCKeyMappings mappings : TCKeyMappings.values()) {
            event.register(mappings.keyMapping);
        }
    }

    private static class BootKeyConflictContext implements IKeyConflictContext {
        @Override
        public boolean isActive() {
            LocalPlayer player = Minecraft.getInstance().player;
            return player != null && player.getItemBySlot(EquipmentSlot.FEET).is(TCItems.TECHNETIUM_BOOTS.get());
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return other.equals(this);
        }
    }
}
