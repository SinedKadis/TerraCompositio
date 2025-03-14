package net.sinedkadis.terracompositio.registries;


import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.entity.*;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TerraCompositio.MOD_ID);

    public static final RegistryObject<BlockEntityType<FlowPortBlockEntity>> FLOW_PORT_BE=
            BLOCK_ENTITIES.register("flow_port_be", () ->
                    BlockEntityType.Builder.of(FlowPortBlockEntity::new,
                            ModBlocks.FLOW_PORT.get()).build(null));
    public static final RegistryObject<BlockEntityType<FlowExtractorBlockEntity>> FLOW_EXTRACTOR_BE=
            BLOCK_ENTITIES.register("flow_extractor_be", () ->
                    BlockEntityType.Builder.of(FlowExtractorBlockEntity::new,
                            ModBlocks.FLOW_EXTRACTOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<CreativeCFESourceBlockEntity>> CREATIVE_CFE_SOURCE_BE =
            BLOCK_ENTITIES.register("creative_cfe_source_be", () ->
                    BlockEntityType.Builder.of(CreativeCFESourceBlockEntity::new,
                            ModBlocks.CREATIVE_CFE_SOURCE.get()).build(null));
    public static final RegistryObject<BlockEntityType<FlowInfuserBlockEntity>> FLOW_INFUSER_BE =
            BLOCK_ENTITIES.register("flow_infuser_be", () ->
                    BlockEntityType.Builder.of(FlowInfuserBlockEntity::new,
                            ModBlocks.FLOW_INFUSER.get()).build(null));
    public static final RegistryObject<BlockEntityType<MatterInfuserPortBlockEntity>> MATTER_INFUSER_PORT_BE =
            BLOCK_ENTITIES.register("matter_infuser_port_be", () ->
                    BlockEntityType.Builder.of(MatterInfuserPortBlockEntity::new,
                            ModBlocks.MATTER_INFUSER_PORT.get()).build(null));
    public static final RegistryObject<BlockEntityType<MatterInfuserIOBlockEntity>> MATTER_INFUSER_IO_BE =
            BLOCK_ENTITIES.register("matter_infuser_io_be", () ->
                    BlockEntityType.Builder.of(MatterInfuserIOBlockEntity::new,
                            ModBlocks.MATTER_INFUSER_IO.get()).build(null));
    public static final RegistryObject<BlockEntityType<FlowCedarCasingBlockEntity>> FLOW_CEDAR_CASING_BE =
            BLOCK_ENTITIES.register("flow_cedar_casing_io_be", () ->
                    BlockEntityType.Builder.of(FlowCedarCasingBlockEntity::new,
                            ModBlocks.FLOW_CEDAR_CASING.get()).build(null));



    public static final RegistryObject<BlockEntityType<ModSignBlockEntity>> MOD_SIGN=
            BLOCK_ENTITIES.register("mod_sign", () ->
                    BlockEntityType.Builder.of(ModSignBlockEntity::new,
                            ModBlocks.FLOW_CEDAR_SIGN.get(),ModBlocks.FLOW_CEDAR_WALL_SIGN.get()).build(null));
    public static final RegistryObject<BlockEntityType<ModHangingSignBlockEntity>> MOD_HANGING_SIGN=
            BLOCK_ENTITIES.register("mod_hanging_sign", () ->
                    BlockEntityType.Builder.of(ModHangingSignBlockEntity::new,
                            ModBlocks.FLOW_CEDAR_HANGING_SIGN.get(),ModBlocks.FLOW_CEDAR_WALL_HANGING_SIGN.get()).build(null));



    public static void register(IEventBus eventBus){
        BLOCK_ENTITIES.register(eventBus);
    }
}
