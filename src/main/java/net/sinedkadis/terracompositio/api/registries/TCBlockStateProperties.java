package net.sinedkadis.terracompositio.api.registries;

import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.sinedkadis.terracompositio.api.HoldTorch;

/**
 * BlockState Properties, that used in my mod
 */
public class TCBlockStateProperties {
    public static final BooleanProperty INFUSED = BooleanProperty.create("infused");
    public static final BooleanProperty WAXED = BooleanProperty.create("waxed");
    public static final EnumProperty<HoldTorch> HOLD_TORCH = EnumProperty.create("hold_torch", HoldTorch.class);
    public static final BooleanProperty PERMANENT = BooleanProperty.create("permanent");
}
