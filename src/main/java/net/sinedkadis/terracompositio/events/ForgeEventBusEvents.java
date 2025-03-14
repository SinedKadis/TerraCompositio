package net.sinedkadis.terracompositio.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.effect.ModEffects;
import net.sinedkadis.terracompositio.registries.ModFluids;
import net.sinedkadis.terracompositio.registries.ModParticles;

@Mod.EventBusSubscriber(modid = TerraCompositio.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventBusEvents {
    private static int counter;
    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.players()) {
                if (player != null && !(player instanceof FakePlayer)) {
                    Vec3 pos = player.getEyePosition();
                    if (player.hasEffect(ModEffects.FLOW_SATURATION.get()) && counter++ > 5) {
                        counter = 0;
                        serverLevel.sendParticles(player, ModParticles.FLOW_STILL_PARTICLE.get(),
                                true, pos.x, pos.y, pos.z, 10, 0.1, 0.1, 0.1, 0.0);
                    }
                }
            }
        }
    }
    @SubscribeEvent
    public static void onLivingTickEvent(LivingEvent.LivingTickEvent event){
        FluidState fluidstate = event.getEntity().level().getFluidState(event.getEntity().blockPosition());
        if (fluidstate.getFluidType() == ModFluids.FLOW_FLUID.type.get()  && !event.getEntity().canStandOnFluid(fluidstate) || event.getEntity().hasEffect(ModEffects.FLOW_SATURATION.get())) {
            if (fluidstate.getFluidType() == ModFluids.FLOW_FLUID.type.get() && !event.getEntity().canStandOnFluid(fluidstate) && event.getEntity().hasEffect(ModEffects.FLOW_SATURATION.get())) {
                event.getEntity().setDeltaMovement(event.getEntity().getDeltaMovement().scale(1.4F)); //todo: Achievement to both
            }else if (event.getEntity().hasEffect(ModEffects.FLOW_SATURATION.get())) {
                if (event.getEntity().onGround() || event.getEntity().onClimbable())
                    event.getEntity().setDeltaMovement(event.getEntity().getDeltaMovement().scale(1.2F));
            }else {
                event.getEntity().setDeltaMovement(event.getEntity().getDeltaMovement().scale(1.2F));
            }
        }
    }
}