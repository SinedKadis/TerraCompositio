package net.sinedkadis.terracompositio.item.custom;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.entity.custom.FlowCedarEntEntity;
import net.sinedkadis.terracompositio.item.models.TechnetiumCrownModel;
import net.sinedkadis.terracompositio.registries.TCArmorMaterials;
import net.sinedkadis.terracompositio.registries.TCItems;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechnetiumArmorItem extends TCArmorItem {
    public static final String BEND_SENDER_TAG = "BendSender";

    @Override
    public @NotNull Type getType() {
        return type;
    }

    private final Type type;

    public TechnetiumArmorItem(Type pType, Properties pProperties) {
        super(TCArmorMaterials.TECHNETIUM, pType, pProperties);
        this.type = pType;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack pStack, Player pPlayer, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
        if (pStack.is(TCItems.TECHNETIUM_CROWN.get()) && pInteractionTarget instanceof FlowCedarEntEntity entity) {
            ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
            if (head.isEmpty()) {
                entity.setItemSlot(EquipmentSlot.HEAD,pStack);
                pStack.shrink(1);
                entity.setDropChance(EquipmentSlot.HEAD, 2.0F);
                entity.setPersistenceRequired();
            }
        }

        return InteractionResult.SUCCESS;
    }

    BlockPos lastPos = BlockPos.ZERO;
    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
        if (pStack.is(TCItems.TECHNETIUM_CROWN.get())) {
            BlockPos onPos = pEntity.blockPosition();
            if (!lastPos.equals(onPos)) {
                lastPos = new BlockPos(onPos);
                BlockPos bendSender = getBendSender(pStack);
                PathPointerBlockEntity memberAt;
                if (bendSender != null) {
                    memberAt = ((PathPointerBlockEntity) pLevel.getBlockEntity(bendSender));
                    if (memberAt != null) {
                        memberAt.onCFENetworkMemberUpdate(pLevel, bendSender);
                    }
                }
            }
        }
    }

    public static void setBendSender(ItemStack stack, @Nullable BlockPos pos) {
        CompoundTag tag = stack.getOrCreateTag();
        if (pos != null) {
            tag.putLong(BEND_SENDER_TAG, pos.asLong());
        } else {
            tag.remove(BEND_SENDER_TAG);
        }
    }


    public static @Nullable BlockPos getBendSender(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(BEND_SENDER_TAG)) {
            long posLong = tag.getLong(BEND_SENDER_TAG);
            return BlockPos.of(posLong);
        }
        return null;
    }

    @Override
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return TerraCompositio.MOD_ID + ":textures/models/armor/technetium_crown.png";
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> defaultModel) {
                if (stack.is(TCItems.TECHNETIUM_CROWN.get())) return TechnetiumCrownModel.bakedInstance;
                return defaultModel;
            }
        });
    }


}
