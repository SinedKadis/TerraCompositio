package net.sinedkadis.terracompositio.api;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.StringRepresentable;

/**
 * Hi, if you are reading this, I'm really very grateful for that. I'm happy that my mod become popular enough
 */
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
