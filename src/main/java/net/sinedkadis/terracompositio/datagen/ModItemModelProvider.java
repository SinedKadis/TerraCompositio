package net.sinedkadis.terracompositio.datagen;


import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.registries.ModBlocks;
import net.sinedkadis.terracompositio.registries.ModFluids;
import net.sinedkadis.terracompositio.registries.ModItems;

import java.util.LinkedHashMap;

public class ModItemModelProvider extends ItemModelProvider {
    private static final LinkedHashMap<ResourceKey<TrimMaterial>, Float> trimMaterials = new LinkedHashMap<>();
    static {
        trimMaterials.put(TrimMaterials.QUARTZ, 0.1F);
        trimMaterials.put(TrimMaterials.IRON, 0.2F);
        trimMaterials.put(TrimMaterials.NETHERITE, 0.3F);
        trimMaterials.put(TrimMaterials.REDSTONE, 0.4F);
        trimMaterials.put(TrimMaterials.COPPER, 0.5F);
        trimMaterials.put(TrimMaterials.GOLD, 0.6F);
        trimMaterials.put(TrimMaterials.EMERALD, 0.7F);
        trimMaterials.put(TrimMaterials.DIAMOND, 0.8F);
        trimMaterials.put(TrimMaterials.LAPIS, 0.9F);
        trimMaterials.put(TrimMaterials.AMETHYST, 1.0F);
    }
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, TerraCompositio.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleItem(ModItems.PEBBLE);
        trapdoorItem(ModBlocks.FLOW_CEDAR_TRAPDOOR);
        fenceItem(ModBlocks.FLOW_CEDAR_FENCE,ModBlocks.FLOW_CEDAR_PLANKS);
        simpleBlockItem(ModBlocks.FLOW_CEDAR_DOOR);
        buttonItem(ModBlocks.FLOW_CEDAR_BUTTON,ModBlocks.FLOW_CEDAR_PLANKS);
        evenSimplerBlockItem(ModBlocks.FLOW_CEDAR_STAIRS);
        evenSimplerBlockItem(ModBlocks.FLOW_CEDAR_SLAB);
        evenSimplerBlockItem(ModBlocks.FLOW_CEDAR_PRESSURE_PLATE);
        evenSimplerBlockItem(ModBlocks.FLOW_CEDAR_FENCE_GATE);
        simpleItem(ModFluids.FLOW_FLUID.bucket);
        simpleItem(ModItems.FLOW_BOTTLE);
        simpleItem(ModFluids.BIRCH_JUICE_FLUID.bucket);

        trimmedArmorItem(ModItems.FLOW_CEDAR_HELMET);
        trimmedArmorItem(ModItems.FLOW_CEDAR_CHESTPLATE);
        trimmedArmorItem(ModItems.FLOW_CEDAR_LEGGINGS);
        trimmedArmorItem(ModItems.FLOW_CEDAR_BOOTS);

        trimmedArmorItem(ModItems.FLOWING_FLOW_CEDAR_HELMET);
        trimmedArmorItem(ModItems.FLOWING_FLOW_CEDAR_CHESTPLATE);
        trimmedArmorItem(ModItems.FLOWING_FLOW_CEDAR_LEGGINGS);
        trimmedArmorItem(ModItems.FLOWING_FLOW_CEDAR_BOOTS);

        simpleItem(ModItems.FLOW_CEDAR_SIGN);
        simpleItem(ModItems.FLOW_CEDAR_HANGING_SIGN);

        simpleItem(ModItems.FLOW_CEDAR_BOAT);
        simpleItem(ModItems.FLOW_CEDAR_CHEST_BOAT);

        simpleItem(ModItems.INFUSED_IRON_INGOT);
        simpleItem(ModItems.COPPER_NUGGET);
        simpleItem(ModItems.FLOW_INFUSER_KIT);
        simpleItem(ModItems.FLOW_CONTAINING_RAW_ORE);
        simpleItem(ModItems.LOW_ENRICHED_FCO);
        simpleItem(ModItems.MEDIUM_ENRICHED_FCO);
        simpleItem(ModItems.HIGH_ENRICHED_FCO);
        simpleItem(ModItems.INFUSED_IRON_ROD);
        simpleItem(ModItems.GOLD_ROD);
        simpleItem(ModItems.INPUT_BUS);
        simpleItem(ModItems.OUTPUT_BUS);
        simpleItem(ModItems.COPPER_ROD);

