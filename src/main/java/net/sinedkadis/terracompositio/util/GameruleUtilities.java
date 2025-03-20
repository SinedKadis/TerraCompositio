package net.sinedkadis.terracompositio.util;

import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public final class GameruleUtilities {
    /**
     * A map containing the gamerule keys and their IDs.
     */
    private static final Map<String, GameRules.Key<?>> ruleIDMap = new HashMap<>();

    /**
     * Register a new gamerule.
     *
     * @param name         The name of the gamerule to register. Must be unique.
     * @param category     The category to put the gamerule under.
     * @param defaultValue The value to set the gamerule to by default.
     *
     * @return A key that can be used to access the gamerule.
     */
    private static <T extends GameRules.Value<T>> GameRules.Key<T> register(String name, GameRules.Category category, GameRules.Type<T> defaultValue) {
        var key = GameRules.register(name, category, defaultValue);
        ruleIDMap.put(name, key);
        return key;
    }

    /**
     * Register a new gamerule.
     *
     * @param name         The name of the gamerule to register. Must be unique.
     * @param category     The category to put the gamerule under.
     * @param defaultValue The value to set the gamerule to by default.
     *
     * @return A key that can be used to access the gamerule.
     */
    public static GameRules.Key<GameRules.BooleanValue> register(String name, GameRules.Category category, boolean defaultValue) {
        return register(name, category, GameRules.BooleanValue.create(defaultValue));
    }

    /**
     * Register a new gamerule.
     *
     * @param name         The name of the gamerule to register. Must be unique.
     * @param category     The category to put the gamerule under.
     * @param defaultValue The value to set the gamerule to by default.
     *
     * @return A key that can be used to access the gamerule.
     */
    public static GameRules.Key<GameRules.IntegerValue> register(String name, GameRules.Category category, int defaultValue) {
        return register(name, category, GameRules.IntegerValue.create(defaultValue));
    }

    /**
     * Get the value associated with a gamerule.
     *
     * @param <T>   The type of the value held in the gamerule.
     * @param level The level to retrieve the gamerule value from.
     * @param key   The key to retrieve the gamerule for.
     *
     * @return The value wrapper object for the gamerule.
     */
    public static <T extends GameRules.Value<T>> T getGamerule(Level level, GameRules.Key<T> key) {
        return level.getGameRules().getRule(key);
    }

    /**
     * Get the value associated with a gamerule.
     *
     * @param <T>   The type of the value held in the gamerule.
     * @param level The level to retrieve the gamerule value from.
     * @param id    The id to retrieve the gamerule for.
     *
     * @return The value wrapper object for the gamerule.
     */
    public static <T extends GameRules.Value<T>> T getGamerule(Level level, String id) {
        @SuppressWarnings("unchecked")
        var key = (GameRules.Key<T>) ruleIDMap.get(id);
        return level.getGameRules().getRule(key);
    }

    /**
     * Get the boolean value associated with a gamerule.
     *
     * @param level The level to retrieve the gamerule value from.
     * @param key   The key to retrieve the gamerule for.
     *
     * @return The boolean value for the gamerule.
     */
    public static boolean getBooleanGamerule(Level level, GameRules.Key<GameRules.BooleanValue> key) {
        return getGamerule(level, key).get();
    }

    /**
     * Get the boolean value associated with a gamerule.
     *
     * @param level The level to retrieve the gamerule value from.
     * @param id    The id to retrieve the gamerule for.
     *
     * @return The boolean value for the gamerule.
     */
    public static boolean getBooleanGamerule(Level level, String id) {
        return GameruleUtilities.<GameRules.BooleanValue>getGamerule(level, id).get();
    }

    /**
     * Get the integer value associated with a gamerule.
     *
     * @param level The level to retrieve the gamerule value from.
     * @param key   The key to retrieve the gamerule for.
     *
     * @return The integer value for the gamerule.
     */
    public static int getIntegerGamerule(Level level, GameRules.Key<GameRules.IntegerValue> key) {
        return getGamerule(level, key).get();
    }

    /**
     * Get the integer value associated with a gamerule.
     *
     * @param level The level to retrieve the gamerule value from.
     * @param id    The id to retrieve the gamerule for.
     *
     * @return The integer value for the gamerule.
     */
    public static int getIntegerGamerule(Level level, String id) {
        return GameruleUtilities.<GameRules.IntegerValue>getGamerule(level, id).get();
    }
}
