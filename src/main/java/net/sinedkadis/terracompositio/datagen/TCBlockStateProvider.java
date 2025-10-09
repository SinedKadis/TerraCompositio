package net.sinedkadis.terracompositio.datagen;


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
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.block.custom.FlowCedarLikeBlock;

import java.util.Objects;


public class TCBlockStateProvider extends BlockStateProvider {
    public TCBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, TerraCompositio.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        flowLogBlockWithItem(TCBlocks.FLOW_CEDAR_LOG);

        blockWithItem(TCBlocks.FLOW_CEDAR_LEAVES);
        blockWithItem(TCBlocks.CREATIVE_CFE_SOURCE);
        blockWithItem(TCBlocks.CFE_TRASH_CAN);
        flowWoodBlockWithItem(TCBlocks.FLOW_CEDAR_WOOD,
                TCBlocks.FLOW_CEDAR_LOG);
        blockWithItem(TCBlocks.FLOW_CEDAR_PLANKS);
        flowLogBlockWithItem(TCBlocks.STRIPPED_FLOW_CEDAR_LOG);
        flowWoodBlockWithItem(TCBlocks.STRIPPED_FLOW_CEDAR_WOOD,
                TCBlocks.STRIPPED_FLOW_CEDAR_LOG);
        blockWithItem(TCBlocks.TECHNETIUM_ORE);
        blockWithItem(TCBlocks.TECHNETIUM_DEEPSLATE_ORE);
        blockWithItem(TCBlocks.TECHNETIUM_RAW_ORE_BLOCK);
        blockWithItem(TCBlocks.TECHNETIUM_BLOCK);
        blockSideTopBottom(TCBlocks.TECHNETIUM_BOARD);


        stairsBlock(((StairBlock) TCBlocks.FLOW_CEDAR_STAIRS.get()),blockTexture(TCBlocks.FLOW_CEDAR_PLANKS.get()));
        slabBlock(((SlabBlock) TCBlocks.FLOW_CEDAR_SLAB.get()),blockTexture(TCBlocks.FLOW_CEDAR_PLANKS.get()),blockTexture(TCBlocks.FLOW_CEDAR_PLANKS.get()));
        buttonBlock(((ButtonBlock) TCBlocks.FLOW_CEDAR_BUTTON.get()),blockTexture(TCBlocks.FLOW_CEDAR_PLANKS.get()));
        pressurePlateBlock(((PressurePlateBlock) TCBlocks.FLOW_CEDAR_PRESSURE_PLATE.get()),blockTexture(TCBlocks.FLOW_CEDAR_PLANKS.get()));
        fenceBlock(((FenceBlock) TCBlocks.FLOW_CEDAR_FENCE.get()),blockTexture(TCBlocks.FLOW_CEDAR_PLANKS.get()));
        fenceGateBlock(((FenceGateBlock) TCBlocks.FLOW_CEDAR_FENCE_GATE.get()),blockTexture(TCBlocks.FLOW_CEDAR_PLANKS.get()));
        doorBlockWithRenderType(((DoorBlock) TCBlocks.FLOW_CEDAR_DOOR.get()),modLoc("block/flow_cedar_door_bottom"),modLoc("block/flow_cedar_door_top"),"cutout");
        trapdoorBlockWithRenderType(((TrapDoorBlock) TCBlocks.FLOW_CEDAR_TRAPDOOR.get()),modLoc("block/flow_cedar_trapdoor"),true,"cutout");

        hangingSignBlock(TCBlocks.FLOW_CEDAR_HANGING_SIGN.get(), TCBlocks.FLOW_CEDAR_WALL_HANGING_SIGN.get(),
                blockTexture(TCBlocks.FLOW_CEDAR_PLANKS.get()));


        saplingBlock(TCBlocks.FLOW_CEDAR_SAPLING);
    }

    private void blockSideTopBottom(RegistryObject<Block> blockRegistryObject) {
        Block block = blockRegistryObject.get();
        simpleBlockWithItem(block,
                models().cubeBottomTop(name(block),
                        Objects.requireNonNull(TerraCompositio.modLoc("block/"+
                                Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(
                                        block)).getPath()+"_side")),
                        Objects.requireNonNull(TerraCompositio.modLoc("block/"+
                                Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(
                                        block)).getPath()+"_bottom")),
                        Objects.requireNonNull(TerraCompositio.modLoc("block/"+
                                Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(
                                        block)).getPath()+"_top")))
                        .renderType("translucent"));
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

    private void flowWoodBlockWithItem(RegistryObject<Block> block, RegistryObject<Block> texture){
        flowLogBlock(block.get(),texture.get(),texture.get(),true);
        simpleBlockItem(block.get(), new ModelFile.UncheckedModelFile(TerraCompositio.MOD_ID+":block/"+ Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block.get())).getPath()));
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
        return ResourceLocation.tryBuild(var10002, var10003 + suffix);
    }

}
