package net.sinedkadis.terracompositio.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.api.networks.cfe.CFECapability;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.cfe.PlayerCFEProvider;
import net.sinedkadis.terracompositio.entity.custom.FlowCedarEntEntity;
import net.sinedkadis.terracompositio.particle.CFEParticleData;
import net.sinedkadis.terracompositio.registries.TCEffects;
import net.sinedkadis.terracompositio.registries.TCFluids;
import net.sinedkadis.terracompositio.registries.TCItems;

@Mod.EventBusSubscriber(modid = TerraCompositio.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventBusEvents {
    private static int counter;
    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.players()) {
                if (player != null && !(player instanceof FakePlayer)) {
                    Vec3 pos = player.getEyePosition();
                    if (player.hasEffect(TCEffects.FLOW_SATURATION.get()) && counter++ > 5) {
                        counter = 0;
                        serverLevel.sendParticles(player, new CFEParticleData(1/20f),
                                true, pos.x, pos.y, pos.z, 10, 0.1, 0.1, 0.1, 0.0);
                    }
                }
            }
        }
    }
    @SubscribeEvent
    public static void onLivingTickEvent(LivingEvent.LivingTickEvent event){
        LivingEntity livingEntity = event.getEntity();
        FluidState fluidstate = livingEntity.level().getFluidState(livingEntity.blockPosition());
        if (fluidstate.getFluidType() == TCFluids.FLOW_FLUID.type.get()  && !livingEntity.canStandOnFluid(fluidstate) || livingEntity.hasEffect(TCEffects.FLOW_SATURATION.get())) {
            if (fluidstate.getFluidType() == TCFluids.FLOW_FLUID.type.get() && !livingEntity.canStandOnFluid(fluidstate) && livingEntity.hasEffect(TCEffects.FLOW_SATURATION.get())) {
                livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().scale(1.4F)); //todo: Achievement to both
            }else if (livingEntity.hasEffect(TCEffects.FLOW_SATURATION.get())) {
                if (livingEntity.onGround() || livingEntity.onClimbable())
                    livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().scale(1.2F));
            }else {
                livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().scale(1.2F));
            }
        }
        if (livingEntity instanceof Player player) {
            player.getCapability(CFECapability.CFE).ifPresent(ICFEHandler::containerTick);
        }

    }

    @SubscribeEvent
    public static void onPlayerOnEntClickEvent(PlayerInteractEvent.EntityInteractSpecific event){
        if (event.getTarget() instanceof FlowCedarEntEntity entity) {
            ItemStack pStack = event.getEntity().getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
            if (pStack.is(TCItems.TECHNETIUM_CROWN.get())) {
                if (head.isEmpty()) {
                    entity.setItemSlot(EquipmentSlot.HEAD,TCItems.TECHNETIUM_CROWN.get().getDefaultInstance());
                    pStack.shrink(1);
                    entity.setDropChance(EquipmentSlot.HEAD, 2.0F);
                    entity.setPersistenceRequired();
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                    return;
                }
            }
            if (pStack.isEmpty()) {
                if (!head.isEmpty()) {
                    head.shrink(1);
                    event.getEntity().setItemInHand(InteractionHand.MAIN_HAND,TCItems.TECHNETIUM_CROWN.get().getDefaultInstance());
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            if (!player.getCapability(CFECapability.CFE).isPresent()) {
                event.addCapability(TerraCompositio.modLoc("cfe_stored"), new PlayerCFEProvider(player));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if(event.isWasDeath()) {
            event.getOriginal().reviveCaps();
            event.getOriginal().getCapability(CFECapability.CFE).ifPresent(oldStore ->
                    event.getEntity().getCapability(CFECapability.CFE).ifPresent(newStore -> {
                CompoundTag tag = new CompoundTag();
                oldStore.writeToNBT(tag);
                newStore.readFromNBT(tag);
            }));
            event.getOriginal().invalidateCaps();
        }
    }
}