        saplingItem(ModBlocks.FLOW_CEDAR_BIG_SAPLING);
        saplingItem(ModBlocks.FLOW_CEDAR_SAPLING);
    }

    // Shoutout to El_Redstoniano for making this
    private void trimmedArmorItem(RegistryObject<Item> itemRegistryObject) {
        final String MOD_ID = TerraCompositio.MOD_ID; // Change this to your mod id

        if(itemRegistryObject.get() instanceof ArmorItem armorItem) {
            trimMaterials.forEach((trimMaterial, value) -> {

                float trimValue = value;

                String armorType = switch (armorItem.getEquipmentSlot()) {
                    case HEAD -> "helmet";
                    case CHEST -> "chestplate";
                    case LEGS -> "leggings";
                    case FEET -> "boots";
                    default -> "";
                };

                String armorItemPath = "item/" + armorItem;
                String trimPath = "trims/items/" + armorType + "_trim_" + trimMaterial.location().getPath();
                String currentTrimName = armorItemPath + "_" + trimMaterial.location().getPath() + "_trim";
                ResourceLocation armorItemResLoc = new ResourceLocation(MOD_ID, armorItemPath);
                ResourceLocation trimResLoc = new ResourceLocation(trimPath); // minecraft namespace
                ResourceLocation trimNameResLoc = new ResourceLocation(MOD_ID, currentTrimName);

                // This is used for making the ExistingFileHelper acknowledge that this texture exist, so this will
                // avoid an IllegalArgumentException
                existingFileHelper.trackGenerated(trimResLoc, PackType.CLIENT_RESOURCES, ".png", "textures");

                // Trimmed armorItem files
                getBuilder(currentTrimName)
                        .parent(new ModelFile.UncheckedModelFile("item/generated"))
                        .texture("layer0", armorItemResLoc)
                        .texture("layer1", trimResLoc);

                // Non-trimmed armorItem file (normal variant)
                this.withExistingParent(itemRegistryObject.getId().getPath(),
                                mcLoc("item/generated"))
                        .override()
                        .model(new ModelFile.UncheckedModelFile(trimNameResLoc))
                        .predicate(mcLoc("trim_type"), trimValue).end()
                        .texture("layer0",
                                new ResourceLocation(MOD_ID,
                                        "item/" + itemRegistryObject.getId().getPath()));
            });
        }
    }


    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(TerraCompositio.MOD_ID,"item/" + item.getId().getPath()));
    }

    private ItemModelBuilder saplingItem(RegistryObject<Block> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(TerraCompositio.MOD_ID,"block/" + item.getId().getPath()));
    }

    public void evenSimplerBlockItem(RegistryObject<Block> block) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block.get());
        if (key != null) {
            this.withExistingParent(TerraCompositio.MOD_ID + ":" + key.getPath(),
                    modLoc("block/" + key.getPath()));
        }
    }

    public void trapdoorItem(RegistryObject<Block> block) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block.get());
        if (key != null) {
            this.withExistingParent(key.getPath(),
                    modLoc("block/" + key.getPath() + "_bottom"));
        }
    }

    public void fenceItem(RegistryObject<Block> block, RegistryObject<Block> baseBlock) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block.get());
        if (key != null) {
            this.withExistingParent(key.getPath(), mcLoc("block/fence_inventory"))
                    .texture("texture",  new ResourceLocation(TerraCompositio.MOD_ID, "block/" + ForgeRegistries.BLOCKS.getKey(baseBlock.get()).getPath()));
        }
    }

    public void buttonItem(RegistryObject<Block> block, RegistryObject<Block> baseBlock) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block.get());
        if (key != null) {
            ResourceLocation key1 = ForgeRegistries.BLOCKS.getKey(baseBlock.get());
            if (key1 != null) {
                this.withExistingParent(key.getPath(), mcLoc("block/button_inventory"))
                        .texture("texture",  new ResourceLocation(TerraCompositio.MOD_ID, "block/" + key1.getPath()));
            }
        }
    }

    public void wallItem(RegistryObject<Block> block, RegistryObject<Block> baseBlock) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block.get());
        if (key != null) {
            ResourceLocation key1 = ForgeRegistries.BLOCKS.getKey(baseBlock.get());
            if (key1 != null) {
                this.withExistingParent(key.getPath(), mcLoc("block/wall_inventory"))
                        .texture("wall",  new ResourceLocation(TerraCompositio.MOD_ID, "block/" + key1.getPath()));
            }
        }
    }

    private ItemModelBuilder simpleBlockItem(RegistryObject<Block> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(TerraCompositio.MOD_ID,"item/" + item.getId().getPath()));
    }

}
