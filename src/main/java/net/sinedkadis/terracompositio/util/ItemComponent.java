package net.sinedkadis.terracompositio.util;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public record ItemComponent(ItemStack itemStack) implements Component {

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
