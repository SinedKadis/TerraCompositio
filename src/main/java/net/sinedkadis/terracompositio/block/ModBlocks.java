package net.sinedkadis.terracompositio.block;

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
import net.sinedkadis.terracompositio.item.ModItems;
import net.sinedkadis.terracompositio.util.ModWoodTypes;
import net.sinedkadis.terracompositio.worldgen.tree.BigFlowCedarTreeGrower;
import net.sinedkadis.terracompositio.worldgen.tree.FlowCedarTreeGrower;

import java.util.function.Supplier;



public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, TerraCompositio.MOD_ID);

    private static <T extends Block>RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }
    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    public static final RegistryObject<Block> FLOW_CEDAR_LOG = registerBlock("flow_cedar_log",
            () -> new FlowCedarLikeBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG).strength(3f)));
    public static final RegistryObject<Block> FLOW_PORT = registerBlock("flow_port",
            () -> new FlowPortBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WOOD).strength(3f)));

    public static final RegistryObject<Block> FLOW_CEDAR_WOOD = registerBlock("flow_cedar_wood",
            () -> new FlowCedarLikeBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WOOD).strength(3f)));
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
            () -> new FlowCedarLeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES).noLootTable()));
    public static final RegistryObject<Block> FLOW_CEDAR_STAIRS = registerBlock("flow_cedar_stairs",
            () -> new StairBlock(() -> ModBlocks.FLOW_CEDAR_PLANKS.get().defaultBlockState(),
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
            () -> new ModStandingSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SIGN), ModWoodTypes.FLOW_CEDAR));
    public static final RegistryObject<Block> FLOW_CEDAR_WALL_SIGN = BLOCKS.register("flow_cedar_wall_sign",
            () -> new ModWallSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WALL_SIGN), ModWoodTypes.FLOW_CEDAR));
    public static final RegistryObject<Block> FLOW_CEDAR_HANGING_SIGN = BLOCKS.register("flow_cedar_hanging_sign",
            () -> new ModHangingSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_HANGING_SIGN), ModWoodTypes.FLOW_CEDAR));
    public static final RegistryObject<Block> FLOW_CEDAR_WALL_HANGING_SIGN = BLOCKS.register("flow_cedar_wall_hanging_sign",
            () -> new ModWallHangingSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WALL_HANGING_SIGN), ModWoodTypes.FLOW_CEDAR));

    public static final RegistryObject<Block> FLOW_EXTRACTOR = registerBlock("flow_extractor",
            () -> new FlowExtractorBlock(BlockBehaviour.Properties.copy(Blocks.DARK_OAK_PLANKS)));
    public static final RegistryObject<Block> CREATIVE_CFE_SOURCE = registerBlock("creative_cfe_source",
            () -> new CreativeCFESourceBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<Block> FLOW_INFUSER = registerBlock("flow_infuser",
            () -> new FlowInfuserBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));
    public static final RegistryObject<Block> FLOW_CONTAINING_ORE = registerBlock("flow_containing_ore",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_ORE)));
    public static final RegistryObject<Block> FLOW_CONTAINING_DEEPSLATE_ORE = registerBlock("flow_containing_deepslate_ore",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_IRON_ORE)));
    public static final RegistryObject<Block> FLOW_CONTAINING_RAW_ORE_BLOCK = registerBlock("flow_containing_raw_ore_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.RAW_IRON_BLOCK)));
    public static final RegistryObject<Block> FLOW_CEDAR_BIG_SAPLING = registerBlock("big_flow_cedar_sapling",
            () -> new SaplingBlock(new BigFlowCedarTreeGrower(), BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING)));
    public static final RegistryObject<Block> FLOW_CEDAR_SAPLING = registerBlock("flow_cedar_sapling",
            () -> new SaplingBlock(new FlowCedarTreeGrower(), BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING)));
}
