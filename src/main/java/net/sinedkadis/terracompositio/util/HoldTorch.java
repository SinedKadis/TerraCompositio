package net.sinedkadis.terracompositio.util;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.StringRepresentable;

@MethodsReturnNonnullByDefault
public enum HoldTorch implements StringRepresentable {
    REDSTONE, SOUL, NORMAL,NONE;

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }

    public boolean isRedstoneTorch() {
        return this.equals(REDSTONE);
    }
    public boolean isEmpty() {
        return this.equals(NONE);
    }

}
