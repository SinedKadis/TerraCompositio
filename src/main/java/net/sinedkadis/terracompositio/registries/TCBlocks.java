package net.sinedkadis.terracompositio.registries;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.custom.*;
import net.sinedkadis.terracompositio.item.custom.UnstableTechnetiumBlockItem;
import net.sinedkadis.terracompositio.worldgen.tree.FlowCedarTreeGrower;

import java.util.function.Supplier;



public class TCBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, TerraCompositio.MOD_ID);

    private static <T extends Block>RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }
    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        TCItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
    private static <T extends Block>RegistryObject<T> registerUnstableTechnetiumBlock(String name, Supplier<T> block,int radiation) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerUnstableTechnetiumBlockItem(name, toReturn,radiation);
        return toReturn;
    }
    private static <T extends Block> void registerUnstableTechnetiumBlockItem(String name, RegistryObject<T> block,int radiation) {
        TCItems.ITEMS.register(name, () -> new UnstableTechnetiumBlockItem(block.get(), new Item.Properties(),radiation));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    public static final RegistryObject<Block> FLOW_CEDAR_LOG = registerBlock("flow_cedar_log",
            () -> new FlowCedarLikeBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG).strength(3f), TCBlocks.STRIPPED_FLOW_CEDAR_LOG));
    public static final RegistryObject<Block> FLOW_CEDAR_PORT = registerBlock("flow_cedar_port",
            () -> new FlowCedarPortBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WOOD).strength(3f), TCBlocks.STRIPPED_FLOW_CEDAR_LOG));

    public static final RegistryObject<Block> FLOW_CEDAR_WOOD = registerBlock("flow_cedar_wood",
            () -> new FlowCedarLikeBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WOOD).strength(3f), TCBlocks.STRIPPED_FLOW_CEDAR_WOOD));
    public static final RegistryObject<Block> STRIPPED_FLOW_CEDAR_LOG = registerBlock("stripped_flow_cedar_log",
            () -> new FlowCedarLikeBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_LOG).strength(3f)));
    public static final RegistryObject<Block> STRIPPED_FLOW_CEDAR_WOOD = registerBlock("stripped_flow_cedar_wood",
            () -> new FlowCedarLikeBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_WOOD).strength(3f)));

    public static final RegistryObject<Block> FLOW_CEDAR_PLANKS = registerBlock("flow_cedar_planks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)){
                @Override
                public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return true;
                }

                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 20;
                }

                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 5;
                }
            });
    public static final RegistryObject<Block> FLOW_CEDAR_LEAVES = registerBlock("flow_cedar_leaves",
            () -> new FlowCedarLeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)));
    public static final RegistryObject<Block> FLOW_CEDAR_STAIRS = registerBlock("flow_cedar_stairs",
            () -> new StairBlock(() -> TCBlocks.FLOW_CEDAR_PLANKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)));
    public static final RegistryObject<Block> FLOW_CEDAR_SLAB = registerBlock("flow_cedar_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(3f)));
    public static final RegistryObject<Block> FLOW_CEDAR_BUTTON = registerBlock("flow_cedar_button",
            () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS),
                    BlockSetType.OAK,30,true));
    public static final RegistryObject<Block> FLOW_CEDAR_PRESSURE_PLATE = registerBlock("flow_cedar_pressure_plate",
            () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING,
                    BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS),BlockSetType.OAK));
    public static final RegistryObject<Block> FLOW_CEDAR_FENCE = registerBlock("flow_cedar_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)));
    public static final RegistryObject<Block> FLOW_CEDAR_FENCE_GATE = registerBlock("flow_cedar_fence_gate",
            () -> new FenceGateBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS), SoundEvents.FENCE_GATE_OPEN,SoundEvents.FENCE_GATE_CLOSE));
    public static final RegistryObject<Block> FLOW_CEDAR_DOOR = registerBlock("flow_cedar_door",
            () -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).noOcclusion(), BlockSetType.OAK));
    public static final RegistryObject<Block> FLOW_CEDAR_TRAPDOOR = registerBlock("flow_cedar_trapdoor",
            () -> new TrapDoorBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).noOcclusion(),BlockSetType.OAK));

    public static final RegistryObject<Block> FLOW_CAULDRON = registerBlock("flow_cauldron",
            () -> new FlowCauldronBlock(BlockBehaviour.Properties.copy(Blocks.CAULDRON), null,CauldronInteraction.EMPTY));
    public static final RegistryObject<Block> BIRCH_JUICE_CAULDRON = registerBlock("birch_juice_cauldron",
            () -> new BirchJuiceCauldronBlock(BlockBehaviour.Properties.copy(Blocks.CAULDRON), null,CauldronInteraction.EMPTY));
    public static final RegistryObject<Block> WEDGE = registerBlock("wedge",
            () -> new WedgeBlock(BlockBehaviour.Properties.copy(Blocks.TRIPWIRE_HOOK)));


    public static final RegistryObject<Block> FLOW_CEDAR_SIGN = BLOCKS.register("flow_cedar_sign",
            () -> new ModStandingSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SIGN), TCWoodTypes.FLOW_CEDAR));
    public static final RegistryObject<Block> FLOW_CEDAR_WALL_SIGN = BLOCKS.register("flow_cedar_wall_sign",
            () -> new TCWallSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WALL_SIGN), TCWoodTypes.FLOW_CEDAR));
    public static final RegistryObject<Block> FLOW_CEDAR_HANGING_SIGN = BLOCKS.register("flow_cedar_hanging_sign",
            () -> new TCHangingSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_HANGING_SIGN), TCWoodTypes.FLOW_CEDAR));
    public static final RegistryObject<Block> FLOW_CEDAR_WALL_HANGING_SIGN = BLOCKS.register("flow_cedar_wall_hanging_sign",
            () -> new TCWallHangingSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WALL_HANGING_SIGN), TCWoodTypes.FLOW_CEDAR));

    public static final RegistryObject<Block> CREATIVE_CFE_SOURCE = registerBlock("creative_cfe_source",
            () -> new CreativeCFESourceBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<Block> CFE_TRASH_CAN = registerBlock("cfe_trash_can",
            () -> new CFETrashCanBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<Block> FLOW_INFUSER = registerBlock("flow_infuser",
            () -> new FlowInfuserBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion(), TCBlocks.STRIPPED_FLOW_CEDAR_LOG));
    public static final RegistryObject<Block> TECHNETIUM_ORE = registerUnstableTechnetiumBlock("technetium_ore",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_ORE)),2);
    public static final RegistryObject<Block> TECHNETIUM_DEEPSLATE_ORE = registerUnstableTechnetiumBlock("technetium_deepslate_ore",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_IRON_ORE)),2);
    public static final RegistryObject<Block> TECHNETIUM_RAW_ORE_BLOCK = registerUnstableTechnetiumBlock("technetium_raw_ore_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.RAW_IRON_BLOCK)),8);
    public static final RegistryObject<Block> FLOW_CEDAR_SAPLING = registerBlock("flow_cedar_sapling",
            () -> new FlowCedarSaplingBlock(new FlowCedarTreeGrower(), BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING)));
    public static final RegistryObject<Block> FLOW_CEDAR_CASING = registerBlock("flow_cedar_casing",
            () -> new FlowCedarCasingBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_LOG).strength(3f)));
    public static final RegistryObject<Block> MATTER_INFUSER_PORT = registerBlock("matter_infuser_port",
            () -> new MatterInfuserPortBlock(BlockBehaviour.Properties.copy(Blocks.TRIPWIRE_HOOK).sound(SoundType.COPPER).strength(3f)));
    public static final RegistryObject<Block> MATTER_INFUSER_IO = registerBlock("matter_infuser_io",
            () -> new MatterInfuserIOBlock(BlockBehaviour.Properties.copy(Blocks.TRIPWIRE_HOOK).sound(SoundType.COPPER).strength(3f)));

    public static final RegistryObject<Block> CONSTRUCTION_DESORBER = registerBlock("construction_desorber",
            () -> new ConstructionDesorberBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(3f).noOcclusion()));
    public static final RegistryObject<Block> CULTIVATION_DESORBER = registerBlock("cultivation_desorber",
            () -> new CultivationDesorberBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(3f).noOcclusion()));
    public static final RegistryObject<Block> TIME_PASSAGE_DESORBER = registerBlock("time_passage_desorber",
            () -> new TimePassageDesorberBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(3f).noOcclusion()));

    public static final RegistryObject<Block> FLOW_CEDAR_PEDESTAL = registerBlock("flow_cedar_pedestal",
            () -> new FlowCedarPedestalBlock(BlockBehaviour.Properties.copy(Blocks.AZALEA).noOcclusion()));
    public static final RegistryObject<Block> FLOW_CEDAR_TANK = registerBlock("flow_cedar_tank",
            () -> new FlowCedarTankBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).noOcclusion()));

    public static final RegistryObject<Block> CFE_SATURATED_AIR = registerBlock("cfe_saturated_air",
            () -> new CFESaturatedAirBlock(BlockBehaviour.Properties.copy(Blocks.AIR).noLootTable()));

    public static final RegistryObject<Block> PP_RECEIVER = registerBlock("pp_receiver",
            () -> new PathPointerBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).noOcclusion(), PathPointerBlock.PPPart.RECEIVER));
    public static final RegistryObject<Block> PP_COLLECTOR = registerBlock("pp_collector",
            () -> new PathPointerBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).noOcclusion(), PathPointerBlock.PPPart.COLLECTOR));
    public static final RegistryObject<Block> PP_SENDER = registerBlock("pp_sender",
            () -> new PathPointerBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).noOcclusion(), PathPointerBlock.PPPart.SENDER));
    public static final RegistryObject<Block> PP_EMITTER = registerBlock("pp_emitter",
            () -> new PathPointerBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).noOcclusion(), PathPointerBlock.PPPart.EMITTER));

    public static final RegistryObject<Block> TECHNETIUM_BLOCK = registerBlock("technetium_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.NETHERITE_BLOCK)));

}
