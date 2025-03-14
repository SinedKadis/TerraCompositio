package net.sinedkadis.terracompositio.datagen;


import mekanism.api.annotations.ParametersAreNotNullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.registries.ModBlocks;
import net.sinedkadis.terracompositio.block.custom.FlowCedarLikeBlock;

import static net.sinedkadis.terracompositio.block.custom.FlowPortBlock.FACING;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, TerraCompositio.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        flowLogBlockWithItem(ModBlocks.FLOW_CEDAR_LOG);

        blockWithItem(ModBlocks.FLOW_CEDAR_LEAVES);
        flowPortBlockWithItem(ModBlocks.FLOW_PORT,
                ModBlocks.FLOW_CEDAR_LOG,
                ModBlocks.FLOW_CEDAR_LOG);
        blockWithItem(ModBlocks.CREATIVE_CFE_SOURCE);
        flowWoodBlockWithItem(ModBlocks.FLOW_CEDAR_WOOD,
                ModBlocks.FLOW_CEDAR_LOG);
        blockWithItem(ModBlocks.FLOW_CEDAR_PLANKS);
        flowLogBlockWithItem(ModBlocks.STRIPPED_FLOW_CEDAR_LOG);
        flowWoodBlockWithItem(ModBlocks.STRIPPED_FLOW_CEDAR_WOOD,
                ModBlocks.STRIPPED_FLOW_CEDAR_LOG);
        blockWithItem(ModBlocks.FLOW_CONTAINING_ORE);
        blockWithItem(ModBlocks.FLOW_CONTAINING_DEEPSLATE_ORE);
        blockWithItem(ModBlocks.FLOW_CONTAINING_RAW_ORE_BLOCK);



        stairsBlock(((StairBlock) ModBlocks.FLOW_CEDAR_STAIRS.get()),blockTexture(ModBlocks.FLOW_CEDAR_PLANKS.get()));
        slabBlock(((SlabBlock) ModBlocks.FLOW_CEDAR_SLAB.get()),blockTexture(ModBlocks.FLOW_CEDAR_PLANKS.get()),blockTexture(ModBlocks.FLOW_CEDAR_PLANKS.get()));
        buttonBlock(((ButtonBlock) ModBlocks.FLOW_CEDAR_BUTTON.get()),blockTexture(ModBlocks.FLOW_CEDAR_PLANKS.get()));
        pressurePlateBlock(((PressurePlateBlock) ModBlocks.FLOW_CEDAR_PRESSURE_PLATE.get()),blockTexture(ModBlocks.FLOW_CEDAR_PLANKS.get()));
        fenceBlock(((FenceBlock) ModBlocks.FLOW_CEDAR_FENCE.get()),blockTexture(ModBlocks.FLOW_CEDAR_PLANKS.get()));
        fenceGateBlock(((FenceGateBlock) ModBlocks.FLOW_CEDAR_FENCE_GATE.get()),blockTexture(ModBlocks.FLOW_CEDAR_PLANKS.get()));
        doorBlockWithRenderType(((DoorBlock) ModBlocks.FLOW_CEDAR_DOOR.get()),modLoc("block/flow_cedar_door_bottom"),modLoc("block/flow_cedar_door_top"),"cutout");
        trapdoorBlockWithRenderType(((TrapDoorBlock) ModBlocks.FLOW_CEDAR_TRAPDOOR.get()),modLoc("block/flow_cedar_trapdoor"),true,"cutout");

        hangingSignBlock(ModBlocks.FLOW_CEDAR_HANGING_SIGN.get(), ModBlocks.FLOW_CEDAR_WALL_HANGING_SIGN.get(),
                blockTexture(ModBlocks.FLOW_CEDAR_PLANKS.get()));

        saplingBlock(ModBlocks.FLOW_CEDAR_BIG_SAPLING);
        saplingBlock(ModBlocks.FLOW_CEDAR_SAPLING);
    }

    private void saplingBlock(RegistryObject<Block> blockRegistryObject) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(blockRegistryObject.get());
        if (key != null) {
            simpleBlock(blockRegistryObject.get(),
                    models().cross(key.getPath(), blockTexture(blockRegistryObject.get())).renderType("cutout"));
        }
    }

    public void hangingSignBlock(Block signBlock, Block wallSignBlock, ResourceLocation texture) {
        ModelFile sign = models().sign(name(signBlock), texture);
        hangingSignBlock(signBlock, wallSignBlock, sign);
    }

    public void hangingSignBlock(Block signBlock, Block wallSignBlock, ModelFile sign) {
        simpleBlock(signBlock, sign);
        simpleBlock(wallSignBlock, sign);
    }

    private String name(Block block) {
        return key(block).getPath();
    }

    private ResourceLocation key(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block);
    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }

    private void fluidBlockWithItem(RegistryObject<LiquidBlock> blockRegistryObject) {
        simpleBlock(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }

    private void flowWoodBlockWithItem(RegistryObject<Block> block, RegistryObject<Block> texture){
        flowLogBlock(((RotatedPillarBlock) block.get()),texture.get(),texture.get(),true);
        simpleBlockItem(block.get(), new ModelFile.UncheckedModelFile(TerraCompositio.MOD_ID+":block/"+ ForgeRegistries.BLOCKS.getKey(block.get()).getPath()));
    }
    private  void flowPortBlockWithItem(RegistryObject<Block> block, RegistryObject<Block> sideTexture, RegistryObject<Block> topTexture){
        ResourceLocation side = this.blockTexture(sideTexture.get());
        ResourceLocation working_side = this.blockTexture(block.get());
        ResourceLocation end = this.extend(this.blockTexture(topTexture.get()), "_top");
        ResourceLocation side_infused = this.extend(this.blockTexture(sideTexture.get()), "_infused");
        ResourceLocation working_side_infused = this.extend(this.blockTexture(block.get()), "_infused");
        ResourceLocation end_infused = this.extend(this.blockTexture(topTexture.get()), "_top_infused");
        ModelFile model = this.models().cube(this.name(block.get()),end,end, working_side, side,side,side).texture("particle",working_side);
        ModelFile model_infused = this.models().cube(this.name(block.get())+"_infused",end_infused,end_infused, working_side_infused, side_infused,side_infused,side_infused).texture("particle",working_side_infused);
        this.getVariantBuilder(block.get())
                .partialState()
                    .with(RotatedPillarBlock.AXIS, Direction.Axis.Y)
                    .with(FACING,Direction.NORTH)
                    .with(FlowCedarLikeBlock.INFUSED, true)
                        .modelForState().modelFile(model_infused)
                            .addModel()
                .partialState()
                    .with(RotatedPillarBlock.AXIS, Direction.Axis.Y)
                    .with(FACING,Direction.EAST)
                    .with(FlowCedarLikeBlock.INFUSED, true)
                        .modelForState().modelFile(model_infused).rotationY(90)
                            .addModel()
                .partialState()
                    .with(RotatedPillarBlock.AXIS, Direction.Axis.Y)
                    .with(FACING,Direction.SOUTH)
                    .with(FlowCedarLikeBlock.INFUSED, true)
                        .modelForState().modelFile(model_infused).rotationY(180)
                            .addModel()
                .partialState()
                    .with(RotatedPillarBlock.AXIS, Direction.Axis.Y)
                    .with(FACING,Direction.WEST)
                    .with(FlowCedarLikeBlock.INFUSED, true)
                        .modelForState().modelFile(model_infused).rotationY(270)
                            .addModel()
                .partialState()
                    .with(RotatedPillarBlock.AXIS, Direction.Axis.Z)
                    .with(FlowCedarLikeBlock.INFUSED, true)
                        .modelForState().modelFile(model_infused).rotationX(90)
                            .addModel()
                .partialState()
                    .with(RotatedPillarBlock.AXIS, Direction.Axis.X)
                    .with(FlowCedarLikeBlock.INFUSED, true)
                        .modelForState().modelFile(model_infused).rotationX(90).rotationY(90)
                            .addModel()
                .partialState()
                    .with(RotatedPillarBlock.AXIS, Direction.Axis.Y)
                    .with(FACING,Direction.NORTH)
                    .with(FlowCedarLikeBlock.INFUSED, false)
                        .modelForState().modelFile(model)
                            .addModel()
                .partialState()
                    .with(RotatedPillarBlock.AXIS, Direction.Axis.Y)
                    .with(FACING,Direction.EAST)
                    .with(FlowCedarLikeBlock.INFUSED, false)
                        .modelForState().modelFile(model).rotationY(90)
                            .addModel()
                .partialState()
                    .with(RotatedPillarBlock.AXIS, Direction.Axis.Y)
                    .with(FACING,Direction.SOUTH)
                    .with(FlowCedarLikeBlock.INFUSED, false)
                        .modelForState().modelFile(model).rotationY(180)
                            .addModel()
                .partialState()
                    .with(RotatedPillarBlock.AXIS, Direction.Axis.Y)
                    .with(FACING,Direction.WEST)
                    .with(FlowCedarLikeBlock.INFUSED, false)
                        .modelForState().modelFile(model).rotationY(270)
                            .addModel()
                .partialState()
                    .with(RotatedPillarBlock.AXIS, Direction.Axis.Z)
                    .with(FlowCedarLikeBlock.INFUSED, false)
                        .modelForState().modelFile(model).rotationX(90)
                            .addModel()
                .partialState()
                    .with(RotatedPillarBlock.AXIS, Direction.Axis.X)
                    .with(FlowCedarLikeBlock.INFUSED, false)
                        .modelForState().modelFile(model).rotationX(90).rotationY(90)
                            .addModel();
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block.get());
        if (key != null) {
            simpleBlockItem(block.get(), new ModelFile.UncheckedModelFile(TerraCompositio.MOD_ID+":block/"+ key.getPath()));
        }
    }

    private void flowLogBlockWithItem(RegistryObject<Block> block){
        flowLogBlock(block.get(),block.get(),block.get(),true);
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block.get());
        if (key != null) {
            simpleBlockItem(block.get(), new ModelFile.UncheckedModelFile(TerraCompositio.MOD_ID+":block/"+ key.getPath()));
        }
    }

    public void flowLogBlock(Block block,Block sideTexture,Block topTexture,boolean infused) {
        ResourceLocation side = this.blockTexture(sideTexture);
        ResourceLocation end = this.extend(this.blockTexture(topTexture), "_top");

        ModelFile vertical = this.models().cubeColumn(this.name(block), side, end);
        ModelFile horizontal = this.models().cubeColumnHorizontal(this.name(block) + "_horizontal", side, end);
        VariantBlockStateBuilder builder = this.getVariantBuilder(block)
                .partialState()
                    .with(RotatedPillarBlock.AXIS, Direction.Axis.Y)
                    .with(FlowCedarLikeBlock.INFUSED, false)
                        .modelForState().modelFile(vertical)
                            .addModel()
                .partialState()
                    .with(RotatedPillarBlock.AXIS, Direction.Axis.Z)
                    .with(FlowCedarLikeBlock.INFUSED, false)
                        .modelForState().modelFile(horizontal).rotationX(90)
                            .addModel()
                .partialState()
                    .with(RotatedPillarBlock.AXIS, Direction.Axis.X)
                    .with(FlowCedarLikeBlock.INFUSED, false)
                        .modelForState().modelFile(horizontal).rotationX(90).rotationY(90)
                            .addModel();
        ResourceLocation side_infused;
        ResourceLocation end_infused;
        if (infused) {
            side_infused = this.extend(this.blockTexture(sideTexture), "_infused");
            end_infused = this.extend(this.blockTexture(topTexture), "_top_infused");

        } else {
            side_infused = this.blockTexture(sideTexture);
            end_infused = this.blockTexture(topTexture);
        }
        ModelFile vertical_infused = this.models().cubeColumn(this.name(block) + "_infused", side_infused, end_infused);
        ModelFile horizontal_infused = this.models().cubeColumnHorizontal(this.name(block) + "_horizontal" + "_infused", side_infused, end_infused);
            builder.partialState()
                        .with(RotatedPillarBlock.AXIS, Direction.Axis.Y)
                        .with(FlowCedarLikeBlock.INFUSED, true)
                            .modelForState().modelFile(vertical_infused)
                                .addModel()
                    .partialState()
                        .with(RotatedPillarBlock.AXIS, Direction.Axis.Z)
                        .with(FlowCedarLikeBlock.INFUSED, true)
                            .modelForState().modelFile(horizontal_infused).rotationX(90)
                            .addModel()
                    .partialState()
                        .with(RotatedPillarBlock.AXIS, Direction.Axis.X)
                        .with(FlowCedarLikeBlock.INFUSED, true)
                            .modelForState().modelFile(horizontal_infused).rotationX(90).rotationY(90)
                                .addModel();

    }
    private ResourceLocation extend(ResourceLocation rl, String suffix) {
        String var10002 = rl.getNamespace();
        String var10003 = rl.getPath();
        return new ResourceLocation(var10002, var10003 + suffix);
    }

}
