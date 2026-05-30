package net.sinedkadis.terracompositio.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class TCClientConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Double> CFE_RENDER_MULTIPLIER;

    public static final ForgeConfigSpec.ConfigValue<Integer> OVERLAY_X_OFFSET;
    public static final ForgeConfigSpec.ConfigValue<Integer> OVERLAY_Y_OFFSET;

    static {
        BUILDER.push("Configs for Terracompositio Mod");

        CFE_RENDER_MULTIPLIER = BUILDER.comment("How many cfe particles per CFE will be rendered")
                .define("CFE Particle multiplier", 0.1d);

        BUILDER.pop();

        BUILDER.push("Knowledge Overlay");

        OVERLAY_X_OFFSET = BUILDER.comment("Left-Right shift")
                .define("X Offset", -60);
        OVERLAY_Y_OFFSET = BUILDER.comment("Up-Down shift")
                .define("Y Offset", 0);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
