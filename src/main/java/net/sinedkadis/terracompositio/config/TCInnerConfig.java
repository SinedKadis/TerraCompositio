package net.sinedkadis.terracompositio.config;

import java.util.function.ToIntFunction;

public class TCInnerConfig {
    //private static final float MIN_CAMERA_DISTANCE_SQUARED = 3.25F;
    public static final ToIntFunction<Integer> RENDER_COUNT_FUNCTION = cfe ->
            (int) Math.ceil(cfe * TCClientConfigs.CFE_RENDER_MULTIPLIER.get());

    public static final int DEFAULT_SOURCE_PRIORITY = -100;
    public static final int DEFAULT_CONSUMER_PRIORITY = 100;
}
