package net.sinedkadis.terracompositio.registries;


import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.entity.*;
import net.sinedkadis.terracompositio.compat.CompatUtils;
import net.sinedkadis.terracompositio.compat.create.block.entity.CedarGearboxBlockEntity;

import java.util.function.Supplier;

import static net.sinedkadis.terracompositio.registries.TCBlocks.*;

@SuppressWarnings({"DataFlowIssue", "unchecked"})
public class TCBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TerraCompositio.MOD_ID);

    public static final RegistryObject<BlockEntityType<FlowCedarPortBlockEntity>> FLOW_PORT_BE=
            BLOCK_ENTITIES.register("flow_port_be", () ->
                    BlockEntityType.Builder.of(FlowCedarPortBlockEntity::new,
                            FLOW_CEDAR_PORT.get()).build(null));
    public static final RegistryObject<BlockEntityType<CreativeCFESourceBlockEntity>> CREATIVE_CFE_SOURCE_BE =
            BLOCK_ENTITIES.register("creative_cfe_source_be", () ->
                    BlockEntityType.Builder.of(CreativeCFESourceBlockEntity::new,
                            CREATIVE_CFE_SOURCE.get()).build(null));
    public static final RegistryObject<BlockEntityType<CFETrashCanBlockEntity>> CFE_TRASH_CAN_BE =
            BLOCK_ENTITIES.register("cfe_trash_can_be", () ->
                    BlockEntityType.Builder.of(CFETrashCanBlockEntity::new,
                            CFE_TRASH_CAN.get()).build(null));
    public static final RegistryObject<BlockEntityType<FlowInfuserBlockEntity>> FLOW_INFUSER_BE =
            BLOCK_ENTITIES.register("flow_infuser_be", () ->
                    BlockEntityType.Builder.of(FlowInfuserBlockEntity::new,
                            FLOW_INFUSER.get()).build(null));
    public static final RegistryObject<BlockEntityType<MatterInfuserPortBlockEntity>> MATTER_INFUSER_PORT_BE =
            BLOCK_ENTITIES.register("matter_infuser_port_be", () ->
                    BlockEntityType.Builder.of(MatterInfuserPortBlockEntity::new,
                            MATTER_INFUSER_PORT.get()).build(null));
    public static final RegistryObject<BlockEntityType<MatterInfuserIOBlockEntity>> MATTER_INFUSER_IO_BE =
            BLOCK_ENTITIES.register("matter_infuser_io_be", () ->
                    BlockEntityType.Builder.of(MatterInfuserIOBlockEntity::new,
                            MATTER_INFUSER_IO.get()).build(null));
    public static final RegistryObject<BlockEntityType<FlowCedarCasingBlockEntity>> FLOW_CEDAR_CASING_BE =
            BLOCK_ENTITIES.register("flow_cedar_casing_io_be", () ->
                    BlockEntityType.Builder.of(FlowCedarCasingBlockEntity::new,
                            FLOW_CEDAR_CASING.get()).build(null));



    public static final RegistryObject<BlockEntityType<ModSignBlockEntity>> MOD_SIGN=
            BLOCK_ENTITIES.register("mod_sign", () ->
                    BlockEntityType.Builder.of(ModSignBlockEntity::new,
                            FLOW_CEDAR_SIGN.get(), FLOW_CEDAR_WALL_SIGN.get()).build(null));
    public static final RegistryObject<BlockEntityType<ModHangingSignBlockEntity>> MOD_HANGING_SIGN=
            BLOCK_ENTITIES.register("mod_hanging_sign", () ->
                    BlockEntityType.Builder.of(ModHangingSignBlockEntity::new,
                            FLOW_CEDAR_HANGING_SIGN.get(), FLOW_CEDAR_WALL_HANGING_SIGN.get()).build(null));

    public static final RegistryObject<BlockEntityType<ConstructionDesorberBlockEntity>> CONSTRUCTION_DESORBER_BE =
            BLOCK_ENTITIES.register("construction_desorber_be", () ->
                    BlockEntityType.Builder.of(ConstructionDesorberBlockEntity::new,
                            CONSTRUCTION_DESORBER.get()).build(null));
    public static final RegistryObject<BlockEntityType<CultivationDesorberBlockEntity>> CULTIVATION_DESORBER_BE =
            BLOCK_ENTITIES.register("cultivation_desorber_be", () ->
                    BlockEntityType.Builder.of(CultivationDesorberBlockEntity::new,
                            CULTIVATION_DESORBER.get()).build(null));
    public static final RegistryObject<BlockEntityType<TimePassageDesorberBlockEntity>> TIME_PASSAGE_DESORBER_BE =
            BLOCK_ENTITIES.register("time_passage_desorber_be", () ->
                    BlockEntityType.Builder.of(TimePassageDesorberBlockEntity::new,
                            TIME_PASSAGE_DESORBER.get()).build(null));
    public static final RegistryObject<BlockEntityType<FlowCedarTankBlockEntity>> FLOW_CEDAR_TANK_BE =
            BLOCK_ENTITIES.register("flow_cedar_tank_be", () ->
                    BlockEntityType.Builder.of(FlowCedarTankBlockEntity::new,
                            FLOW_CEDAR_TANK.get()).build(null));

    public static final RegistryObject<BlockEntityType<PathPointerBlockEntity>> PATH_POINTER_BE =
            registerBE("path_pointer_be",PathPointerBlockEntity::new,
                    PP_COLLECTOR,
                    PP_EMITTER,
                    PP_RECEIVER,
                    PP_SENDER,
                    PP_EXTRACTOR,
                    PP_INFUSER);

    public static final RegistryObject<BlockEntityType<EntStatueBlockEntity>> ENT_STATUE_BE =
            BLOCK_ENTITIES.register("ent_statue_be", () ->
                    BlockEntityType.Builder.of(EntStatueBlockEntity::new,
                            FLOW_CEDAR_ENT_STATUE.get()).build(null));
    public static final RegistryObject<BlockEntityType<AirSaturatorBlockEntity>> AIR_SATURATOR_BE =
            registerBE("air_saturator_be",AirSaturatorBlockEntity::new, AIR_SATURATOR);

    public static final RegistryObject<BlockEntityType<FloatingTorchHolderBlockEntity>> FLOATING_TORCH_HOLDER_BE =
            registerBE("floating_torch_holder_be",FloatingTorchHolderBlockEntity::new, FLOATING_TORCH_HOLDER);

    public static final RegistryObject<BlockEntityType<CedarGearboxBlockEntity>> CEDAR_GEARBOX_BE =
            registerBE("cedar_gearbox_be",CedarGearboxBlockEntity::new, CompatUtils.CREATE_EXISTENCE, CEDAR_GEARBOX);


    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> registerBE(String name, BlockEntityType.BlockEntitySupplier<T> blockEntity, RegistryObject<Block>... blocks) {
        return registerBE(name, blockEntity,  () -> true, blocks);
    }

    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> registerBE(String name, BlockEntityType.BlockEntitySupplier<T> blockEntity, Supplier<Boolean> predicate, RegistryObject<Block>... blocks) {
        if (!predicate.get()) return null;

        return BLOCK_ENTITIES.register(name, () -> {
            Block[] oBlocks = new Block[blocks.length];
            for (int i = 0; i < blocks.length; i++) {
                RegistryObject<Block> block = blocks[i];
                oBlocks[i] = block.get();
            }
            return BlockEntityType.Builder.of(blockEntity,
                    oBlocks).build(null);
        });
    }

    public static void register(IEventBus eventBus){
        BLOCK_ENTITIES.register(eventBus);
    }
}
