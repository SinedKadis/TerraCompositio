package net.sinedkadis.terracompositio.item.custom;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.CFECapability;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.cfe.CFEItemWrapper;
import net.sinedkadis.terracompositio.item.models.TechnetiumCloakModel;
import net.sinedkadis.terracompositio.item.models.TechnetiumCrownModel;
import net.sinedkadis.terracompositio.registries.TCArmorMaterials;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.registries.TCItems;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Mod.EventBusSubscriber(modid = TerraCompositio.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE,value = Dist.CLIENT)
public class TechnetiumArmorItem extends TCArmorItem {


    @Override
    public Type getType() {
        return type;
    }

    private final Type type;

    public TechnetiumArmorItem(Type pType, Properties pProperties) {
        super(TCArmorMaterials.TECHNETIUM, pType, pProperties);
        this.type = pType;
    }



    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
        boolean canEquip = super.canEquip(stack, armorType, entity);
        if (entity instanceof Player || entity instanceof CFENetworkMemberEntity) {
            if (canEquip) {
                if (armorType.equals(EquipmentSlot.HEAD)) {
                    CFENetworkMemberEntity member = ((CFENetworkMemberEntity) entity);
                    TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(member, NetworkAction.UPDATE);
                }
                if (armorType.equals(EquipmentSlot.FEET)) {
                    CompoundTag tag = stack.getOrCreateTag();
                    if (!tag.contains("boot_height")) {
                        tag.putInt("boot_height", (int) (entity.position().y-1));
                    }
                }
            }
        }

        return canEquip;
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity entity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, entity, pSlotId, pIsSelected);
        ICFEHandler icfeHandler = pStack.getCapability(CFECapability.CFE).orElse(DummyCFEHandler.instance);
        switch (type) {
            case BOOTS -> this.bootTick(pStack, pLevel, entity, icfeHandler);
            case CHESTPLATE -> this.cloakTick(pStack, pLevel, entity, icfeHandler);
            default -> {
                return;
            }

        }
        transferCFE(pStack, entity, icfeHandler);
