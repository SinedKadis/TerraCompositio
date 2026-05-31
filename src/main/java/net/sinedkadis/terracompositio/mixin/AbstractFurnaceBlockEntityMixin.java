package net.sinedkadis.terracompositio.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.recipe.TechnetiumFiringRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

import static net.sinedkadis.terracompositio.util.helpers.CFEHelper.placeCFECloud;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {
    @Inject(
            method = "serverTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;getTotalCookTime(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;)I",
                    shift = At.Shift.AFTER
            )
    )
    private static void beforeSetRecipeUsed(
            Level pLevel,
            BlockPos pPos,
            BlockState pState,
            AbstractFurnaceBlockEntity pBlockEntity,
            CallbackInfo ci,
            @Local Recipe<?> recipe
    ) {
        if (recipe != null) {
            Optional<TechnetiumFiringRecipe> firingRecipe = pLevel.getRecipeManager().getRecipeFor(TechnetiumFiringRecipe.Type.INSTANCE,
                    new SimpleContainer(pBlockEntity.getItem(0)),pLevel);
            if (firingRecipe.isPresent()) {
                int cfe = firingRecipe.get().getCfe();
                placeCFECloud(pLevel, pPos, cfe);
            }
        }
    }
}
