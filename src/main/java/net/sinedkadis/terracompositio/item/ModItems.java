package net.sinedkadis.terracompositio.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.ModBlocks;
import net.sinedkadis.terracompositio.entity.custom.ModBoatEntity;
import net.sinedkadis.terracompositio.item.custom.*;
import net.sinedkadis.terracompositio.particle.ModParticles;
import net.sinedkadis.terracompositio.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.sinedkadis.terracompositio.block.ModBlockStateProperties.INFUSED;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TerraCompositio.MOD_ID);

    public static final RegistryObject<Item> PEBBLE = ITEMS.register("pebble",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STONE_STAFF = ITEMS.register("stone_staff",
            () -> new Item(new Item.Properties().stacksTo(1)){
                @Override
                public InteractionResult useOn(UseOnContext pContext) {
                    for (int i = 0; i<360;i++){
                        pContext.getLevel().addParticle(ModParticles.FLOW_PARTICLE.get(),
                                pContext.getClickedPos().getX() + 16D, pContext.getClickedPos().getY() + 18D, pContext.getClickedPos().getZ() + 18D, 0, -1, 0);

                    }
                    return InteractionResult.SUCCESS;
                }
            });
    public static final RegistryObject<Item> FLOW_BOTTLE = ITEMS.register("flow_bottle",
            () -> new FlowBottleItem(new Item.Properties().stacksTo(16).food(ModFoods.FLOW)));
    public static final RegistryObject<Item> OAK_STAFF = ITEMS.register("oak_staff",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> FLOW_CEDAR_HELMET = ITEMS.register("flow_cedar_helmet",
            () -> new ModArmorItem(ModArmorMaterials.FLOW_CEDAR, ArmorItem.Type.HELMET,new Item.Properties()));
    public static final RegistryObject<Item> FLOW_CEDAR_CHESTPLATE = ITEMS.register("flow_cedar_chestplate",
            () -> new ModArmorItem(ModArmorMaterials.FLOW_CEDAR, ArmorItem.Type.CHESTPLATE,new Item.Properties()));
    public static final RegistryObject<Item> FLOW_CEDAR_LEGGINGS = ITEMS.register("flow_cedar_leggings",
            () -> new ModArmorItem(ModArmorMaterials.FLOW_CEDAR, ArmorItem.Type.LEGGINGS,new Item.Properties()));
    public static final RegistryObject<Item> FLOW_CEDAR_BOOTS = ITEMS.register("flow_cedar_boots",
            () -> new ModArmorItem(ModArmorMaterials.FLOW_CEDAR, ArmorItem.Type.BOOTS,new Item.Properties()));
    public static final RegistryObject<Item> FLOWING_FLOW_CEDAR_HELMET = ITEMS.register("flowing_flow_cedar_helmet",
            () -> new FlowArmorItem(ModArmorMaterials.FLOWING_FLOW_CEDAR, ArmorItem.Type.HELMET,new Item.Properties()));
    public static final RegistryObject<Item> FLOWING_FLOW_CEDAR_CHESTPLATE = ITEMS.register("flowing_flow_cedar_chestplate",
            () -> new FlowArmorItem(ModArmorMaterials.FLOWING_FLOW_CEDAR, ArmorItem.Type.CHESTPLATE,new Item.Properties()));
    public static final RegistryObject<Item> FLOWING_FLOW_CEDAR_LEGGINGS = ITEMS.register("flowing_flow_cedar_leggings",
            () -> new FlowArmorItem(ModArmorMaterials.FLOWING_FLOW_CEDAR, ArmorItem.Type.LEGGINGS,new Item.Properties()));
    public static final RegistryObject<Item> FLOWING_FLOW_CEDAR_BOOTS = ITEMS.register("flowing_flow_cedar_boots",
            () -> new FlowArmorItem(ModArmorMaterials.FLOWING_FLOW_CEDAR, ArmorItem.Type.BOOTS,new Item.Properties()));

    public static final RegistryObject<Item> FLOW_CEDAR_SIGN = ITEMS.register("flow_cedar_sign",
            () -> new SignItem(new Item.Properties().stacksTo(16), ModBlocks.FLOW_CEDAR_SIGN.get(),ModBlocks.FLOW_CEDAR_WALL_SIGN.get()));
    public static final RegistryObject<Item> FLOW_CEDAR_HANGING_SIGN = ITEMS.register("flow_cedar_hanging_sign",
            () -> new HangingSignItem(ModBlocks.FLOW_CEDAR_HANGING_SIGN.get(),ModBlocks.FLOW_CEDAR_WALL_HANGING_SIGN.get(),new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> FLOW_CEDAR_BOAT = ITEMS.register("flow_cedar_boat",
            () -> new ModBoatItem(false, ModBoatEntity.Type.FLOW_CEDAR, new Item.Properties()));
    public static final RegistryObject<Item> FLOW_CEDAR_CHEST_BOAT = ITEMS.register("flow_cedar_chest_boat",
            () -> new ModBoatItem(true, ModBoatEntity.Type.FLOW_CEDAR, new Item.Properties()));

    public static final RegistryObject<Item> INFUSED_IRON_INGOT = ITEMS.register("infused_iron_ingot",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> COPPER_NUGGET = ITEMS.register("copper_nugget",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FLOW_INFUSER_KIT = ITEMS.register("flow_infuser_kit",
            () -> new Item(new Item.Properties()){
                @Override
                public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
                    ItemStack offhand = context.getPlayer().getItemInHand(InteractionHand.OFF_HAND);
                    if (offhand != null && offhand.getItem() instanceof AxeItem){
                        if (context.getLevel().getBlockState(context.getClickedPos()).is(ModTags.Blocks.FLOW_CEDAR_LOGS)) {
                            context.getLevel().setBlockAndUpdate(context.getClickedPos(),
                                    ModBlocks.FLOW_INFUSER.get().defaultBlockState()
                                            .setValue(INFUSED,context.getLevel().getBlockState(context.getClickedPos()).getValue(INFUSED)));
                            stack.shrink(1);
                            if (context.getLevel() instanceof ServerLevel level){
                                level.playSound(null,context.getClickedPos(), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS);
                                level.sendParticles(ModParticles.FLOW_STILL_PARTICLE.get(),
                                        context.getClickedPos().getX(),
                                        context.getClickedPos().getY(),
                                        context.getClickedPos().getZ(),
                                        10,
                                        context.getLevel().getRandom().nextFloat(),
                                        context.getLevel().getRandom().nextFloat(),
                                        context.getLevel().getRandom().nextFloat(),
                                        0.5D);
                            }
                            return InteractionResult.CONSUME_PARTIAL;
                        }
                    }
                    return InteractionResult.PASS;
                }

                @Override
                public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
                    super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
                    pTooltipComponents.add(Component.translatable("item.terracompositio.flow_infuser_kit.tooltip").withStyle(ChatFormatting.GRAY));
                }
            });
    public static final RegistryObject<Item> FLOW_CONTAINING_RAW_ORE = ITEMS.register("flow_containing_raw_ore",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> LOW_ENRICHED_FCO = ITEMS.register("low_enriched_fco",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MEDIUM_ENRICHED_FCO = ITEMS.register("medium_enriched_fco",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HIGH_ENRICHED_FCO = ITEMS.register("high_enriched_fco",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FLOW_ROTATING_AXE = ITEMS.register("flow_rotating_axe",
            () -> new WrenchAxeItem(Tiers.IRON, 6.0F, -3.1F, new Item.Properties()));


    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }

}
