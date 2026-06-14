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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.cfe.CFEItemWrapper;
import net.sinedkadis.terracompositio.config.TCCommonConfigs;
import net.sinedkadis.terracompositio.item.models.TechnetiumBootsModel;
import net.sinedkadis.terracompositio.item.models.TechnetiumCloakModel;
import net.sinedkadis.terracompositio.item.models.TechnetiumCrownModel;
import net.sinedkadis.terracompositio.network.TCPackets;
import net.sinedkadis.terracompositio.network.packets.C2SBoardSync;
import net.sinedkadis.terracompositio.registries.TCArmorMaterials;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.registries.TCItems;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Mod.EventBusSubscriber(modid = TerraCompositio.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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
        ICFEHandler icfeHandler = pStack.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance);
        switch (type) {
            case HELMET -> this.helmetInventoryTick(pStack, pLevel, entity, icfeHandler);
            case CHESTPLATE -> this.chestplateInventoryTick(pStack, pLevel, entity, icfeHandler);
            case LEGGINGS -> this.leggingsInventoryTick(pStack, pLevel, entity, icfeHandler);
            case BOOTS -> this.bootInventoryTick(pStack, pLevel, entity, icfeHandler);

            default -> {
            }

        }


    }

    private void helmetInventoryTick(ItemStack ignoredPStack, Level ignoredPLevel, Entity pEntity, ICFEHandler ignoredIcfeHandler) {
        if (pEntity.tickCount % 20 != 0) return;
        ICFEHandler playerHandler = pEntity.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance);
        boolean hasSpace = playerHandler.getFreeSpace() > 0;
        if (!hasSpace) {
            for (ItemStack slot : pEntity.getArmorSlots()) {
                hasSpace |= slot.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance).getFreeSpace() > 0;
            }
        }
        if (hasSpace) {
            TerraCompositioAPI.instance().getCFENetworkInstance().fireCFENetworkEvent((CFENetworkMember) pEntity, NetworkAction.UPDATE);
        }
    }

    private void chestplateInventoryTick(ItemStack ignoredPStack, Level ignoredPLevel, Entity ignoredEntity, ICFEHandler ignoredIcfeHandler) {

    }

    private void leggingsInventoryTick(ItemStack itemStack, Level ignoredPLevel, Entity entity, ICFEHandler thisHandler) {
        for (ItemStack stack : entity.getArmorSlots()) {
            if (stack.equals(itemStack)) return;
            ICFEHandler icfeHandler = stack.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance);
            int taken = thisHandler.takeCFE(TCCommonConfigs.CFE_PER_BURST_TRANSFER_LIMIT.get(), true);
            int added = icfeHandler.addCFE(taken,false);
            thisHandler.takeCFE(added,false);
        }
        ICFEHandler playerHandler = entity.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance);
        int taken = thisHandler.addCFE(TCCommonConfigs.CFE_PER_BURST_TRANSFER_LIMIT.get(), true);
        int added = playerHandler.takeCFE(taken,false);
        thisHandler.addCFE(added,false);
    }

    static final String height = "Height";
    static final String cd = "cd";
    private void bootInventoryTick(ItemStack ignoredPStack, Level ignoredLevel, Entity ignoredEntity, ICFEHandler ignoredIcfeHandler) {

    }

    public static boolean isOnCoolDown(Entity entity, CompoundTag persistentData) {
        return !persistentData.contains(cd) || (entity.tickCount - persistentData.getInt(cd) > 20);
    }

    public static boolean setHeightIfFalling(Level level, Entity entity, ICFEHandler icfeHandler, BlockPos onPos, CompoundTag persistentData) {
        if (entity.fallDistance > 3 && !entity.isShiftKeyDown()
                && icfeHandler.getCFE() >= 1) {

            BlockState blockStateBelow1 = level.getBlockState(onPos.below());
            BlockState blockStateBelow3 = level.getBlockState(onPos.below(3));

            if (blockStateBelow1.is(BlockTags.REPLACEABLE)
                    && !blockStateBelow3.isAir()) {
                persistentData.putInt(height, onPos.getY()-1);
                return true;
            }
        }
        return false;
    }


    public static void onBlockChanged(LivingEntity livingEntity, BlockPos lastPos) {
        if (!(livingEntity instanceof Player player))
            return;
        Level level = livingEntity.level();

        BlockPos onPos = BlockPos.containing(player.position().add(0,-1,0));
        BlockPos standingPos = player.getOnPos();

        //if (onPos.equals(standingPos)) return;

        BlockState standingState = level.getBlockState(standingPos);

        CompoundTag persistentData = player.getPersistentData();

        ICFEHandler icfeHandler = player.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance);

        boolean fallSaveActivated = setHeightIfFalling(level, player, icfeHandler, onPos, persistentData);

        boolean standingOnBoard = standingState.is(TCBlocks.TECHNETIUM_BOARD.get());

        if (!standingOnBoard && !fallSaveActivated && isOnCoolDown(player, persistentData)) {
            persistentData.remove(height);
        }

        if (!persistentData.contains(height))
            return;

        BlockPos posOnHeight = onPos.atY(persistentData.getInt(height));
        BlockState blockStateOnHeight = level.getBlockState(posOnHeight);

        boolean allowBoardPlace = blockStateOnHeight.is(BlockTags.REPLACEABLE)
                && !blockStateOnHeight.is(TCBlocks.TECHNETIUM_BOARD.get())
                && (standingState.is(TCBlocks.TECHNETIUM_BOARD.get()) || fallSaveActivated);

        if (!allowBoardPlace)
            return;


        boolean waterlogged = blockStateOnHeight.hasProperty(WATERLOGGED) && blockStateOnHeight.getValue(WATERLOGGED);

        BlockState boardState = TCBlocks.TECHNETIUM_BOARD.get().defaultBlockState().setValue(WATERLOGGED,waterlogged);

        takeCFEAndSetBoard(icfeHandler, level, posOnHeight, boardState);


        if (!standingOnBoard)
            return;


        BlockPos lastPosOn = lastPos.below();
        BlockState blockState = level.getBlockState(lastPosOn);

        if (!blockState.is(TCBlocks.TECHNETIUM_BOARD.get()))
            return;
        BlockPos newOnPos = player.getOnPos();
        if (lastPosOn.equals(onPos) || lastPosOn.equals(newOnPos))
            return;

        level.destroyBlock(lastPosOn, true);
    }

    private static void takeCFEAndSetBoard(ICFEHandler icfeHandler, Level level, BlockPos posOnHeight, BlockState boardState) {
        if (icfeHandler.takeCFE(1,false) > 0 && level.isClientSide()) {
            level.destroyBlock(posOnHeight,true);
            level.setBlock(posOnHeight, boardState, 1);
            TCPackets.CHANNEL.send(PacketDistributor.SERVER.noArg(),new C2SBoardSync(posOnHeight));
        }
    }


    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player)) return;

        Level level = livingEntity.level();
        ItemStack stack = livingEntity.getItemBySlot(EquipmentSlot.FEET);

        if (!stack.is(TCItems.TECHNETIUM_BOOTS.get())) return;

        CompoundTag persistentData = livingEntity.getPersistentData();

        BlockPos onPos = BlockPos.containing(
                livingEntity.position().add(0, -1, 0)
        );
        BlockState blockStateOn = level.getBlockState(onPos);

        boolean waterlogged = blockStateOn.hasProperty(WATERLOGGED) && blockStateOn.getValue(WATERLOGGED);
        BlockState boardState = TCBlocks.TECHNETIUM_BOARD.get().defaultBlockState().setValue(WATERLOGGED, waterlogged);


        ICFEHandler icfeHandler = stack.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance);
        if (icfeHandler.getCFE() < 1) return;

        if (!blockStateOn.is(TCBlocks.TECHNETIUM_BOARD.get())) {
            calculateVelocityAndSetBoard(onPos, livingEntity, level, persistentData, icfeHandler, boardState);
        } else {
            changeHeightByView(livingEntity, persistentData, onPos, level, icfeHandler, boardState);
        }
    }

    private static void changeHeightByView(LivingEntity livingEntity,
                                           CompoundTag persistentData,
                                           BlockPos onPos,
                                           Level level,
                                           ICFEHandler icfeHandler,
                                           BlockState boardState) {
        if (!livingEntity.isShiftKeyDown()) {
            int h = persistentData.getInt(height);

            float viewXRot = -livingEntity.getXRot();

            if (viewXRot > 30) {
                h++;

            }
            if (viewXRot < -30) {
                h--;
            }
            BlockPos pPos = onPos.atY(h);
            if (level.getBlockState(pPos).is(BlockTags.REPLACEABLE)) {
                takeCFEAndSetBoard(icfeHandler, level, pPos, boardState);
            }
            persistentData.putInt(height, h);
        }
    }

    private static void calculateVelocityAndSetBoard(BlockPos onPos,
                                                     LivingEntity livingEntity,
                                                     Level level,
                                                     CompoundTag persistentData,
                                                     ICFEHandler icfeHandler,
                                                     BlockState boardState) {

        Vec3 deltaMovement = livingEntity.getDeltaMovement();

        double deltaX = Math.round(deltaMovement.x() * 10);
        double deltaZ = Math.round(deltaMovement.z() * 10);
        BlockPos relative = onPos.offset((int) deltaX, 0, (int) deltaZ);
        if (level.getBlockState(relative).is(BlockTags.REPLACEABLE)) {
            persistentData.putInt(height, onPos.getY());
            persistentData.putInt(cd, livingEntity.tickCount);
            takeCFEAndSetBoard(icfeHandler, level, relative, boardState);
        }
    }

    @Override
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return switch (slot) {
            case HEAD -> TerraCompositio.MOD_ID + ":textures/models/armor/technetium_crown.png";
            case CHEST -> TerraCompositio.MOD_ID + ":textures/models/armor/technetium_chestplate.png";
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
                if (stack.is(TCItems.TECHNETIUM_CHESTPLATE.get())) return TechnetiumCloakModel.bakedInstance;
                if (stack.is(TCItems.TECHNETIUM_BOOTS.get())) return TechnetiumBootsModel.Humanoid.bakedInstance;
                return defaultModel;
            }
        });
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        Type type = ((ArmorItem) stack.getItem()).getType();
        if (type.equals(Type.HELMET)) return null;
        return (ICapabilityProvider) new CFEItemWrapper(stack).setMaxCFE(type.equals(Type.LEGGINGS)?100:1);
    }
}
