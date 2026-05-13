package net.sinedkadis.terracompositio.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.config.TCInnerConfig;
import net.sinedkadis.terracompositio.registries.TCFluids;
import net.sinedkadis.terracompositio.registries.TCItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    @Unique
    private boolean tc$processing = false;

    @Unique
    private int tc$progress = 0;

    public ItemEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = "tick()V",
            at = @At("RETURN")
    )
    private void onTickEnd(CallbackInfo ci) {

        if (this.isRemoved()) return;

        ItemEntity self = (ItemEntity) (Object) this;
        Level level = level();

        // =========================
        // ПРОЦЕСС УЖЕ ИДЕТ
        // =========================

        if (tc$processing) {

            if (level.isClientSide()) {

//                level.addParticle(
//                        ParticleTypes.WAX_ON,
//                        getX(),
//                        getY() + 1.3f,
//                        getZ(),
//                        0,
//                        0,
//                        0
//                );

                return;
            }

            ServerLevel serverLevel = (ServerLevel) level;

            BlockPos center = blockPosition();

            Iterable<BlockPos> positions = BlockPos.betweenClosed(
                    center.offset(-3, -3, -3),
                    center.offset(3, 3, 3)
            );

            int totalDuration = TCInnerConfig.instance.TOTAL_TICKS;
            int totalRandomTicks = TCInnerConfig.instance.TOTAL_RANDOM_TICKS;

            for (BlockPos pos : positions) {

                BlockState state = serverLevel.getBlockState(pos);

                if (!state.is(BlockTags.CROPS))
                    continue;


                int RANDOM_TICKS_PER_TICK = totalRandomTicks / totalDuration;
                for (int i = 0; i < RANDOM_TICKS_PER_TICK; i++) {
                    state.randomTick(
                            serverLevel,
                            pos,
                            serverLevel.random
                    );

                    // обновляем state после randomTick,
                    // потому что блок мог измениться
                    state = serverLevel.getBlockState(pos);

                    if (!state.is(BlockTags.CROPS))
                        break;
                }
            }

            tc$progress++;

            if (tc$progress >= totalDuration) {
                discard();
            }

            return;
        }

        // =========================
        // СТАРТ ПРОЦЕССА
        // =========================

        if (!self.getItem().is(TCItems.INFUSED_FERTILIZER.get()))
            return;

        FluidState fluidState = level.getFluidState(blockPosition());

        if (!fluidState.is(TCFluids.FLOW_FLUID.source.get()))
            return;

        tc$processing = true;

        // запрет подбора
        self.setPickUpDelay(Integer.MAX_VALUE);

        // выключаем физику
        self.setDeltaMovement(Vec3.ZERO);
        self.setNoGravity(true);

        // фикс позиции
        self.setPos(
                blockPosition().getX() + 0.5,
                blockPosition().getY() + 0.2,
                blockPosition().getZ() + 0.5
        );
    }
}