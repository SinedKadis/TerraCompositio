package net.sinedkadis.terracompositio.util;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.registries.TCBlockStateProperties;
import org.jetbrains.annotations.NotNull;

public enum FunctionSide implements StringRepresentable {
    PLUS("plus"),MINUS("minus"),NONE("none");

    private final String name;

    FunctionSide(String name) {
        this.name = name;
    }

    public static Direction getDirectionByFunctionSide(BlockState blockState){
        if (blockState.hasProperty(RotatedPillarBlock.AXIS) && blockState.hasProperty(TCBlockStateProperties.FUNCTION_SIDE)){
            FunctionSide functionSide = blockState.getValue(TCBlockStateProperties.FUNCTION_SIDE);
            if (functionSide.equals(NONE))
                return Direction.DOWN;
            return switch (blockState.getValue(RotatedPillarBlock.AXIS)){
                case Y -> Direction.DOWN;
                case Z -> {
                    if (functionSide.equals(PLUS))
                        yield Direction.EAST;
                    yield Direction.WEST;
                }
                case X -> {
                    if (functionSide.equals(PLUS))
                        yield Direction.SOUTH;
                    yield Direction.NORTH;
                }
            };
        }
        return Direction.DOWN;
    }

    public static FunctionSide getFunctionSideByDirection(BlockState blockState, Direction direction){
        if (blockState.hasProperty(RotatedPillarBlock.AXIS) && blockState.hasProperty(TCBlockStateProperties.FUNCTION_SIDE)){
            return switch (direction){
                case WEST -> {
                    if (blockState.getValue(RotatedPillarBlock.AXIS).equals(Direction.Axis.Z))
                        yield MINUS;
                    yield NONE;
                }
                case EAST -> {
                    if (blockState.getValue(RotatedPillarBlock.AXIS).equals(Direction.Axis.Z))
                        yield PLUS;
                    yield NONE;
                }
                case NORTH -> {
                    if (blockState.getValue(RotatedPillarBlock.AXIS).equals(Direction.Axis.X))
                        yield MINUS;
                    yield NONE;
                }
                case SOUTH -> {
                    if (blockState.getValue(RotatedPillarBlock.AXIS).equals(Direction.Axis.X))
                        yield PLUS;
                    yield NONE;
                }
                default -> NONE;
            };
        }
        return NONE;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    @Override
    public String toString() {
        return "FunctionSide{" +
                "name='" + name + '\'' +
                '}';
    }
}
