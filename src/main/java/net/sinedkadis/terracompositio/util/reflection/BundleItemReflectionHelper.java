package net.sinedkadis.terracompositio.util.reflection;

import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nonnull;
import java.lang.reflect.Method;

public final class BundleItemReflectionHelper {
    private static final Method ADD_METHOD;

    static {
        try {

            ADD_METHOD = BundleItem.class.getDeclaredMethod(
                    "add",
                    ItemStack.class,
                    ItemStack.class
            );
            ADD_METHOD.setAccessible(true); // Делаем метод доступным
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to access BundleItem.add() method!", e);
        }
    }


    public static int addToBundle(ItemStack bundle, @Nonnull ItemStack stackToAdd) {
        try {
            return (int) ADD_METHOD.invoke(null, bundle, stackToAdd);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke BundleItem.add()!", e);
        }
    }
}