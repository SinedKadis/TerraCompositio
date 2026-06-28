package net.sinedkadis.terracompositio.api.components;

import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.sinedkadis.terracompositio.api.helpers.TooltipHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The {@link Component} implementation that holds {@link net.sinedkadis.terracompositio.api.helpers.TooltipHelper.ICustomHeader} and contents under
 * header for further processing in KnowledgeOverlay.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HeaderComponent extends EmptyComponent {

    /**
     * The Header
     */
    @Getter
    TooltipHelper.ICustomHeader header;
    /**
     * The Consumer list.
     */
    @Getter
    List<Consumer<List<Component>>> consumerList = new ArrayList<>();

    /**
     * Instantiates a new Header component.
     *
     * @param header the header
     */
    public HeaderComponent(TooltipHelper.ICustomHeader header) {
        this.header = header;
    }

    /**
     * Create header component.
     *
     * @param header the header
     * @return the header component
     */
    public static HeaderComponent create(TooltipHelper.ICustomHeader header) {
        return new HeaderComponent(header);
    }
}
