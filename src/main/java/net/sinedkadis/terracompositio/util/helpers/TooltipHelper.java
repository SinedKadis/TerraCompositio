package net.sinedkadis.terracompositio.util.helpers;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TooltipHelper {
    public static MutableComponent defaultTextWithArg(String translationKey, Object arg) {
        return defaultTextWithArg(translationKey, arg, Units.UNITS);
    }

    public static MutableComponent defaultTextWithArg(String translationKey, Object arg, Units measurement) {
        return Component.translatable(translationKey,
                        Component.literal(String.valueOf(arg))
                                .append(Component.translatable(measurement.toString()))
                                .withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.GRAY);
    }

    public enum Units {
        BLOCKS("block.terracompositio.blocks"),
        UNITS("block.terracompositio.units"),
        MILIBUCKETS("block.terracompositio.milibuckets");

        private final String translationKey;

        Units(String translationKey) {
            this.translationKey = translationKey;
        }

        @Override
        public String toString() {
            return translationKey;
        }
    }
}
