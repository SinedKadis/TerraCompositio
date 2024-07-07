package net.sinedkadis.terracompositio.mixin;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.util.GetSelector;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Predicate;

@Mixin(MultiPartBakedModel.class)
public class MultipartBakedModelMixin implements GetSelector {
    @Shadow @Final private List<Pair<Predicate<BlockState>, BakedModel>> selectors;

    @Override
    public List<Pair<Predicate<BlockState>, BakedModel>> getselectors() {
        return this.selectors;
    }
}
