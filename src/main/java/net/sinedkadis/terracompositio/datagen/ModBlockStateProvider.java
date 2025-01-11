package net.sinedkadis.terracompositio.datagen;


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

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, TerraCompositio.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        logBlockWithItem(ModBlocks.FLOWING_FLOW_CEDAR_LOG);
        blockWithItem(ModBlocks.FLOWING_FLOW_PORT);
        blockWithItem(ModBlocks.FLOW_CEDAR_LEAVES);
        blockWithItem(ModBlocks.FLOW_PORT);
        blockWithItem(ModBlocks.CREATIVE_CFE_SOURCE);
        woodBlockWithItem(ModBlocks.FLOWING_FLOW_CEDAR_WOOD,ModBlocks.FLOWING_FLOW_CEDAR_LOG);
        logBlockWithItem(ModBlocks.FLOW_CEDAR_LOG);
        blockWithItem(ModBlocks.FLOW_CEDAR_PLANKS);
        woodBlockWithItem(ModBlocks.FLOW_CEDAR_WOOD,ModBlocks.FLOW_CEDAR_LOG);
        logBlockWithItem(ModBlocks.STRIPPED_FLOW_CEDAR_LOG);
        woodBlockWithItem(ModBlocks.STRIPPED_FLOW_CEDAR_WOOD,ModBlocks.STRIPPED_FLOW_CEDAR_LOG);



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

    private  void woodBlockWithItem(RegistryObject<Block> block, RegistryObject<Block> texture){
        axisBlock(((RotatedPillarBlock) block.get()),blockTexture(texture.get()),blockTexture(texture.get()));
        simpleBlockItem(block.get(), new ModelFile.UncheckedModelFile(TerraCompositio.MOD_ID+":block/"+ ForgeRegistries.BLOCKS.getKey(block.get()).getPath()));
    }
    private void logBlockWithItem(RegistryObject<Block> block){
        logBlock((RotatedPillarBlock) block.get());
        simpleBlockItem(block.get(), new ModelFile.UncheckedModelFile(TerraCompositio.MOD_ID+":block/"+ ForgeRegistries.BLOCKS.getKey(block.get()).getPath()));
    }

}
