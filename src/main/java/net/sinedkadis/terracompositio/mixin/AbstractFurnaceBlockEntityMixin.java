package net.sinedkadis.terracompositio.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.sinedkadis.terracompositio.recipe.TechnetiumFiringRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

import static net.sinedkadis.terracompositio.block.custom.CFESaturatedAirBlock.placeCFECloud;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {
    @Inject(
            method = "serverTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;getTotalCookTime(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;)I",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void beforeSetRecipeUsed(
            Level pLevel,
            BlockPos pPos,
            BlockState pState,
            AbstractFurnaceBlockEntity pBlockEntity,
            CallbackInfo ci,
            boolean flag,
            boolean flag1,
            ItemStack itemstack,
            boolean flag2,
            boolean flag3,
            Recipe<?> recipe,
            int i
    ) {
        if (recipe != null) {
            for (Direction direction : Direction.values()){
                BlockPos airPos = pPos.relative(direction);
                BlockState airState = pLevel.getBlockState(airPos);
                if (airState.isAir()){
                    Optional<TechnetiumFiringRecipe> firingRecipe = pLevel.getRecipeManager().getRecipeFor(TechnetiumFiringRecipe.Type.INSTANCE,
                            new SimpleContainer(pBlockEntity.getItem(0)),pLevel);
                    if (firingRecipe.isPresent()) {
                        int cfe = firingRecipe.get().getCfe();
                        placeCFECloud(pLevel, airPos, cfe);
                    }
                    break;
                }
            }
        }
    }
}
