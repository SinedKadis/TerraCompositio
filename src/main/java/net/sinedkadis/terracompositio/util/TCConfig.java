package net.sinedkadis.terracompositio.util;

import java.util.function.ToIntFunction;

public class TCConfig {
    //private static final float MIN_CAMERA_DISTANCE_SQUARED = 3.25F;
    public static final ToIntFunction<Integer> RENDER_COUNT_FUNCTION = cfe -> (int) Math.ceil(cfe * 0.1f);
    public static final int CFE_BY_TICK_TRANSFER_LIMIT = 10;
}
