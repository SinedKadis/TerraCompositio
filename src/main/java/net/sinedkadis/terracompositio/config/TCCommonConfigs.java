package net.sinedkadis.terracompositio.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class TCCommonConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> ECF_PER_BURST_TRANSFER_LIMIT;
    public static final ForgeConfigSpec.ConfigValue<Integer> TICKS_BETWEEN_BURSTS;
    public static final ForgeConfigSpec.ConfigValue<Integer> PLATFORM_ALIVE_PER_PLAYER;
    public static final ForgeConfigSpec.ConfigValue<Boolean> DEBUG;

    static {
        BUILDER.push("Balance");

        PLATFORM_ALIVE_PER_PLAYER = BUILDER.comment("How many ecf boards from ecf charges can be")
                .define("Max boards per player", 3);
        ECF_PER_BURST_TRANSFER_LIMIT = BUILDER.comment("How many ecf can be transferred between two entities per burst")
                .define("CFE/burst transfer limit", 20);
        TICKS_BETWEEN_BURSTS = BUILDER.comment("How many ticks should pass before ecf source shot a new burst")
                .define("Ticks for burst", 20);



        BUILDER.pop();

        DEBUG = BUILDER.comment("Enables extra info in knowledge tooltip and more")
                .define("Debug mode", false);

        SPEC = BUILDER.build();
    }
}
