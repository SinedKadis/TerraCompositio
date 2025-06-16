package net.sinedkadis.terracompositio.compat.patchouli;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.registries.TCItems;

import java.lang.reflect.Method;

public class PatchouliCompat {
    private static final String BOOK_CLASS = "vazkii.patchouli.common.book.Book";
    private static final boolean PATCHOULI_LOADED = ModList.get().isLoaded("patchouli");

    public static void reloadBookContents(Object book, Level level) {
        if (!PATCHOULI_LOADED) return;

        try {
            Class<?> bookClass = Class.forName(BOOK_CLASS);
            if (bookClass.isInstance(book)) {
                Method reloadMethod = bookClass.getDeclaredMethod("reloadContents", Level.class, boolean.class);
                reloadMethod.setAccessible(true);
                reloadMethod.invoke(book, level, true);
            }
        } catch (Exception e) {
            TerraCompositio.LOGGER.error("Failed to reload Patchouli book contents", e);
        }
    }

    public static boolean isMyBook(ItemStack book) {
        if (!PATCHOULI_LOADED) return false;

        return book.is(TCItems.CREATION_FLOW_JOURNAL.get());
    }
}
