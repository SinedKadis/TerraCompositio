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
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.registries.TCFluids;
import net.sinedkadis.terracompositio.registries.TCItems;

import java.util.LinkedHashMap;
import java.util.Objects;

public class TCItemModelProvider extends ItemModelProvider {
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
    public TCItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, TerraCompositio.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        trapdoorItem(TCBlocks.FLOW_CEDAR_TRAPDOOR);
        fenceItem(TCBlocks.FLOW_CEDAR_FENCE, TCBlocks.FLOW_CEDAR_PLANKS);
        simpleBlockItem(TCBlocks.FLOW_CEDAR_DOOR);
        buttonItem(TCBlocks.FLOW_CEDAR_BUTTON, TCBlocks.FLOW_CEDAR_PLANKS);
        evenSimplerBlockItem(TCBlocks.FLOW_CEDAR_STAIRS);
        evenSimplerBlockItem(TCBlocks.FLOW_CEDAR_SLAB);
        evenSimplerBlockItem(TCBlocks.FLOW_CEDAR_PRESSURE_PLATE);
        evenSimplerBlockItem(TCBlocks.FLOW_CEDAR_FENCE_GATE);
        simpleItem(TCFluids.FLOW_FLUID.bucket);
        simpleItem(TCItems.FLOW_BOTTLE);
        simpleItem(TCFluids.BIRCH_JUICE_FLUID.bucket);

        trimmedArmorItem(TCItems.FLOW_CEDAR_HELMET);
        trimmedArmorItem(TCItems.FLOW_CEDAR_CHESTPLATE);
        trimmedArmorItem(TCItems.FLOW_CEDAR_LEGGINGS);
        trimmedArmorItem(TCItems.FLOW_CEDAR_BOOTS);

        trimmedArmorItem(TCItems.FLOWING_FLOW_CEDAR_HELMET);
        trimmedArmorItem(TCItems.FLOWING_FLOW_CEDAR_CHESTPLATE);
        trimmedArmorItem(TCItems.FLOWING_FLOW_CEDAR_LEGGINGS);
        trimmedArmorItem(TCItems.FLOWING_FLOW_CEDAR_BOOTS);

        simpleItem(TCItems.FLOW_CEDAR_SIGN);
        simpleItem(TCItems.FLOW_CEDAR_HANGING_SIGN);

        simpleItem(TCItems.FLOW_CEDAR_BOAT);
        simpleItem(TCItems.FLOW_CEDAR_CHEST_BOAT);

        simpleItem(TCItems.INFUSED_IRON_INGOT);
        simpleItem(TCItems.INFUSED_IRON_NUGGET);
        simpleItem(TCItems.COPPER_NUGGET);
        simpleItem(TCItems.FLOW_INFUSER_KIT);
        simpleItem(TCItems.RAW_TECHNETIUM);
        simpleItem(TCItems.LOW_ENRICHED_TECHNETIUM);
        simpleItem(TCItems.MEDIUM_ENRICHED_TECHNETIUM);
        simpleItem(TCItems.HIGH_ENRICHED_TECHNETIUM);
        simpleItem(TCItems.INFUSED_IRON_ROD);
        simpleItem(TCItems.GOLD_ROD);
        simpleItem(TCItems.INPUT_BUS);
        simpleItem(TCItems.OUTPUT_BUS);
        simpleItem(TCItems.COPPER_ROD);

        simpleItem(TCItems.TECHNETIUM_INGOT);
        simpleItem(TCItems.TECHNETIUM_ROD);
        simpleItem(TCItems.TECHNETIUM_NUGGET);
        simpleItem(TCItems.WRENCH_TAG_HOLDER);

        simpleItem(TCItems.CFE_BALL);
        simpleItem(TCItems.INFUSED_FERTILIZER);
        simpleItem(TCItems.TECHNETIUM_CHESTPLATE);
        simpleItem(TCItems.TECHNETIUM_LEGGINGS);
        simpleItem(TCItems.TECHNETIUM_BOOTS);


