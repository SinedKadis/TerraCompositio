package net.sinedkadis.terracompositio.api.components;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * The implementation of Component that holds {@link FluidStack}. Processed in Knowledge Overlay.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public record FluidComponent(FluidStack fluidStack) implements Component {

    /**
     * Factory method for {@link FluidComponent}
     *
     * @param fluidStack the fluid stack
     * @return the fluid component
     */
    public static FluidComponent of(FluidStack fluidStack) {
        return new FluidComponent(fluidStack);
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
