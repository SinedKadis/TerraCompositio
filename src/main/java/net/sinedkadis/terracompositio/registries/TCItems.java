package net.sinedkadis.terracompositio.registries;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.entity.custom.TCBoatEntity;
import net.sinedkadis.terracompositio.item.custom.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TCItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TerraCompositio.MOD_ID);

    public static final RegistryObject<Item> FLOW_BOTTLE = ITEMS.register("flow_bottle",
            () -> new FlowBottleItem(new Item.Properties().stacksTo(16).food(TCFoods.FLOW)));

    public static final RegistryObject<Item> FLOW_CEDAR_HELMET = ITEMS.register("flow_cedar_helmet",
            () -> new TCArmorItem(TCArmorMaterials.FLOW_CEDAR, ArmorItem.Type.HELMET,new Item.Properties()));
    public static final RegistryObject<Item> FLOW_CEDAR_CHESTPLATE = ITEMS.register("flow_cedar_chestplate",
            () -> new TCArmorItem(TCArmorMaterials.FLOW_CEDAR, ArmorItem.Type.CHESTPLATE,new Item.Properties()));
    public static final RegistryObject<Item> FLOW_CEDAR_LEGGINGS = ITEMS.register("flow_cedar_leggings",
            () -> new TCArmorItem(TCArmorMaterials.FLOW_CEDAR, ArmorItem.Type.LEGGINGS,new Item.Properties()));
    public static final RegistryObject<Item> FLOW_CEDAR_BOOTS = ITEMS.register("flow_cedar_boots",
            () -> new TCArmorItem(TCArmorMaterials.FLOW_CEDAR, ArmorItem.Type.BOOTS,new Item.Properties()));
    public static final RegistryObject<Item> FLOWING_FLOW_CEDAR_HELMET = ITEMS.register("flowing_flow_cedar_helmet",
            () -> new FlowArmorItem(TCArmorMaterials.FLOWING_FLOW_CEDAR, ArmorItem.Type.HELMET,new Item.Properties()));
    public static final RegistryObject<Item> FLOWING_FLOW_CEDAR_CHESTPLATE = ITEMS.register("flowing_flow_cedar_chestplate",
            () -> new FlowArmorItem(TCArmorMaterials.FLOWING_FLOW_CEDAR, ArmorItem.Type.CHESTPLATE,new Item.Properties()));
    public static final RegistryObject<Item> FLOWING_FLOW_CEDAR_LEGGINGS = ITEMS.register("flowing_flow_cedar_leggings",
            () -> new FlowArmorItem(TCArmorMaterials.FLOWING_FLOW_CEDAR, ArmorItem.Type.LEGGINGS,new Item.Properties()));
    public static final RegistryObject<Item> FLOWING_FLOW_CEDAR_BOOTS = ITEMS.register("flowing_flow_cedar_boots",
            () -> new FlowArmorItem(TCArmorMaterials.FLOWING_FLOW_CEDAR, ArmorItem.Type.BOOTS,new Item.Properties()));

    public static final RegistryObject<Item> FLOW_CEDAR_SIGN = ITEMS.register("flow_cedar_sign",
            () -> new SignItem(new Item.Properties().stacksTo(16), TCBlocks.FLOW_CEDAR_SIGN.get(), TCBlocks.FLOW_CEDAR_WALL_SIGN.get()));
    public static final RegistryObject<Item> FLOW_CEDAR_HANGING_SIGN = ITEMS.register("flow_cedar_hanging_sign",
            () -> new HangingSignItem(TCBlocks.FLOW_CEDAR_HANGING_SIGN.get(), TCBlocks.FLOW_CEDAR_WALL_HANGING_SIGN.get(),new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> FLOW_CEDAR_BOAT = ITEMS.register("flow_cedar_boat",
            () -> new TCBoatItem(false, TCBoatEntity.Type.FLOW_CEDAR, new Item.Properties()));
    public static final RegistryObject<Item> FLOW_CEDAR_CHEST_BOAT = ITEMS.register("flow_cedar_chest_boat",
            () -> new TCBoatItem(true, TCBoatEntity.Type.FLOW_CEDAR, new Item.Properties()));

    public static final RegistryObject<Item> INFUSED_IRON_INGOT = ITEMS.register("infused_iron_ingot",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> COPPER_NUGGET = ITEMS.register("copper_nugget",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FLOW_INFUSER_KIT = ITEMS.register("flow_infuser_kit",
            () -> new Item(new Item.Properties()){
                @Override
                @ParametersAreNotNullByDefault
                public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
                    super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
                    pTooltipComponents.add(Component.translatable("item.terracompositio.flow_infuser_kit.tooltip").withStyle(ChatFormatting.GRAY));
                }
            });
    public static final RegistryObject<Item> RAW_TECHNETIUM = ITEMS.register("technetium_raw_ore",
            () -> new UnstableTechnetiumItem(new Item.Properties(),0));
    public static final RegistryObject<Item> LOW_ENRICHED_TECHNETIUM = ITEMS.register("low_enriched_technetium",
            () -> new UnstableTechnetiumItem(new Item.Properties(),1));
    public static final RegistryObject<Item> MEDIUM_ENRICHED_TECHNETIUM = ITEMS.register("medium_enriched_technetium",
            () -> new UnstableTechnetiumItem(new Item.Properties(),2));
    public static final RegistryObject<Item> HIGH_ENRICHED_TECHNETIUM = ITEMS.register("high_enriched_technetium",
            () -> new UnstableTechnetiumItem(new Item.Properties(),3));
    public static final RegistryObject<Item> WRENCH_AXE = ITEMS.register("flow_rotating_axe",
            () -> new WrenchAxeItem(Tiers.IRON, 6.0F, -3.1F, new Item.Properties().durability(330)));
    public static final RegistryObject<Item> WRENCH_TAG_HOLDER = ITEMS.register("wrench_tag_holder",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GOLD_ROD = ITEMS.register("gold_rod",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> INFUSED_IRON_ROD = ITEMS.register("infused_iron_rod",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> COPPER_ROD = ITEMS.register("copper_rod",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> INPUT_BUS = ITEMS.register("lapis_input_bus",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> OUTPUT_BUS = ITEMS.register("copper_output_bus",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SHIELDED_BUNDLE = ITEMS.register("shielded_bundle",
            () -> new ShieldedBundleItem(new Item.Properties()));

    public static final RegistryObject<Item> TECHNETIUM_INGOT = ITEMS.register("technetium_ingot",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CREATION_FLOW_JOURNAL = ITEMS.register("creation_flow_journal",
            () -> new CreationFlowJournalItem(new Item.Properties()));

    public static final RegistryObject<Item> FLOW_CEDAR_ENT_SPAWN_EGG = ITEMS.register("flow_cedar_ent_spawn_egg",
            () -> new ForgeSpawnEggItem(TCEntities.FLOW_CEDAR_ENT, 0x352001, 0x015161, new Item.Properties()));

    public static final RegistryObject<Item> FLUID_APPLIER = ITEMS.register("fluid_applier",
            () -> new FluidApplierItem(new Item.Properties()));

    public static final RegistryObject<Item> TECHNETIUM_CROWN = ITEMS.register("technetium_crown",
            () -> new TechnetiumArmorItem(ArmorItem.Type.HELMET,new Item.Properties()));
    public static final RegistryObject<Item> TECHNETIUM_CLOAK = ITEMS.register("technetium_cloak",
            () -> new TechnetiumArmorItem(ArmorItem.Type.CHESTPLATE,new Item.Properties()));
    public static final RegistryObject<Item> TECHNETIUM_ACCUMULATORS = ITEMS.register("technetium_accumulators",
            () -> new TechnetiumArmorItem(ArmorItem.Type.LEGGINGS,new Item.Properties()));
    public static final RegistryObject<Item> TECHNETIUM_BOOTS = ITEMS.register("technetium_boots",
            () -> new TechnetiumArmorItem(ArmorItem.Type.BOOTS,new Item.Properties()));

    public static final RegistryObject<Item> CFE_BALL = ITEMS.register("cfe_ball",
            () -> new CFEBallItem(new Item.Properties()));


    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }

}
