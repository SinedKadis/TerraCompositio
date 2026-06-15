package net.sinedkadis.terracompositio.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class TCCommonConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> CFE_PER_BURST_TRANSFER_LIMIT;
    public static final ForgeConfigSpec.ConfigValue<Integer> TICKS_BETWEEN_BURSTS;
    public static final ForgeConfigSpec.ConfigValue<Integer> PLATFORM_ALIVE_PER_PLAYER;
    public static final ForgeConfigSpec.ConfigValue<Boolean> DEBUG;

    static {
        BUILDER.push("Balance");

        PLATFORM_ALIVE_PER_PLAYER = BUILDER.comment("How many cfe boards from cfe charges can be")
                .define("Max boards per player", 3);
        CFE_PER_BURST_TRANSFER_LIMIT = BUILDER.comment("How many cfe can be transferred between two entities per burst")
                .define("CFE/burst transfer limit", 20);
        TICKS_BETWEEN_BURSTS = BUILDER.comment("How many ticks should pass before cfe source shot a new burst")
                .define("Ticks for burst", 20);



        BUILDER.pop();

        DEBUG = BUILDER.comment("Enables extra info in knowledge tooltip and more")
                .define("Debug mode", false);

        SPEC = BUILDER.build();
    }
}
