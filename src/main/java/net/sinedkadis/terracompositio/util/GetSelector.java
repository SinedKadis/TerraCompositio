package net.sinedkadis.terracompositio.util;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Predicate;

public interface GetSelector {
    List<Pair<Predicate<BlockState>, BakedModel>> getselectors();
}
