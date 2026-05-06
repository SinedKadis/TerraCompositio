package net.sinedkadis.terracompositio.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class TCCommonConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> CFE_PER_BURST_TRANSFER_LIMIT;
    public static final ForgeConfigSpec.ConfigValue<Integer> TICKS_BETWEEN_BURSTS;
//    public static final ForgeConfigSpec.ConfigValue<Integer> CITRINE_ORE_VEIN_SIZE;

    static {
        BUILDER.push("Configs for Terracompositio Mod");

        CFE_PER_BURST_TRANSFER_LIMIT = BUILDER.comment("How many cfe can be transferred between two entities per burst")
                .define("CFE/burst transfer limit", 20);
        TICKS_BETWEEN_BURSTS = BUILDER.comment("How many ticks should pass before cfe source shot a new burst")
                .define("Ticks for burst", 20);
//        CITRINE_ORE_VEIN_SIZE = BUILDER.comment("How many Citrine Ore Blocks spawn in one Vein!")
//                .defineInRange("Vein Size", 9, 4, 20);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
