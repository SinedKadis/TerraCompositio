package net.sinedkadis.terracompositio.datagen;


import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.ModBlocks;
import net.sinedkadis.terracompositio.block.custom.FlowCedarLikeBlock;
import net.sinedkadis.terracompositio.block.custom.FlowPortBlock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, TerraCompositio.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        flowLogBlockWithItem(ModBlocks.FLOW_CEDAR_LOG);
        blockWithItem(ModBlocks.FLOW_CEDAR_LEAVES);
        flowPortBlockWithItem(ModBlocks.FLOW_PORT);
        blockWithItem(ModBlocks.CREATIVE_CFE_SOURCE);
        flowWoodBlockWithItem(ModBlocks.FLOW_CEDAR_WOOD,ModBlocks.FLOW_CEDAR_LOG);
        blockWithItem(ModBlocks.FLOW_CEDAR_PLANKS);
        flowLogBlockWithItem(ModBlocks.STRIPPED_FLOW_CEDAR_LOG);
        flowWoodBlockWithItem(ModBlocks.STRIPPED_FLOW_CEDAR_WOOD,ModBlocks.STRIPPED_FLOW_CEDAR_LOG);
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

    private  void flowWoodBlockWithItem(RegistryObject<Block> block, RegistryObject<Block> texture){
        flowLogBlock(((RotatedPillarBlock) block.get()),texture.get());
        simpleBlockItem(block.get(), new ModelFile.UncheckedModelFile(TerraCompositio.MOD_ID+":block/"+ ForgeRegistries.BLOCKS.getKey(block.get()).getPath()));
    }

    private void flowLogBlockWithItem(RegistryObject<Block> block){
        flowLogBlock((RotatedPillarBlock) block.get(),block.get());
        simpleBlockItem(block.get(), new ModelFile.UncheckedModelFile(TerraCompositio.MOD_ID+":block/"+ ForgeRegistries.BLOCKS.getKey(block.get()).getPath()));
    }

    public void flowPortBlockWithItem(RegistryObject<Block> blockRegistryObject){
        FlowPortBlock block = (FlowPortBlock) blockRegistryObject.get();
        this.getVariantBuilder(block)
                .partialState()
                    .with(FlowPortBlock.INFUSED,true)
                        .modelForState().modelFile(this.models()
                        .cubeAll(this.name(block),this.extend(this.blockTexture(block),"_infused")))
                            .addModel()
                .partialState()
                    .with(FlowPortBlock.INFUSED,false)
                        .modelForState().modelFile(this.models()
                        .cubeAll(this.name(block),this.blockTexture(block)))
                            .addModel();
        this.simpleBlockItem(blockRegistryObject.get(),cubeAll(blockRegistryObject.get()));
    }

    public void flowLogBlock(RotatedPillarBlock block,Block texture) {
        ResourceLocation side = this.blockTexture(texture);
        ResourceLocation end = this.extend(this.blockTexture(texture), "_top");
        ResourceLocation side_infused = this.extend(this.blockTexture(texture), "_infused");
        ResourceLocation end_infused = this.extend(this.blockTexture(texture), "_top_infused");
        ModelFile vertical = this.models().cubeColumn(this.name(block), side, end);
        ModelFile horizontal = this.models().cubeColumnHorizontal(this.name(block) + "_horizontal", side, end);
        ModelFile vertical_infused = this.models().cubeColumn(this.name(block)+"_infused", side_infused, end_infused);
        ModelFile horizontal_infused = this.models().cubeColumnHorizontal(this.name(block) + "_horizontal" + "_infused", side_infused, end_infused);
        this.getVariantBuilder(block)
                .partialState()
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
                            .addModel()
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
    }
    private ResourceLocation extend(ResourceLocation rl, String suffix) {
        String var10002 = rl.getNamespace();
        String var10003 = rl.getPath();
        return new ResourceLocation(var10002, var10003 + suffix);
    }

}
