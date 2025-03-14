package net.sinedkadis.terracompositio.util;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum FunctionSide implements StringRepresentable {
    PLUS("plus"),MINUS("minus"),NONE("none");

    private final String name;

    FunctionSide(String name) {
        this.name = name;
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
