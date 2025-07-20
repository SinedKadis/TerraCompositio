package net.sinedkadis.terracompositio.registries;


import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.entity.*;

@SuppressWarnings("DataFlowIssue")
public class TCBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TerraCompositio.MOD_ID);

    public static final RegistryObject<BlockEntityType<FlowCedarPortBlockEntity>> FLOW_PORT_BE=
            BLOCK_ENTITIES.register("flow_port_be", () ->
                    BlockEntityType.Builder.of(FlowCedarPortBlockEntity::new,
                            TCBlocks.FLOW_CEDAR_PORT.get()).build(null));
    public static final RegistryObject<BlockEntityType<CreativeCFESourceBlockEntity>> CREATIVE_CFE_SOURCE_BE =
            BLOCK_ENTITIES.register("creative_cfe_source_be", () ->
                    BlockEntityType.Builder.of(CreativeCFESourceBlockEntity::new,
                            TCBlocks.CREATIVE_CFE_SOURCE.get()).build(null));
    public static final RegistryObject<BlockEntityType<CFETrashCanBlockEntity>> CFE_TRASH_CAN_BE =
            BLOCK_ENTITIES.register("cfe_trash_can_be", () ->
                    BlockEntityType.Builder.of(CFETrashCanBlockEntity::new,
                            TCBlocks.CFE_TRASH_CAN.get()).build(null));
    public static final RegistryObject<BlockEntityType<FlowInfuserBlockEntity>> FLOW_INFUSER_BE =
            BLOCK_ENTITIES.register("flow_infuser_be", () ->
                    BlockEntityType.Builder.of(FlowInfuserBlockEntity::new,
                            TCBlocks.FLOW_INFUSER.get()).build(null));
    public static final RegistryObject<BlockEntityType<MatterInfuserPortBlockEntity>> MATTER_INFUSER_PORT_BE =
            BLOCK_ENTITIES.register("matter_infuser_port_be", () ->
                    BlockEntityType.Builder.of(MatterInfuserPortBlockEntity::new,
                            TCBlocks.MATTER_INFUSER_PORT.get()).build(null));
    public static final RegistryObject<BlockEntityType<MatterInfuserIOBlockEntity>> MATTER_INFUSER_IO_BE =
            BLOCK_ENTITIES.register("matter_infuser_io_be", () ->
                    BlockEntityType.Builder.of(MatterInfuserIOBlockEntity::new,
                            TCBlocks.MATTER_INFUSER_IO.get()).build(null));
    public static final RegistryObject<BlockEntityType<FlowCedarCasingBlockEntity>> FLOW_CEDAR_CASING_BE =
            BLOCK_ENTITIES.register("flow_cedar_casing_io_be", () ->
                    BlockEntityType.Builder.of(FlowCedarCasingBlockEntity::new,
                            TCBlocks.FLOW_CEDAR_CASING.get()).build(null));



    public static final RegistryObject<BlockEntityType<ModSignBlockEntity>> MOD_SIGN=
            BLOCK_ENTITIES.register("mod_sign", () ->
                    BlockEntityType.Builder.of(ModSignBlockEntity::new,
                            TCBlocks.FLOW_CEDAR_SIGN.get(), TCBlocks.FLOW_CEDAR_WALL_SIGN.get()).build(null));
    public static final RegistryObject<BlockEntityType<ModHangingSignBlockEntity>> MOD_HANGING_SIGN=
            BLOCK_ENTITIES.register("mod_hanging_sign", () ->
                    BlockEntityType.Builder.of(ModHangingSignBlockEntity::new,
                            TCBlocks.FLOW_CEDAR_HANGING_SIGN.get(), TCBlocks.FLOW_CEDAR_WALL_HANGING_SIGN.get()).build(null));

    public static final RegistryObject<BlockEntityType<ConstructionDesorberBlockEntity>> CONSTRUCTION_DESORBER_BE =
            BLOCK_ENTITIES.register("construction_desorber_be", () ->
                    BlockEntityType.Builder.of(ConstructionDesorberBlockEntity::new,
                            TCBlocks.CONSTRUCTION_DESORBER.get()).build(null));
    public static final RegistryObject<BlockEntityType<CultivationDesorberBlockEntity>> CULTIVATION_DESORBER_BE =
            BLOCK_ENTITIES.register("cultivation_desorber_be", () ->
                    BlockEntityType.Builder.of(CultivationDesorberBlockEntity::new,
                            TCBlocks.CULTIVATION_DESORBER.get()).build(null));
    public static final RegistryObject<BlockEntityType<TimePassageDesorberBlockEntity>> TIME_PASSAGE_DESORBER_BE =
            BLOCK_ENTITIES.register("time_passage_desorber_be", () ->
                    BlockEntityType.Builder.of(TimePassageDesorberBlockEntity::new,
                            TCBlocks.TIME_PASSAGE_DESORBER.get()).build(null));
    public static final RegistryObject<BlockEntityType<FlowCedarTankBlockEntity>> FLOW_CEDAR_TANK_BE =
            BLOCK_ENTITIES.register("flow_cedar_tank_be", () ->
                    BlockEntityType.Builder.of(FlowCedarTankBlockEntity::new,
                            TCBlocks.FLOW_CEDAR_TANK.get()).build(null));
    public static final RegistryObject<BlockEntityType<CFESaturatedAirBlockEntity>> CFE_SATURATED_AIR_BE =
            BLOCK_ENTITIES.register("cfe_saturated_air_be", () ->
                    BlockEntityType.Builder.of(CFESaturatedAirBlockEntity::new,
                            TCBlocks.CFE_SATURATED_AIR.get()).build(null));

    public static final RegistryObject<BlockEntityType<PathPointerBlockEntity>> PATH_POINTER_BE =
            BLOCK_ENTITIES.register("path_pointer_be", () ->
                    BlockEntityType.Builder.of(PathPointerBlockEntity::new,
                            TCBlocks.PP_RECEIVER.get(),
                            TCBlocks.PP_COLLECTOR.get(),
                            TCBlocks.PP_EMITTER.get(),
                            TCBlocks.PP_SENDER.get()
                    ).build(null));



    public static void register(IEventBus eventBus){
        BLOCK_ENTITIES.register(eventBus);
    }
}
