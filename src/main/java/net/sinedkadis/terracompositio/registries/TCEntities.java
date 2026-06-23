package net.sinedkadis.terracompositio.registries;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.ecf.burst.ECFBurstProjectileEntity;
import net.sinedkadis.terracompositio.entity.custom.*;

public class TCEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TerraCompositio.MOD_ID);


    public static final RegistryObject<EntityType<TCBoatEntity>> TC_BOAT =
            ENTITY_TYPES.register("tc_boat", () -> EntityType.Builder.<TCBoatEntity>of(TCBoatEntity::new, MobCategory.MISC)
                    .sized(1.375f, 0.5625f).build("tc_boat"));
    public static final RegistryObject<EntityType<TCChestBoatEntity>> TC_CHEST_BOAT =
            ENTITY_TYPES.register("tc_chest_boat", () -> EntityType.Builder.<TCChestBoatEntity>of(TCChestBoatEntity::new, MobCategory.MISC)
                    .sized(1.375f, 0.5625f).build("tc_chest_boat"));

    public static final RegistryObject<EntityType<FlowCedarEntEntity>> FLOW_CEDAR_ENT =
            ENTITY_TYPES.register("flow_cedar_ent_entity", () -> EntityType.Builder.of(FlowCedarEntEntity::new, MobCategory.CREATURE)
                    .sized(0.5f, 1.5f).build("flow_cedar_ent_entity"));

    public static final RegistryObject<EntityType<ECFBallProjectileEntity>> ECF_BALL_PROJECTILE =
            ENTITY_TYPES.register("ecf_ball_projectile", () -> EntityType.Builder.<ECFBallProjectileEntity>of(ECFBallProjectileEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).build("ecf_ball_projectile"));
    public static final RegistryObject<EntityType<ECFDropProjectileEntity>> ECF_DROP_PROJECTILE =
            ENTITY_TYPES.register("ecf_drop_projectile", () -> EntityType.Builder.<ECFDropProjectileEntity>of(ECFDropProjectileEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).build("ecf_drop_projectile"));

    public static final RegistryObject<EntityType<ECFBurstProjectileEntity>> ECF_BURST_PROJECTILE =
            ENTITY_TYPES.register("ecf_burst_projectile", () -> EntityType.Builder.<ECFBurstProjectileEntity>of(ECFBurstProjectileEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).build("ecf_burst_projectile"));

    public static final RegistryObject<EntityType<ECFCloudEntity>> ECF_CLOUD =
            ENTITY_TYPES.register("ecf_cloud_entity", () -> EntityType.Builder.<ECFCloudEntity>of(ECFCloudEntity::new, MobCategory.MISC)
                    .build("ecf_cloud_entity"));


    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}