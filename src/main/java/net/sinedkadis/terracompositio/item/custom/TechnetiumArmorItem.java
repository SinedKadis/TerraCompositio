package net.sinedkadis.terracompositio.item.custom;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
import net.sinedkadis.terracompositio.item.models.TechnetiumCrownModel;
import net.sinedkadis.terracompositio.registries.TCArmorMaterials;
import net.sinedkadis.terracompositio.registries.TCItems;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

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
            }
        }

        return canEquip;
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity entity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, entity, pSlotId, pIsSelected);
        ICFEHandler icfeHandler = pStack.getCapability(CFECapability.CFE).orElse(DummyCFEHandler.instance);
        transferCFE(pStack, entity, icfeHandler);
        Type type = ((ArmorItem) pStack.getItem()).getType();
        switch (type) {
            case BOOTS -> this.bootTick(pStack,pLevel,entity,icfeHandler);
            case CHESTPLATE -> this.cloakTick(pStack,pLevel,entity,icfeHandler);
        }
    }

    private void cloakTick(ItemStack pStack, Level pLevel, Entity entity, ICFEHandler icfeHandler) {

    }

    private void bootTick(ItemStack pStack, Level pLevel, Entity entity, ICFEHandler icfeHandler) {

    }

    private static void transferCFE(ItemStack pStack, Entity entity, ICFEHandler icfeHandler) {
        if (!(icfeHandler instanceof DummyCFEHandler)) {
            int cfe = icfeHandler.getCFE();
            Type type = ((ArmorItem) pStack.getItem()).getType();
            ICFEHandler playerCap = entity.getCapability(CFECapability.CFE).orElse(DummyCFEHandler.instance);
            if (!type.equals(Type.LEGGINGS)) {
                ICFEHandler leggings = DummyCFEHandler.instance;
                for (ItemStack armor : entity.getArmorSlots()) {
                    if (((ArmorItem) armor.getItem()).getType().equals(Type.LEGGINGS)) {
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
        return TerraCompositio.MOD_ID + ":textures/models/armor/technetium_crown.png";
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> defaultModel) {
                if (stack.is(TCItems.TECHNETIUM_CROWN.get())) return TechnetiumCrownModel.bakedInstance;
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
