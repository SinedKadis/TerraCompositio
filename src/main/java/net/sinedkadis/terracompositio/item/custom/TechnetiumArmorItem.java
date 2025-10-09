package net.sinedkadis.terracompositio.item.custom;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
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
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
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
import net.sinedkadis.terracompositio.registries.TCKeyMappings;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
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
                        tag.putInt("boot_height", entity.getOnPos().getY());
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
        CompoundTag tag = pStack.getOrCreateTag();
        if (pLevel.isClientSide) {
            BlockPos onPos = BlockPos.containing(entity.position().add(0,-1,0));
            if (Minecraft.getInstance().options.keyJump.consumeClick()
                && pLevel.getBlockState(onPos).is(BlockTags.REPLACEABLE)) {
                tag.putInt("boot_height", entity.blockPosition().getY()-1);
            }
            if (TCKeyMappings.BOOT_REDUCE.getKeyMapping().consumeClick()) {
                tag.putInt("boot_height",tag.getInt("boot_height")-1);
                tag.putBoolean("update_block",true);
            }
        }
        if (tag.contains("boot_height")) {
            BlockPos onPos = BlockPos.containing(entity.position().add(0,-1,0));
            BlockState blockState = pLevel.getBlockState(onPos);
            BlockState boardState = TCBlocks.TECHNETIUM_BOARD.get().defaultBlockState().setValue(WATERLOGGED,
                    (blockState.hasProperty(WATERLOGGED) && blockState.getValue(WATERLOGGED))
                            || (blockState.getFluidState().isSource() && blockState.is(Blocks.WATER)));
            BlockState replaceState = boardState.getValue(WATERLOGGED)
                    ? Blocks.WATER.defaultBlockState()
                    : Blocks.AIR.defaultBlockState();
            if (!pLevel.isClientSide){
                if (blockState.is(BlockTags.REPLACEABLE)) {
                    if (!entity.isShiftKeyDown()) {
                        if (onPos.getY() == tag.getInt("boot_height")) {
                            pLevel.setBlockAndUpdate(onPos, boardState);
                            if (tag.contains("lastBlockPos")) {
                                pLevel.setBlockAndUpdate(BlockPos.of(tag.getLong("lastBlockPos")),
                                        replaceState);
                            }
                            tag.putLong("lastBlockPos", onPos.asLong());
                        }
                    }
                } else if (!blockState.is(boardState.getBlock()) && tag.contains("lastBlockPos")) {
                    pLevel.setBlockAndUpdate(BlockPos.of(tag.getLong("lastBlockPos")),
                            replaceState);
                    tag.remove("lastBlockPos");
                }
                if (tag.contains("update_block")
                        && tag.getBoolean("update_block")) {
                    pLevel.setBlockAndUpdate(onPos, boardState);
                    if (tag.contains("lastBlockPos")) {
                        pLevel.setBlockAndUpdate(BlockPos.of(tag.getLong("lastBlockPos")),
                                replaceState);
                    }
                    tag.putLong("lastBlockPos", onPos.asLong());
                    tag.putBoolean("update_block",false);
                }
                boolean isOn = false;
                for (ItemStack armorSlot : entity.getArmorSlots()) {
                    if (armorSlot == pStack) {
                        isOn = true;
                        break;
                    }
                }

                if (!isOn) {
                    if (tag.contains("lastBlockPos")) {
                        pLevel.setBlockAndUpdate(BlockPos.of(tag.getLong("lastBlockPos")),
                                replaceState);
                    }
                }
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
