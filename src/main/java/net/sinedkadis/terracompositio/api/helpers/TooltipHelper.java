package net.sinedkadis.terracompositio.api.helpers;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.components.HeaderComponent;

import java.util.List;
import java.util.function.Consumer;

/**
 * The class with methods, that helps with {@link Component}.
 */
public class TooltipHelper {
    private static final String translationKeyHeader = "info.";
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

        MutableComponent mutableComponent;


        if (arg instanceof MutableComponent component)
            mutableComponent = component;
        else {
            String stringArg = String.valueOf(arg);
            int toCrop = 0;
            if (stringArg.endsWith("f")) toCrop++;

            int length = stringArg.length();

            if (length > 6) toCrop = length - 6;
            stringArg = stringArg.substring(0, length - toCrop);


            mutableComponent = Component.literal(stringArg)
                    .append(Component.translatable(measurement.toTranslation()));
        }

        ChatFormatting color;
        if (translationKey.equals(Keys.CRAFT_EXCEPTION.toTranslation()))
            color = ChatFormatting.RED;
        else
            color = ChatFormatting.AQUA;
        return Component.translatable(translationKey,
                        mutableComponent.withStyle(color))
                .withStyle(ChatFormatting.GRAY);
    }

    /**
     * Adds header to {@link List} of {@link Component}.
     *
     * @param header              the header
     * @param list                the list
     * @param tooltipsUnderHeader the lines that will be added under the header
     */
    public static void addWithHeader(ICustomHeader header, List<Component> list, Consumer<List<Component>> tooltipsUnderHeader) {
        for (Component component : list) {
            if (component instanceof HeaderComponent headerComponent && headerComponent.getHeader().equals(header)) {
                headerComponent.getConsumerList().add(tooltipsUnderHeader);
                return;
            }
        }
        HeaderComponent headerComponent = HeaderComponent.create(header);
        headerComponent.getConsumerList().add(tooltipsUnderHeader);
        list.add(headerComponent);
    }

    /**
     * Translation key with argument and default units.
     *
     * @param key the key
     * @param arg the arg
     * @return the mutable component
     */
    public static MutableComponent keyWithArg(ICustomKey key, Object arg) {
        return defaultTextWithArg(key.toTranslation(), arg);
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
        return defaultTextWithArg(key.toTranslation(), arg, unit);
    }

    /**
     * Adds component with default units to list if component data exist in {@link CompoundTag}.
     *
     * @param key  the key
     * @param list the list
     * @param data the data
     */
    public static void addIfExist(ICustomKey key, List<Component> list, CompoundTag data) {
        if (data.contains(key.toData())) {
            list.add(keyWithArg(key, data.get(key.toData())));
        }
    }

    /**
     * Adds component with given units to list if component data exist in {@link CompoundTag}.
     *
     * @param key   the key
     * @param units the units
     * @param list  the list
     * @param data  the data
     */
    public static void addIfExist(ICustomKey key, Units units, List<Component> list, CompoundTag data) {
        if (data.contains(key.toData())) {
            list.add(keyWithArg(key, data.get(key.toData()), units));
        }
    }

    /**
     * Adds component with default units and given index to list if component data exist in {@link CompoundTag}.
     *
     * @param key   the key
     * @param list  the list
     * @param data  the data
     * @param index the index, added to key in data
     */
    public static void addIfExist(ICustomKey key, List<Component> list, CompoundTag data, int index) {
        if (data.contains(key.toData(index))) {
            list.add(keyWithArg(key, data.get(key.toData(index))));
        }
    }

    /**
     * Adds component with given units and given index to list if component data exist in {@link CompoundTag}.
     *
     * @param key   the key
     * @param units the units
     * @param list  the list
     * @param data  the data
     * @param index the index, added to key in data
     */
    public static void addIfExist(ICustomKey key, ICustomUnit units, List<Component> list, CompoundTag data, int index) {
        if (data.contains(key.toData(index))) {
            list.add(keyWithArg(key, data.get(key.toData(index)), units));
        }
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
     * Add translation key without args and with given units, without checking it existence. Data provided to check Headers.
     * If any header is active, adds values right after it header
     *
     * @param key  the key
     * @param unit the unit
     * @param list the list
     * @return the boolean
     */
    public static boolean addWithNoArg(ICustomKey key, ICustomUnit unit, List<Component> list) {
        return list.add(keyWithArg(key, "", unit));
    }

    /**
     * The Headers. Auto generated using enum entry names
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
        public String getModID() {
            return TerraCompositioAPI.MOD_ID;
        }
    }

    /**
     * The Keys. Auto generated using enum entry names
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
        MAX_PROGRESS,
        TIME_COLLECTED,
        TIME_COLLECTION_CHANCE,
        CRAFT_EXCEPTION;

        @Override
        public String getModID() {
            return TerraCompositioAPI.MOD_ID;
        }
    }

    /**
     * The Units. Auto generated using enum entry names and adding
     * {@link net.sinedkadis.terracompositio.api.helpers.TooltipHelper#translationKeyHeader} at the start of string to get a translation key
     */
    public enum Units implements ICustomUnit {
        NO_UNITS,
        ERROR,
        BLOCKS,
        UNITS,
        MILIBUCKETS,
        ECF_SECOND,
        SECONDS,
        CONSUMER,
        SOURCE;

        @Override
        public String toTranslation() {
            if (this.equals(NO_UNITS)) return "";
            return ICustomUnit.super.toTranslation();
        }

        @Override
        public String getModID() {
            return TerraCompositioAPI.MOD_ID;
        }
    }

    /**
     * Implement that to uze custom headers. Generated via adding
     *  {@link net.sinedkadis.terracompositio.api.helpers.TooltipHelper#translationKeyHeader} at the start of string,
     *  then provided MOD_ID, then provided name and with
     *  {@link TooltipHelper#headerEnding} at the end of the string
     */
    public interface ICustomHeader {

        /**
         * Name of the header without "_header".
         *
         * @return the string
         */
        String name();

        /**
         * To translation string. Looks like "info.mod_id.name_header"
         *
         * @return the string
         */
        default String toTranslation() {
            return translationKeyHeader + getModID() + "." + name().toLowerCase() + headerEnding;
        }

        /**
         * Gets the MOD_ID used in translation key.
         *
         * @return the MOD_ID
         */
        String getModID();
    }

    /**
     * Implement that to uze custom keys. Generated via adding
     *   {@link net.sinedkadis.terracompositio.api.helpers.TooltipHelper#translationKeyHeader} at the start of string,
     *   then provided MOD_ID, then provided name. For usage in compoundTag data adds {@link TooltipHelper#dataHeader} at the start.
     *   Translation keys must contain "%s" to make passing args possible
     */
    public interface ICustomKey {

        /**
         * Name of the key.
         *
         * @return the string
         */
        String name();

        /**
         * To translation string. Looks like "info.mod_id.name"
         *
         * @return the string
         */
        default String toTranslation() {
            return translationKeyHeader + getModID() + "." + name().toLowerCase();
        }

        /**
         * Adds {@link TooltipHelper#dataHeader} at the start of string.
         *
         * @return the string
         */
        default String toData() {
            return dataHeader + name().toLowerCase();
        }

        /**
         * Adds {@link TooltipHelper#dataHeader} at the start of name and index to the end of string
         *
         * @param index the index
         * @return the string
         */
        default String toData(int index) {
            return dataHeader + name().toLowerCase() + index;
        }

        /**
         * Gets the MOD_ID used in translation key.
         *
         * @return the MOD_ID
         */
        String getModID();
    }

    /**
     * Implement that to use custom units. Generated via adding
     *   {@link net.sinedkadis.terracompositio.api.helpers.TooltipHelper#translationKeyHeader} at the start of string,
     *   then provided MOD_ID, then provided name
     */
    public interface ICustomUnit {
        /**
         * Name of the unit.
         *
         * @return the string
         */
        String name();

        /**
         * To translation string. Looks like "info.mod_id.name"
         *
         * @return the string
         */
        default String toTranslation() {
            return translationKeyHeader + getModID() + "." + name().toLowerCase();
        }

        /**
         * Gets the MOD_ID used in translation key.
         *
         * @return the MOD_ID
         */
        String getModID();
    }
}
