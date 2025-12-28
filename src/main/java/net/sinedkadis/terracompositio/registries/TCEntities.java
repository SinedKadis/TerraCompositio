package net.sinedkadis.terracompositio.registries;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.cfe.CFEBurstProjectileEntity;
import net.sinedkadis.terracompositio.entity.custom.*;

public class TCEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TerraCompositio.MOD_ID);


    public static final RegistryObject<EntityType<TCBoatEntity>> MOD_BOAT =
            ENTITY_TYPES.register("mod_boat", () -> EntityType.Builder.<TCBoatEntity>of(TCBoatEntity::new, MobCategory.MISC)
                    .sized(1.375f, 0.5625f).build("mod_boat"));
    public static final RegistryObject<EntityType<TCChestBoatEntity>> MOD_CHEST_BOAT =
            ENTITY_TYPES.register("mod_chest_boat", () -> EntityType.Builder.<TCChestBoatEntity>of(TCChestBoatEntity::new, MobCategory.MISC)
                    .sized(1.375f, 0.5625f).build("mod_chest_boat"));

    public static final RegistryObject<EntityType<FlowCedarEntEntity>> FLOW_CEDAR_ENT =
            ENTITY_TYPES.register("flow_cedar_ent_entity", () -> EntityType.Builder.of(FlowCedarEntEntity::new, MobCategory.CREATURE)
                    .sized(0.5f, 1.5f).build("flow_cedar_ent_entity"));

    public static final RegistryObject<EntityType<CFEBallProjectileEntity>> CFE_BALL_PROJECTILE =
            ENTITY_TYPES.register("cfe_ball_projectile", () -> EntityType.Builder.<CFEBallProjectileEntity>of(CFEBallProjectileEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).build("cfe_ball_projectile"));
    public static final RegistryObject<EntityType<CFEDropProjectileEntity>> CFE_DROP_PROJECTILE =
            ENTITY_TYPES.register("cfe_drop_projectile", () -> EntityType.Builder.<CFEDropProjectileEntity>of(CFEDropProjectileEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).build("cfe_drop_projectile"));

    public static final RegistryObject<EntityType<CFEBurstProjectileEntity>> CFE_BURST_PROJECTILE =
            ENTITY_TYPES.register("cfe_burst_projectile", () -> EntityType.Builder.<CFEBurstProjectileEntity>of(CFEBurstProjectileEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).build("cfe_burst_projectile"));


    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}