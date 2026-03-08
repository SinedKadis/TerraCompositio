package net.sinedkadis.terracompositio.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class TCClientConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Double> CFE_RENDER_MULTIPLIER;

    static {
        BUILDER.push("Configs for Terracompositio Mod");

        CFE_RENDER_MULTIPLIER = BUILDER.comment("How many cfe particles per CFE will be rendered")
                .define("CFE Particle multiplier", 0.1d);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
