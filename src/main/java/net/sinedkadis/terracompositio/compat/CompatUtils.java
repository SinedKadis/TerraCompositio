package net.sinedkadis.terracompositio.compat;

import net.minecraftforge.fml.ModList;

import java.util.function.Supplier;

public class CompatUtils {
    public static Supplier<Boolean> CREATE_EXISTENCE = () -> ModList.get().isLoaded("create");
}
