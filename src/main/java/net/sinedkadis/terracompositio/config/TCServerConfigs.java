package net.sinedkadis.terracompositio.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class TCServerConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> IF_DURATION;
    public static final ForgeConfigSpec.ConfigValue<Double> IF_RANDOM_TICK_PER_TICK;

    public static final ForgeConfigSpec.ConfigValue<Integer> CFE_SEND_FREQUENCY;

    static {
        BUILDER.push("Infused Fertiliser");

        IF_DURATION = BUILDER.comment("How many ticks will infused fertiliser work")
                .define("Infused Fertilizer Duration", 100);
        IF_RANDOM_TICK_PER_TICK = BUILDER.comment("How many random ticks per tick will infused fertiliser do")
                .define("Infused Fertilizer random ticks per Tick", 0.5d);

        BUILDER.pop();
        BUILDER.push("CFE");

        CFE_SEND_FREQUENCY = BUILDER.comment("How often cfe will transfer between blocks")
                .define("Send Cooldown", 40);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
