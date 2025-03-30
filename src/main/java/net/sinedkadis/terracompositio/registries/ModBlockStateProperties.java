package net.sinedkadis.terracompositio.registries;

import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.sinedkadis.terracompositio.util.FunctionSide;

public class ModBlockStateProperties {
    public static final BooleanProperty INFUSED = BooleanProperty.create("infused");
    public static final BooleanProperty INPUT_BUS = BooleanProperty.create("input_bus");
    public static final BooleanProperty OUTPUT_BUS = BooleanProperty.create("output_bus");
    public static final EnumProperty<FunctionSide> FUNCTION_SIDE = EnumProperty.create("function_side",FunctionSide.class);
    public static final BooleanProperty INPUT_BUS_CONNECTION = BooleanProperty.create("input_bus_connection");
    public static final BooleanProperty OUTPUT_BUS_CONNECTION = BooleanProperty.create("output_bus_connection");
    public static final BooleanProperty UP_CONNECTION = BooleanProperty.create("up_connection");
    public static final BooleanProperty DOWN_CONNECTION = BooleanProperty.create("down_connection");
    public static final BooleanProperty RIGHT_CONNECTION = BooleanProperty.create("right_connection");
    public static final BooleanProperty LEFT_CONNECTION = BooleanProperty.create("left_connection");
}
