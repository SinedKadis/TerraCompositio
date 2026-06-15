package net.sinedkadis.terracompositio.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.cfe.PlayerCFEProvider;
import net.sinedkadis.terracompositio.entity.custom.FlowCedarEntEntity;
import net.sinedkadis.terracompositio.registries.TCEffects;
import net.sinedkadis.terracompositio.registries.TCFluids;
import net.sinedkadis.terracompositio.registries.TCItems;
import net.sinedkadis.terracompositio.util.helpers.CFEHelper;

@Mod.EventBusSubscriber(modid = TerraCompositio.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventBusEvents {
    @SubscribeEvent
    public static void onTickLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        long gameTime = event.level.getGameTime();
        CFEHelper.transferManager().applyTransfers(gameTime);
        CFEHelper.CFESpawnQueue.flushSpawns();
    }

    @SubscribeEvent
    public static void onLivingTickEvent(LivingEvent.LivingTickEvent event){
        LivingEntity livingEntity = event.getEntity();

        applyLiquidFlowEffect(livingEntity);
    }

    private static void applyLiquidFlowEffect(LivingEntity livingEntity) {
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
    }

    @SubscribeEvent
    public static void onPlayerOnEntClickEvent(PlayerInteractEvent.EntityInteractSpecific event){
        if (event.getTarget() instanceof FlowCedarEntEntity entity) {
            ItemStack pStack = event.getEntity().getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
            if (pStack.is(TCItems.TECHNETIUM_CROWN.get())) {
                if (head.isEmpty()) {
                    entity.setItemSlot(EquipmentSlot.HEAD,pStack.copy());
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
                    event.getEntity().setItemInHand(InteractionHand.MAIN_HAND,head.copy());
                    head.shrink(1);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            if (!player.getCapability(TCCapabilities.CFE).isPresent()) {
                event.addCapability(TerraCompositio.modLoc("cfe_stored"), new PlayerCFEProvider(player));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if(event.isWasDeath()) {
            event.getOriginal().reviveCaps();
            event.getOriginal().getCapability(TCCapabilities.CFE).ifPresent(oldStore ->
                    event.getEntity().getCapability(TCCapabilities.CFE).ifPresent(newStore -> {
                CompoundTag tag = new CompoundTag();
                oldStore.writeToNBT(tag);
                newStore.readFromNBT(tag);
            }));
            event.getOriginal().invalidateCaps();
        }
    }
}