//        Type type = ((ArmorItem) pStack.getItem()).getType();

    }

    private void cloakTick(ItemStack pStack, Level pLevel, Entity entity, ICFEHandler icfeHandler) {

    }

    private void bootTick(ItemStack pStack, Level pLevel, Entity entity, ICFEHandler icfeHandler) {


    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        Level pLevel = livingEntity.level();
        if (pLevel.isClientSide) return;
        BlockPos onPos = BlockPos.containing(livingEntity.position().add(0,-1,0));
        CompoundTag persistentData = livingEntity.getPersistentData();
        String blockPosBelow = "BlockPosBelow";
        String height = "Height";
        if (livingEntity.fallDistance > 5 && !livingEntity.isShiftKeyDown()) {
            BlockState blockStateOn = pLevel.getBlockState(onPos);
            BlockState blockStateOn1 = pLevel.getBlockState(onPos.below(2));

            if (blockStateOn.is(BlockTags.REPLACEABLE)
                    && !persistentData.contains(blockPosBelow)
                    && !blockStateOn1.isAir()) {
                persistentData.putLong(blockPosBelow, onPos.asLong());
                persistentData.putInt(height,onPos.getY()-1);
            }
        }

        if (!livingEntity.getItemBySlot(EquipmentSlot.FEET).is(TCItems.TECHNETIUM_BOOTS.get())) {
            if (persistentData.contains(blockPosBelow)) {
                BlockPos last = BlockPos.of(persistentData.getLong(blockPosBelow));
                BlockState lastBlockState = pLevel.getBlockState(last);
                BlockState replaceState = lastBlockState.hasProperty(WATERLOGGED)
                        && lastBlockState.getValue(WATERLOGGED)
                        ? Blocks.WATER.defaultBlockState()
                        : Blocks.AIR.defaultBlockState();
                if (lastBlockState.is(TCBlocks.TECHNETIUM_BOARD.get()))
                    pLevel.setBlockAndUpdate(last, replaceState);
                persistentData.remove(blockPosBelow);
                persistentData.remove(height);
            }
        }
        BlockState blockStateOn = pLevel.getBlockState(onPos);
        boolean condition = !blockStateOn.is(BlockTags.REPLACEABLE)
                && !blockStateOn.is(TCBlocks.TECHNETIUM_BOARD.get());

//        BlockPos.MutableBlockPos mutableBlockPos = onPos.mutable();
//        boolean remove = false;
//
//        float speed = livingEntity.getSpeed();
//        for (int i = 0; i <= speed*40; i++) {
//            BlockState blockStateOn = pLevel.getBlockState(mutableBlockPos);
//            boolean condition = !blockStateOn.is(BlockTags.REPLACEABLE)
//                    && !blockStateOn.is(TCBlocks.TECHNETIUM_BOARD.get());
//            remove = condition;
//            if (!condition) break;
//            mutableBlockPos.move(livingEntity.getDirection(),1);
//        }
        String cd = "cd";
        boolean cooldown = persistentData.contains(cd) && persistentData.getInt(cd) <= 0;
        if (condition && cooldown) {
            if (persistentData.contains(blockPosBelow)) {
                BlockPos last = BlockPos.of(persistentData.getLong(blockPosBelow));
                BlockState lastBlockState = pLevel.getBlockState(last);
                BlockState replaceState = lastBlockState.hasProperty(WATERLOGGED)
                        && lastBlockState.getValue(WATERLOGGED)
                        ? Blocks.WATER.defaultBlockState()
                        : Blocks.AIR.defaultBlockState();
                if (lastBlockState.is(TCBlocks.TECHNETIUM_BOARD.get()))
                    pLevel.setBlockAndUpdate(last, replaceState);
                persistentData.remove(blockPosBelow);
                persistentData.remove(height);
            }
        } else if (persistentData.contains(cd) && !(persistentData.getInt(cd) <= 0)) {
            persistentData.putInt(cd,persistentData.getInt(cd)-1);
        }

        if (persistentData.contains(blockPosBelow) && !livingEntity.isShiftKeyDown()) {
            BlockPos last = BlockPos.of(persistentData.getLong(blockPosBelow));
            int h = persistentData.getInt(height);
            BlockPos target = onPos.atY(h);
            BlockState boardState = TCBlocks.TECHNETIUM_BOARD.get().defaultBlockState().setValue(WATERLOGGED,
                    blockStateOn.getFluidState().is(Fluids.WATER));
            BlockState lastBlockState = pLevel.getBlockState(last);
            BlockState replaceState = lastBlockState.hasProperty(WATERLOGGED)
                    && lastBlockState.getValue(WATERLOGGED)
                    ? Blocks.WATER.defaultBlockState()
                    : Blocks.AIR.defaultBlockState();
            BlockState targetState = pLevel.getBlockState(target);
            if (!target.equals(last)) {
                persistentData.putLong(blockPosBelow, target.asLong());
                if (targetState.is(BlockTags.REPLACEABLE)) {
                    pLevel.destroyBlock(target,true);
                    pLevel.setBlockAndUpdate(target, boardState);
                }
                if (lastBlockState.is(TCBlocks.TECHNETIUM_BOARD.get())) {
                    pLevel.setBlockAndUpdate(last, replaceState);
                }
            }
        }
    }



    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity livingEntity = event.getEntity();
        Level pLevel = livingEntity.level();
        if (pLevel.isClientSide) return;
        if (livingEntity.getItemBySlot(EquipmentSlot.FEET).is(TCItems.TECHNETIUM_BOOTS.get())){
            CompoundTag persistentData = livingEntity.getPersistentData();
            String blockPosBelow = "BlockPosBelow";
            String height = "Height";
            String cd = "cd";

            BlockPos onPos = BlockPos.containing(
                    livingEntity.position().add(0,-1,0)
            );


            BlockPos.MutableBlockPos mutableBlockPos = onPos.mutable();
            float speed = livingEntity.getSpeed();
            for (int i = 0; i <= speed*30; i++) {
                BlockState blockStateOn = pLevel.getBlockState(mutableBlockPos);
                if (blockStateOn.is(BlockTags.REPLACEABLE) && !persistentData.contains(blockPosBelow)) {
                    persistentData.putLong(blockPosBelow, onPos.asLong());
                    persistentData.putInt(height,onPos.getY());
                    persistentData.putInt(cd,20);
                    break;
                }
                mutableBlockPos.move(livingEntity.getDirection(),1);
            }


            if (persistentData.contains(blockPosBelow) && !livingEntity.isShiftKeyDown()) {
                int h = persistentData.getInt(height);

                float viewXRot = -livingEntity.getXRot();

                if (viewXRot > 30) {
                    h++;
                }
                if (viewXRot < -30) {
                    h--;
                }
                persistentData.putInt(height,h);
            }
        }
    }


    private static void transferCFE(ItemStack pStack, Entity entity, ICFEHandler icfeHandler) {
        if (!(icfeHandler instanceof DummyCFEHandler)) {
            int cfe = icfeHandler.getCFE();
            Type type = ((ArmorItem) pStack.getItem()).getType();
            ICFEHandler playerCap = entity.getCapability(CFECapability.CFE).orElse(DummyCFEHandler.instance);
            if (!type.equals(Type.LEGGINGS)) {
                ICFEHandler leggings = DummyCFEHandler.instance;
                for (ItemStack armor : entity.getArmorSlots()) {
                    if (armor.getItem() instanceof ArmorItem armorItem &&  armorItem.getType().equals(Type.LEGGINGS)) {
                        leggings = armor.getCapability(CFECapability.CFE).orElse(DummyCFEHandler.instance);
                    }
                }
                if (cfe < 1) {
                    if (leggings.takeCFE(1,true)>0) {
                        icfeHandler.addCFE(
                                leggings.takeCFE(1,false),null,false);
                    } else if (playerCap.takeCFE(1,true)>0) {
                        icfeHandler.addCFE(
                                playerCap.takeCFE(1,false),null,false);
                    }
                }
            } else {
                if (playerCap.takeCFE(icfeHandler.getFreeSpace(),true)>0) {
                    icfeHandler.addCFE(
                            playerCap.takeCFE(icfeHandler.getFreeSpace(),false),null,false);
                }
            }
        }
    }

    @Override
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return switch (slot) {
            case HEAD -> TerraCompositio.MOD_ID + ":textures/models/armor/technetium_crown.png";
            case CHEST -> TerraCompositio.MOD_ID + ":textures/models/armor/technetium_cloak.png";
            case FEET -> TerraCompositio.MOD_ID + ":textures/models/armor/technetium_boots.png";
            case LEGS -> TerraCompositio.MOD_ID + ":textures/models/armor/technetium_leggings.png";
            default -> null;
        };
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> defaultModel) {
                if (stack.is(TCItems.TECHNETIUM_CROWN.get())) return TechnetiumCrownModel.bakedInstance;
                if (stack.is(TCItems.TECHNETIUM_CLOAK.get())) return TechnetiumCloakModel.bakedInstance;
                return defaultModel;
            }
        });
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        Type type = ((ArmorItem) stack.getItem()).getType();
        return (ICapabilityProvider) new CFEItemWrapper(stack).setMaxCFE(type.equals(Type.LEGGINGS)?100:1);
    }
}