        saplingItem(TCBlocks.FLOW_CEDAR_SAPLING);

        withExistingParent(Objects.requireNonNull(TCItems.FLOW_CEDAR_ENT_SPAWN_EGG.getId()).getPath(), mcLoc("item/template_spawn_egg"));

        if (ModList.get().isLoaded("create")) {
            TerraCompositio.createCompat.getDataGen().registerItemModels();
        }
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
                ResourceLocation armorItemResLoc = ResourceLocation.tryBuild(MOD_ID, armorItemPath);
                ResourceLocation trimResLoc = ResourceLocation.tryParse(trimPath); // minecraft namespace
                ResourceLocation trimNameResLoc = ResourceLocation.tryBuild(MOD_ID, currentTrimName);

                // This is used for making the ExistingFileHelper acknowledge that this texture exist, so this will
                // avoid an IllegalArgumentException
                assert trimResLoc != null;
                existingFileHelper.trackGenerated(trimResLoc, PackType.CLIENT_RESOURCES, ".png", "textures");

                // Trimmed armorItem files
                getBuilder(currentTrimName)
                        .parent(new ModelFile.UncheckedModelFile("item/generated"))
                        .texture("layer0", armorItemResLoc)
                        .texture("layer1", trimResLoc);

                // Non-trimmed armorItem file (normal variant)
                this.withExistingParent(Objects.requireNonNull(itemRegistryObject.getId()).getPath(),
                                mcLoc("item/generated"))
                        .override()
                        .model(new ModelFile.UncheckedModelFile(trimNameResLoc))
                        .predicate(mcLoc("trim_type"), trimValue).end()
                        .texture("layer0",
                                ResourceLocation.tryBuild(MOD_ID,
                                        "item/" + itemRegistryObject.getId().getPath()));
            });
        }
    }


    private void simpleItem(RegistryObject<Item> item) {
        withExistingParent(Objects.requireNonNull(item.getId()).getPath(),
                ResourceLocation.tryParse("item/generated")).texture("layer0",
                ResourceLocation.tryBuild(TerraCompositio.MOD_ID, "item/" + item.getId().getPath()));
    }

    private void saplingItem(RegistryObject<Block> item) {
        withExistingParent(Objects.requireNonNull(item.getId()).getPath(),
                ResourceLocation.tryParse("item/generated")).texture("layer0",
                ResourceLocation.tryBuild(TerraCompositio.MOD_ID, "block/" + item.getId().getPath()));
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
                    .texture("texture",  ResourceLocation.tryBuild(TerraCompositio.MOD_ID, "block/" + Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(baseBlock.get())).getPath()));
        }
    }

    public void buttonItem(RegistryObject<Block> block, RegistryObject<Block> baseBlock) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block.get());
        if (key != null) {
            ResourceLocation key1 = ForgeRegistries.BLOCKS.getKey(baseBlock.get());
            if (key1 != null) {
                this.withExistingParent(key.getPath(), mcLoc("block/button_inventory"))
                        .texture("texture",  ResourceLocation.tryBuild(TerraCompositio.MOD_ID, "block/" + key1.getPath()));
            }
        }
    }

    public void wallItem(RegistryObject<Block> block, RegistryObject<Block> baseBlock) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block.get());
        if (key != null) {
            ResourceLocation key1 = ForgeRegistries.BLOCKS.getKey(baseBlock.get());
            if (key1 != null) {
                this.withExistingParent(key.getPath(), mcLoc("block/wall_inventory"))
                        .texture("wall",  ResourceLocation.tryBuild(TerraCompositio.MOD_ID, "block/" + key1.getPath()));
            }
        }
    }

    private void simpleBlockItem(RegistryObject<Block> item) {
        withExistingParent(Objects.requireNonNull(item.getId()).getPath(),
                ResourceLocation.tryParse("item/generated")).texture("layer0",
                ResourceLocation.tryBuild(TerraCompositio.MOD_ID, "item/" + item.getId().getPath()));
    }

}
