package net.sinedkadis.terracompositio.api.helpers;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class TooltipHelper {
    private static final String translationKeyHeader = "info.terracompositio.";
    private static final String dataHeader = "val.";
    private static final String headerEnding = "_header";

    public static MutableComponent defaultTextWithArg(String translationKey, Object arg) {
        return defaultTextWithArg(translationKey, arg, Units.UNITS);
    }

    public static MutableComponent defaultTextWithArg(String translationKey, Object arg, Units measurement) {

        String stringArg = String.valueOf(arg);
        int toCrop = 0;
        if (stringArg.endsWith("f")) toCrop++;

        int length = stringArg.length();

        if (length > 6) toCrop = length - 6;
        stringArg = stringArg.substring(0, length - toCrop);

        MutableComponent mutableComponent = Component.literal(stringArg)
                .append(Component.translatable(measurement.toString()));
        if (arg instanceof MutableComponent component)
            mutableComponent = component;
        return Component.translatable(translationKey,
                        mutableComponent.withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.GRAY);
    }

    public static void addHeader(Headers header, List<Component> list) {
        list.add(Component.translatable(header.toString()));
    }

    public static MutableComponent keyWithArg(Keys key, Object arg) {
        return defaultTextWithArg(key.toString(), arg);
    }

    public static MutableComponent keyWithArg(Keys key, Object arg, Units unit) {
        return defaultTextWithArg(key.toString(), arg, unit);
    }

    public static boolean addIfExist(Keys key, List<Component> list, CompoundTag data) {
        if (data.contains(key.toData())) {
            return list.add(keyWithArg(key, data.get(key.toData())));
        }
        return false;
    }

    public static boolean addIfExist(Keys key, Units units, List<Component> list, CompoundTag data) {
        if (data.contains(key.toData())) {
            return list.add(keyWithArg(key, data.get(key.toData()), units));
        }
        return false;
    }

    public static boolean addIfExist(Keys key, List<Component> list, CompoundTag data, int index) {
        if (data.contains(key.toData(index))) {
            return list.add(keyWithArg(key, data.get(key.toData(index))));
        }
        return false;
    }

    public static boolean addIfExist(Keys key, Units units, List<Component> list, CompoundTag data, int index) {
        if (data.contains(key.toData(index))) {
            return list.add(keyWithArg(key, data.get(key.toData(index)), units));
        }
        return false;
    }

    public static boolean add(Keys key, List<Component> list, CompoundTag data) {
        return list.add(keyWithArg(key, data.get(key.toData())));
    }

    public static boolean add(Keys key, Units unit, List<Component> list, CompoundTag data) {
        return list.add(keyWithArg(key, data.get(key.toData()), unit));
    }

    public static boolean add(Keys key, Units unit, List<Component> list) {
        return list.add(keyWithArg(key, "", unit));
    }

    public enum Headers {
        BLOCK,
        ENTITY,
        ITEMS,
        FLUIDS,
        CRAFTING,
        ECF,
        ENT_HOLD,
        ENT_INNER,
        ENT_COMMON;

        @Override
        public String toString() {
            return translationKeyHeader + name().toLowerCase() + headerEnding;
        }
    }

    public enum Keys {
        CONSUME,
        GENERATE,
        STORAGE_EXTENSION,
        TIME_REMAINING,
        ECF,
        ECF_TICK,
        MAX_ECF,
        PRIORITY,
        RANGE,
        QUEUED,
        EXTRACTED_ECF,
        TYPE,
        FLUID,
        PROGRESS,
        MAX_PROGRESS;

        @Override
        public String toString() {
            return translationKeyHeader + name().toLowerCase();
        }

        public String toData() {
            return dataHeader + name().toLowerCase();
        }

        public String toData(int index) {
            return dataHeader + name().toLowerCase() + index;
        }
    }

    public enum Units {
        BLOCKS,
        UNITS,
        MILIBUCKETS,
        CFE_SECOND,
        SECONDS,
        CONSUMER,
        NO_UNITS,
        SOURCE;

        @Override
        public String toString() {
            if (this.equals(NO_UNITS)) return "";
            return translationKeyHeader + name().toLowerCase();
        }
    }
}
