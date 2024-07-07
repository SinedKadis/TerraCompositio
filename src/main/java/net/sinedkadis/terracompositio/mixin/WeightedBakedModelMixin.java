package net.sinedkadis.terracompositio.mixin;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.sinedkadis.terracompositio.util.GetWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WeightedBakedModel.class)
public class WeightedBakedModelMixin implements GetWrapper {

    @Shadow @Final private BakedModel wrapped;

    @Override
    public BakedModel getwrapped() {
        return this.wrapped;
    }
}
