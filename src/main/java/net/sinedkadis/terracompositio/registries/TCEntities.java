package net.sinedkadis.terracompositio.registries;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.entity.custom.FlowCedarEntEntity;
import net.sinedkadis.terracompositio.entity.custom.TCBoatEntity;
import net.sinedkadis.terracompositio.entity.custom.TCChestBoatEntity;

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
            ENTITY_TYPES.register("flow_cedar_ent_entity", () -> EntityType.Builder.of(FlowCedarEntEntity::new, MobCategory.MISC)
                    .sized(0.5f, 1.5f).build("flow_cedar_ent_entity"));


    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}