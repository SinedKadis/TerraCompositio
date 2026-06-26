package net.sinedkadis.terracompositio.item.custom;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.api.IECFStorageExtensionItem;
import net.sinedkadis.terracompositio.api.IHaveExtensibleECFStorageItem;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.dummies.DummyECFHandler;
import net.sinedkadis.terracompositio.api.helpers.TooltipHelper;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMember;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.ecf.IECFHandler;
import net.sinedkadis.terracompositio.config.TCClientConfigs;
import net.sinedkadis.terracompositio.config.TCCommonConfigs;
import net.sinedkadis.terracompositio.ecf.ECFItemWrapper;
import net.sinedkadis.terracompositio.item.models.TechnetiumBootsModel;
import net.sinedkadis.terracompositio.item.models.TechnetiumChestplateModel;
import net.sinedkadis.terracompositio.item.models.TechnetiumCrownModel;
import net.sinedkadis.terracompositio.network.TCPackets;
import net.sinedkadis.terracompositio.network.packets.C2SBoardSync;
import net.sinedkadis.terracompositio.registries.TCArmorMaterials;
import net.sinedkadis.terracompositio.registries.TCBlockStateProperties;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.registries.TCItems;
import net.sinedkadis.terracompositio.util.OffsetVConsumer;
import net.sinedkadis.terracompositio.util.accessors.PlayerKnowledgeAccessor;
import net.sinedkadis.terracompositio.util.helpers.BlockPosHelper;
import net.sinedkadis.terracompositio.util.helpers.ParticleHelper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Mod.EventBusSubscriber(modid = TerraCompositio.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TechnetiumArmorItem extends TCArmorItem implements IHaveExtensibleECFStorageItem {

    private static final String last = "last";
    private static final String height = "Height";
    private static final String cd = "cd";

    @Override
    public Type getType() {
        return type;
    }

    private final Type type;

    public TechnetiumArmorItem(Type pType, Properties pProperties) {
        super(TCArmorMaterials.TECHNETIUM, pType, pProperties);
        this.type = pType;
    }



    public static void onLivingHurtEvent(LivingAttackEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player)) return;

        DamageSource source = event.getSource();
        Entity damager = source.getEntity();
        if (damager == null) return;

        ItemStack itemBySlot = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
        if (itemBySlot.is(TCItems.TECHNETIUM_CHESTPLATE.get())) {
            IECFHandler IECFHandler = itemBySlot.getCapability(TCCapabilities.ECF).orElse(DummyECFHandler.instance);
            if (IECFHandler.takeECF(1, true) > 0) {
                Level level = livingEntity.level();
                BlockPos pPos = livingEntity.blockPosition();
                if (livingEntity.getRandom().nextFloat() > 0.3f) {
                    IECFHandler.takeECF(1, false);
                    if (IECFHandler.getECF() <= 0) {
                        level.playSound(null,
                                pPos,
                                SoundEvents.SHIELD_BREAK,
                                SoundSource.PLAYERS);
                    }
                }


                level.playSound(null,
                        pPos,
                        SoundEvents.SHIELD_BLOCK,
                        SoundSource.PLAYERS);

                ParticleHelper.spawnParticlesIn(level, pPos.above());
                event.setCanceled(true);
            }
        }
    }

    public static void onBlockChanged(LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player))
            return;
        if (player.getAbilities().mayfly) return;

        Level level = livingEntity.level();
        if (!level.isClientSide()) return;

        BlockPos onPos = BlockPos.containing(player.position().add(0,-1,0));
        BlockPos standingPos = player.getOnPos();

        BlockState standingState = level.getBlockState(standingPos);

        if (standingState.is(Blocks.AIR)) {
            standingState = level.getBlockState(standingPos.below());
        }

        CompoundTag persistentData = player.getPersistentData();

        IECFHandler IECFHandler = player.getItemBySlot(EquipmentSlot.FEET).getCapability(TCCapabilities.ECF).orElse(DummyECFHandler.instance);

        boolean fallSaveActivated = setHeightIfFalling(level, player, IECFHandler, onPos, persistentData);

        boolean standingOnBoard = standingState.is(TCBlocks.ECF_BOARD.get()) && !standingState.getValue(TCBlockStateProperties.PERMANENT);

        boolean jumped = justJumped(player, persistentData);

        if (!standingOnBoard && !fallSaveActivated && !jumped
                && persistentData.contains(height)) {
            persistentData.remove(height);
        }

        String last = TechnetiumArmorItem.last;
        BlockPos destroyPos = null;
        if (persistentData.contains(last)) {
            if (level.isClientSide()) {
                destroyPos = BlockPosHelper.loadBlockPos(persistentData.getCompound(last));
            }
        }

        if (!persistentData.contains(height)) {
            if (destroyPos != null) {
                level.destroyBlock(destroyPos, true);
                TCPackets.CHANNEL.send(PacketDistributor.SERVER.noArg(), new C2SBoardSync(destroyPos, false));
                persistentData.remove(last);
            }
            return;
        }


        BlockPos posOnHeight = onPos.atY(persistentData.getInt(height));
        BlockState blockStateOnHeight = level.getBlockState(posOnHeight);

        boolean allowBoardPlace = blockStateOnHeight.is(BlockTags.REPLACEABLE)
                && !blockStateOnHeight.is(TCBlocks.ECF_BOARD.get())
                && (standingState.is(TCBlocks.ECF_BOARD.get()) || fallSaveActivated || jumped);

        if (destroyPos != null && (allowBoardPlace || !posOnHeight.equals(destroyPos))) {
            level.destroyBlock(destroyPos, true);
            TCPackets.CHANNEL.send(PacketDistributor.SERVER.noArg(), new C2SBoardSync(destroyPos, false));
            persistentData.remove(last);
        }

        if (allowBoardPlace && !livingEntity.isShiftKeyDown()) {
            FluidState fluidState = blockStateOnHeight.getFluidState();
            boolean waterlogged = (blockStateOnHeight.hasProperty(WATERLOGGED) && blockStateOnHeight.getValue(WATERLOGGED))
                    || fluidState.is(Fluids.WATER);

            BlockState boardState = TCBlocks.ECF_BOARD.get().defaultBlockState().setValue(WATERLOGGED, waterlogged);

            takeECFAndSetBoard(IECFHandler, level, posOnHeight, boardState);
            persistentData.put(last, BlockPosHelper.saveBlockPos(posOnHeight));
        }
    }

    public static boolean setHeightIfFalling(Level level, Entity entity, IECFHandler IECFHandler, BlockPos onPos, CompoundTag persistentData) {
        float fallDistance = entity.fallDistance;
        if (fallDistance > 3 && !entity.isShiftKeyDown()
                && IECFHandler.getECF() >= 1) {

            BlockState blockStateBelow1 = level.getBlockState(onPos.below());
            BlockState blockStateBelow3 = level.getBlockState(onPos.below(3));

            if (blockStateBelow1.is(BlockTags.REPLACEABLE)
                    && !blockStateBelow3.isAir()) {
                persistentData.putInt(height, onPos.getY() - 1);
                return true;
            }
        }
        return false;
    }

    public static boolean justJumped(Entity entity, CompoundTag persistentData) {
        return persistentData.contains(cd) && (entity.tickCount - persistentData.getInt(cd) < 30);
    }

    public static void onDoubleJump(LocalPlayer localPlayer) {
        if (localPlayer.getAbilities().mayfly) return;
        ItemStack itemBySlot = localPlayer.getItemBySlot(EquipmentSlot.FEET);
        if (!itemBySlot.is(TCItems.TECHNETIUM_BOOTS.get())) return;

        IECFHandler IECFHandler = itemBySlot.getCapability(TCCapabilities.ECF).orElse(DummyECFHandler.instance);
        if (!(IECFHandler.takeECF(1, false) > 0)) return;

        CompoundTag persistentData = localPlayer.getPersistentData();

        persistentData.putInt(cd, localPlayer.tickCount);
        localPlayer.level().playSound(localPlayer, localPlayer.blockPosition(), SoundEvents.CHICKEN_EGG, SoundSource.PLAYERS);
        localPlayer.move(MoverType.SELF, new Vec3(0, 5, 0));
        persistentData.putInt(height, localPlayer.getBlockY() - 1);

    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity entity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, entity, pSlotId, pIsSelected);
        IECFHandler IECFHandler = pStack.getCapability(TCCapabilities.ECF).orElse(DummyECFHandler.instance);
        switch (type) {
            case HELMET -> this.helmetInventoryTick(pStack, pLevel, entity, IECFHandler);
            case CHESTPLATE -> {
                // made via onLivingHurt
            }
            case LEGGINGS -> this.leggingsInventoryTick(pStack, pLevel, entity, IECFHandler);
            case BOOTS -> {
                // made via onBlockChanged and onLivingJump
            }

            default -> {
            }

        }
    }

    private static void takeECFAndSetBoard(IECFHandler IECFHandler, Level level, BlockPos posOnHeight, BlockState boardState) {
        if (IECFHandler.takeECF(1, false) > 0 && level.isClientSide()) {
            level.destroyBlock(posOnHeight,true);
            level.setBlock(posOnHeight, boardState, 1);
            TCPackets.CHANNEL.send(PacketDistributor.SERVER.noArg(),
                    new C2SBoardSync(posOnHeight, true, 1, boardState.getValue(WATERLOGGED))
            );
        }
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player player)) return;
        if (player.getAbilities().mayfly) return;

        Level level = livingEntity.level();
        ItemStack stack = livingEntity.getItemBySlot(EquipmentSlot.FEET);

        if (!stack.is(TCItems.TECHNETIUM_BOOTS.get())) return;

        CompoundTag persistentData = livingEntity.getPersistentData();

        BlockPos onPos = BlockPos.containing(
                livingEntity.position().add(0, -1, 0)
        );
        BlockState blockStateOn = level.getBlockState(onPos);

        IECFHandler IECFHandler = stack.getCapability(TCCapabilities.ECF).orElse(DummyECFHandler.instance);
        if (IECFHandler.getECF() < 1) return;

        if (!blockStateOn.is(TCBlocks.ECF_BOARD.get())) {
            calculateVelocityAndSetBoard(onPos, livingEntity, level, persistentData);
        } else if (!blockStateOn.getValue(TCBlockStateProperties.PERMANENT)) {
            changeHeightByView(livingEntity, persistentData, level);
        }
        persistentData.putInt(cd, livingEntity.tickCount);
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
        boolean canEquip = super.canEquip(stack, armorType, entity);
        if (entity instanceof Player || entity instanceof ECFNetworkMemberEntity) {
            if (canEquip) {
                if (armorType.equals(EquipmentSlot.HEAD)) {
                    ECFNetworkMemberEntity member = ((ECFNetworkMemberEntity) entity);
                    TerraCompositioAPI.INSTANCE.getECFNetworkInstance().fireECFNetworkEvent(member, NetworkAction.UPDATE);
                }
            }
        }

        return canEquip;
    }

    private void helmetInventoryTick(ItemStack ignoredPStack, Level ignoredPLevel, Entity pEntity, IECFHandler ignoredIECFHandler) {
        if (pEntity.tickCount % 20 != 0) return;
        IECFHandler playerHandler = pEntity.getCapability(TCCapabilities.ECF).orElse(DummyECFHandler.instance);

        if (playerHandler.getFreeSpace() > 0) {
            TerraCompositioAPI.instance().getECFNetworkInstance().fireECFNetworkEvent((ECFNetworkMember) pEntity, NetworkAction.UPDATE);
        }
    }

    private void leggingsInventoryTick(ItemStack itemStack, Level ignoredPLevel, Entity entity, IECFHandler thisHandler) {
        for (ItemStack stack : entity.getArmorSlots()) {
            if (stack.equals(itemStack)) {
                IECFHandler playerHandler = entity.getCapability(TCCapabilities.ECF).orElse(DummyECFHandler.instance).getMainHandler();
                int taken = thisHandler.addECF(TCCommonConfigs.ECF_PER_BURST_TRANSFER_LIMIT.get(), true);
                int added = playerHandler.takeECF(taken, false);
                thisHandler.addECF(added, false);
                continue;
            }
            IECFHandler IECFHandler = stack.getCapability(TCCapabilities.ECF).orElse(DummyECFHandler.instance);
            int taken = thisHandler.takeECF(TCCommonConfigs.ECF_PER_BURST_TRANSFER_LIMIT.get(), true);
            int added = IECFHandler.addECF(taken, false);
            thisHandler.takeECF(added, false);
        }
    }

    public static @Nullable VertexConsumer getTrimVertexConsumer(TextureAtlas armorTrimAtlas, MultiBufferSource pBuffer, LivingEntity pLivingEntity, ItemStack itemstack, boolean normal) {
        Optional<ArmorTrim> trim = ArmorTrim.getTrim(pLivingEntity.level().registryAccess(), itemstack);

        if (trim.isEmpty()) return null;

        TextureAtlasSprite sprite = armorTrimAtlas.getSprite(trim.get().outerTexture(TCArmorMaterials.TECHNETIUM));

        VertexConsumer base = sprite.wrap(pBuffer.getBuffer(Sheets.armorTrimsSheet()));

        float vOffset = normal ? 0 : 0.04f;
        return new OffsetVConsumer(base, vOffset);
    }

    private static void changeHeightByView(LivingEntity livingEntity,
                                           CompoundTag persistentData,
                                           Level level) {
        if (!livingEntity.isShiftKeyDown() && level.isClientSide()) {
            int h = persistentData.getInt(height);

            float viewXRot = -livingEntity.getXRot();

            if (viewXRot > 30) {
                h++;
            } else if (viewXRot < -30) {
                h--;
            } else return;

            persistentData.putInt(height, h);
        }
    }

    private static void calculateVelocityAndSetBoard(BlockPos onPos,
                                                     LivingEntity livingEntity,
                                                     Level level,
                                                     CompoundTag persistentData) {

        Vec3 deltaMovement = livingEntity.getDeltaMovement();

        double deltaX = Math.round(deltaMovement.x() * 10);
        double deltaZ = Math.round(deltaMovement.z() * 10);
        BlockPos relative = onPos.offset((int) deltaX, 0, (int) deltaZ);
        if (level.getBlockState(relative).is(BlockTags.REPLACEABLE)) {
            persistentData.putInt(height, onPos.getY());
        }
    }

    @Override
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return switch (slot) {
            case HEAD -> TerraCompositio.MOD_ID + ":textures/models/armor/technetium_crown.png";
            case CHEST -> {
                if (stack.getCapability(TCCapabilities.ECF).orElse(DummyECFHandler.instance).getECF() <= 0)
                    yield TerraCompositio.MOD_ID + ":textures/models/armor/technetium_chestplate/armor_layer_no_shield.png";
                int textureIndex = (int) (Util.getMillis() / 300) % 16;
                yield TerraCompositio.MOD_ID + ":textures/models/armor/technetium_chestplate/armor_layer_"
                        + textureIndex + ".png";
            }
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
                if (stack.is(TCItems.TECHNETIUM_CHESTPLATE.get())) return TechnetiumChestplateModel.bakedInstance;
                if (stack.is(TCItems.TECHNETIUM_BOOTS.get())) return TechnetiumBootsModel.Humanoid.bakedInstance;
                return defaultModel;
            }
        });
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null
                && ((PlayerKnowledgeAccessor) player).isCreationAcknowledged()
                && TCClientConfigs.APPLE_ITEM_TOOLTIP.get()) {
            pTooltipComponents.add(
                    TooltipHelper.keyWithArg(TooltipHelper.Keys.ECF,
                            pStack.getCapability(TCCapabilities.ECF).orElse(DummyECFHandler.instance).getECF())
            );
            IECFStorageExtensionItem currentExtension = this.getCurrentExtension(pStack);
            if (currentExtension.maxStorage() > 0) {
                Component description = currentExtension.self().getItem().getDescription();
                pTooltipComponents.add(
                        TooltipHelper.keyWithArg(TooltipHelper.Keys.STORAGE_EXTENSION, description, TooltipHelper.Units.NO_UNITS)
                );
            }
            if (TCCommonConfigs.DEBUG.get()) {
                pTooltipComponents.add(
                        TooltipHelper.keyWithArg(TooltipHelper.Keys.MAX_ECF,
                                pStack.getCapability(TCCapabilities.ECF).orElse(DummyECFHandler.instance).getMaxECF())
                );
            }
        }
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ECFItemWrapper(stack);
    }

    @Override
    public IECFStorageExtensionItem getCurrentExtension(ItemStack stack) {
        if (stack.getCapability(TCCapabilities.ECF).orElse(DummyECFHandler.instance).getMaxECF() == 8)
            return () -> 0;
        Item item = ItemStack.of(stack.getOrCreateTag().getCompound("StorageExtension")).getItem();
        return item instanceof IECFStorageExtensionItem iecfse ? iecfse : () -> 0;
    }

    @Override
    public void setExtension(ItemStack stack, IECFStorageExtensionItem extensionItem) {
        if (extensionItem.maxStorage() <= 0) return;
        stack.getOrCreateTag().put("StorageExtension", ((Item) extensionItem).getDefaultInstance().serializeNBT());
        stack.getCapability(TCCapabilities.ECF).orElse(DummyECFHandler.instance).setMaxECF(extensionItem.maxStorage());
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return this.getCurrentExtension(itemStack).self();
    }
}
