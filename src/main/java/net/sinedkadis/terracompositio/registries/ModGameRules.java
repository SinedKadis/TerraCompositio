package net.sinedkadis.terracompositio.registries;


import net.minecraft.world.level.GameRules;
import net.sinedkadis.terracompositio.util.GameruleUtilities;


public class ModGameRules {
    public static GameRules.Key<GameRules.BooleanValue> DISABLE_FLOW_LEAKING;
    public static void init() {
        DISABLE_FLOW_LEAKING = GameruleUtilities.register("disableFlowLeaking", GameRules.Category.MISC,false);
    }
}
