package net.sinedkadis.terracompositio.api.helpers;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

/**
 * The class with methods, that helps with {@link Component}.
 */
public class TooltipHelper {
    private static final String translationKeyHeader = "info.terracompositio.";
    private static final String dataHeader = "val.";
    private static final String headerEnding = "_header";

    /**
     * Translation key as string with argument and default units.
     *
     * @param translationKey the translation key
     * @param arg            the arg
     * @return the mutable component
     */
    public static MutableComponent defaultTextWithArg(String translationKey, Object arg) {
        return defaultTextWithArg(translationKey, arg, Units.UNITS);
    }

    /**
     * Translation key as string with argument and given units. Cuts long floats
     *
     * @param translationKey the translation key
     * @param arg            the arg
     * @param measurement    the measurement unit
     * @return the mutable component
     */
    public static MutableComponent defaultTextWithArg(String translationKey, Object arg, ICustomUnit measurement) {

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

    /**
     * Adds header to {@link List} of {@link Component}.
     *
     * @param header the header
     * @param list   the list
     */
    public static void addHeader(ICustomHeader header, List<Component> list) {
        list.add(Component.translatable(header.toString()));
    }

    /**
     * Translation key with argument and default units.
     *
     * @param key the key
     * @param arg the arg
     * @return the mutable component
     */
    public static MutableComponent keyWithArg(ICustomKey key, Object arg) {
        return defaultTextWithArg(key.toString(), arg);
    }

    /**
     * Translation key with argument and given units.
     *
     * @param key  the key
     * @param arg  the arg
     * @param unit the unit
     * @return the mutable component
     */
    public static MutableComponent keyWithArg(ICustomKey key, Object arg, ICustomUnit unit) {
        return defaultTextWithArg(key.toString(), arg, unit);
    }

    /**
     * Adds component with default units to list if component data exist in {@link CompoundTag}.
     *
     * @param key  the key
     * @param list the list
     * @param data the data
     * @return is added
     */
    public static boolean addIfExist(ICustomKey key, List<Component> list, CompoundTag data) {
        if (data.contains(key.toData())) {
            return list.add(keyWithArg(key, data.get(key.toData())));
        }
        return false;
    }

    /**
     * Adds component with given units to list if component data exist in {@link CompoundTag}.
     *
     * @param key   the key
     * @param units the units
     * @param list  the list
     * @param data  the data
     * @return the boolean
     */
    public static boolean addIfExist(ICustomKey key, Units units, List<Component> list, CompoundTag data) {
        if (data.contains(key.toData())) {
            return list.add(keyWithArg(key, data.get(key.toData()), units));
        }
        return false;
    }

    /**
     * Adds component with default units and given index to list if component data exist in {@link CompoundTag}.
     *
     * @param key   the key
     * @param list  the list
     * @param data  the data
     * @param index the index, added to key in data
     * @return the boolean
     */
    public static boolean addIfExist(ICustomKey key, List<Component> list, CompoundTag data, int index) {
        if (data.contains(key.toData(index))) {
            return list.add(keyWithArg(key, data.get(key.toData(index))));
        }
        return false;
    }

    /**
     * Adds component with given units and given index to list if component data exist in {@link CompoundTag}.
     *
     * @param key   the key
     * @param units the units
     * @param list  the list
     * @param data  the data
     * @param index the index, added to key in data
     * @return the boolean
     */
    public static boolean addIfExist(ICustomKey key, ICustomUnit units, List<Component> list, CompoundTag data, int index) {
        if (data.contains(key.toData(index))) {
            return list.add(keyWithArg(key, data.get(key.toData(index)), units));
        }
        return false;
    }

    /**
     * Adds translation key with args and default units, that lay in data, without checking it existence.
     *
     * @param key  the key
     * @param list the list
     * @param data the data
     * @return is added
     */
    public static boolean add(ICustomKey key, List<Component> list, CompoundTag data) {
        return list.add(keyWithArg(key, data.get(key.toData())));
    }

    /**
     * Adds translation key with args and given units, that lay in data, without checking it existence.
     *
     * @param key  the key
     * @param unit the units
     * @param list the list
     * @param data the data
     * @return is added
     */
    public static boolean add(ICustomKey key, ICustomUnit unit, List<Component> list, CompoundTag data) {
        return list.add(keyWithArg(key, data.get(key.toData()), unit));
    }

    /**
     * Add translation key without args and with given units, without checking it existence.
     *
     * @param key  the key
     * @param unit the unit
     * @param list the list
     * @return the boolean
     */
    public static boolean add(ICustomKey key, ICustomUnit unit, List<Component> list) {
        return list.add(keyWithArg(key, "", unit));
    }

    /**
     * The Headers. Auto generated using enum entry names and adding
     * {@link net.sinedkadis.terracompositio.api.helpers.TooltipHelper#translationKeyHeader} at the start of string, and
     * {@link TooltipHelper#headerEnding} to the end of string
     */
    public enum Headers implements ICustomHeader {
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

    /**
     * The Keys. Auto generated using enum entry names and adding
     * {@link net.sinedkadis.terracompositio.api.helpers.TooltipHelper#translationKeyHeader} at the start of string to get a translation key,
     * and
     * {@link TooltipHelper#dataHeader} at the start of string to get a data key. Translation keys contains "%s" to make passing args possible
     */
    public enum Keys implements ICustomKey {
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

    /**
     * The Units. Auto generated using enum entry names and adding
     * {@link net.sinedkadis.terracompositio.api.helpers.TooltipHelper#translationKeyHeader} at the start of string to get a translation key
     */
    public enum Units implements ICustomUnit {
        BLOCKS,
        UNITS,
        MILIBUCKETS,
        ECF_SECOND,
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

    /**
     * Implement that to uze custom headers.
     */
    public interface ICustomHeader {

    }

    /**
     * Implement that to uze custom keys.
     */
    public interface ICustomKey {
        /**
         * Adds {@link TooltipHelper#dataHeader} at the start of string.
         *
         * @return the string
         */
        String toData();

        /**
         * Adds {@link TooltipHelper#dataHeader} at the start of name and index to the end of string
         *
         * @param index the index
         * @return the string
         */
        String toData(int index);
    }

    /**
     * Implement that to use custom units.
     */
    public interface ICustomUnit {

    }
}
