package net.sinedkadis.terracompositio.api.components;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * The implementation of Component that holds {@link ItemStack}. Processed in Knowledge Overlay.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public record ItemComponent(ItemStack itemStack) implements Component {

    /**
     * Factory method for {@link ItemComponent}
     *
     * @param itemStack the item stack
     * @return the item component
     */
    public static ItemComponent of(ItemStack itemStack) {
        return new ItemComponent(itemStack);
    }

    @Override
    public Style getStyle() {
        return Style.EMPTY;
    }

    @Override
    public ComponentContents getContents() {
        return ComponentContents.EMPTY;
    }

    @Override
    public List<Component> getSiblings() {
        return List.of();
    }

    @Override
    public FormattedCharSequence getVisualOrderText() {
        return FormattedCharSequence.EMPTY;
    }

}
