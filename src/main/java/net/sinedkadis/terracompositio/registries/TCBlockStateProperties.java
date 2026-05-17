package net.sinedkadis.terracompositio.registries;

import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.sinedkadis.terracompositio.util.HoldTorch;

public class TCBlockStateProperties {
    public static final BooleanProperty INFUSED = BooleanProperty.create("infused");
    public static final BooleanProperty FUNCTION_SIDE = BooleanProperty.create("function_side");
    public static final BooleanProperty UP_CONNECTION = BooleanProperty.create("up_connection");
    public static final BooleanProperty DOWN_CONNECTION = BooleanProperty.create("down_connection");
    public static final BooleanProperty RIGHT_CONNECTION = BooleanProperty.create("right_connection");
    public static final BooleanProperty LEFT_CONNECTION = BooleanProperty.create("left_connection");
    public static final BooleanProperty WAXED = BooleanProperty.create("waxed");
    public static final EnumProperty<HoldTorch> HOLD_TORCH = EnumProperty.create("hold_torch", HoldTorch.class);
}
