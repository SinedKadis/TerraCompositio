package net.sinedkadis.terracompositio.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.cfe.CFEItemWrapper;
import net.sinedkadis.terracompositio.cfe.CFEMemberProxy;
import net.sinedkadis.terracompositio.item.models.TechnetiumBootsModel;
import net.sinedkadis.terracompositio.item.models.TechnetiumCloakModel;
import net.sinedkadis.terracompositio.item.models.TechnetiumCrownModel;
import net.sinedkadis.terracompositio.network.TCPackets;
import net.sinedkadis.terracompositio.network.packets.C2SBoardSync;
import net.sinedkadis.terracompositio.registries.TCArmorMaterials;
import net.sinedkadis.terracompositio.registries.TCBlocks;
import net.sinedkadis.terracompositio.registries.TCItems;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;
import static net.sinedkadis.terracompositio.item.custom.WrenchAxeItem.isPlayerLookingAtBlock;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Mod.EventBusSubscriber(modid = TerraCompositio.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TechnetiumArmorItem extends TCArmorItem {

    public static final String crownModeTag = "crown_mode";
    private static final String cachedExtractorsTag = "cached_extractors";


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
        transferCFE(pStack, entity, icfeHandler);
        switch (type) {
            case HELMET -> this.helmetInventoryTick(pStack, pLevel, entity, icfeHandler);
            case CHESTPLATE -> this.chestplateInventoryTick(pStack, pLevel, entity, icfeHandler);
            case LEGGINGS -> this.leggingsInventoryTick(pStack, pLevel, entity, icfeHandler);
            case BOOTS -> this.bootInventoryTick(pStack, pLevel, entity, icfeHandler);

            default -> {
            }

        }


    }

    private void helmetInventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, ICFEHandler icfeHandlerCFE) {
        switch (getCrownMode(pStack)) {
            case SEND,ALL -> {
                if (!(pEntity instanceof CFENetworkMember pMember)) return;

                if (icfeHandlerCFE.getCFE() > 0) {
                    getCachedExtractors(pStack).stream()
                            //Convert BlockPoses to PPBlockEntities
                            .map(pLevel::getBlockEntity)
                            .map(blockEntity -> blockEntity instanceof PathPointerBlockEntity ppbe ? ppbe : null)
                            .filter(Objects::nonNull)
                            //Pick random ppbe
                            .findAny()
                            .ifPresent(inputEntity ->  {
                                BlockEntity outputEntity = pLevel.getBlockEntity(inputEntity.getOutputPos());
                                //Search targets from output positions with entity member data
                                TerraCompositioAPI.instance().getCFENetworkInstance().getAvailableNetworkTargets(
                                        new CFEMemberProxy(pMember, (PathPointerBlockEntity) outputEntity))
                                        .forEach(cfeNetworkMember ->
                                                //Try to transfer to input position to the target
                                                TCUtil.tryCFETransfer(
                                                        new CFEMemberProxy(cfeNetworkMember,inputEntity),
                                                        pMember));
                            });
                }
            }
        }
    }

    public static Set<BlockPos> getCachedExtractors(ItemStack itemStack) {
        CompoundTag persistentData = itemStack.getOrCreateTag();
        ListTag list = persistentData.getList(cachedExtractorsTag, Tag.TAG_COMPOUND);
        return list.stream()
                .map(TCUtil::loadBlockPos)
                .collect(Collectors.toSet());
    }

    private void chestplateInventoryTick(ItemStack ignoredPStack, Level ignoredPLevel, Entity ignoredEntity, ICFEHandler ignoredIcfeHandler) {

    }

    private void leggingsInventoryTick(ItemStack ignoredPStack, Level ignoredPLevel, Entity ignoredEntity, ICFEHandler ignoredIcfeHandler) {

    }

    private void bootInventoryTick(ItemStack ignoredPStack, Level ignoredPLevel, Entity ignoredEntity, ICFEHandler ignoredIcfeHandler) {
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        Level pLevel = livingEntity.level();
        //if (pLevel.isClientSide) return;
        BlockPos onPos = BlockPos.containing(livingEntity.position().add(0,-1,0));
        CompoundTag persistentData = livingEntity.getPersistentData();
        String blockPosBelow = "BlockPosBelow";
        String height = "Height";

        ItemStack stack = livingEntity.getItemBySlot(EquipmentSlot.FEET);

        ICFEHandler handler = stack.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance);

        BlockState blockStateOn = pLevel.getBlockState(onPos);

        if (livingEntity.fallDistance > 3 && !livingEntity.isShiftKeyDown()
                && handler.getCFE() >= 1) {

            BlockState blockStateOn1 = pLevel.getBlockState(onPos.below(3));

            if (blockStateOn.is(BlockTags.REPLACEABLE)
//                    && !persistentData.contains(blockPosBelow)
                    && !blockStateOn1.isAir()) {
                persistentData.putLong(blockPosBelow, onPos.asLong());
                persistentData.putInt(height,onPos.getY()-1);
            }
        }


        if (!stack.is(TCItems.TECHNETIUM_BOOTS.get())
                || handler instanceof DummyCFEHandler
                || handler.getCFE() < 1) {
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

        if (persistentData.contains(blockPosBelow)) {
            if (!livingEntity.isShiftKeyDown()) {
                BlockPos last = BlockPos.of(persistentData.getLong(blockPosBelow));
                int h = persistentData.getInt(height);
                BlockPos target = onPos.atY(h);
                BlockState boardState = TCBlocks.TECHNETIUM_BOARD.get().defaultBlockState().setValue(WATERLOGGED,
//                        blockStateOn.getFluidState().is(Fluids.WATER));
                        false);
                BlockState lastBlockState = pLevel.getBlockState(last);
//                BlockState replaceState = lastBlockState.hasProperty(WATERLOGGED)
//                        && lastBlockState.getValue(WATERLOGGED)
//                        ? Blocks.WATER.defaultBlockState()
//                        : Blocks.AIR.defaultBlockState();
                BlockState replaceState = Blocks.AIR.defaultBlockState();
                BlockState targetState = pLevel.getBlockState(target);
                if (!target.equals(last)) {
                    persistentData.putLong(blockPosBelow, target.asLong());
                    if (targetState.is(BlockTags.REPLACEABLE)) {
                        if (pLevel.isClientSide) {
                            pLevel.destroyBlock(target, true);
                            pLevel.setBlockAndUpdate(target, boardState);
                            TCPackets.CHANNEL.send(PacketDistributor.SERVER.noArg(),new C2SBoardSync(target,last));
                        } else {
                            handler.takeCFE(1, false);
                        }
                    }
                    if (lastBlockState.is(TCBlocks.TECHNETIUM_BOARD.get())) {
                        pLevel.setBlock(last, replaceState,0);
                    }
                }
            }
        }
    }



    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity livingEntity = event.getEntity();
        Level pLevel = livingEntity.level();
        //if (pLevel.isClientSide) return;
        ItemStack stack = livingEntity.getItemBySlot(EquipmentSlot.FEET);
        if (stack.is(TCItems.TECHNETIUM_BOOTS.get())){
            CompoundTag persistentData = livingEntity.getPersistentData();
            String blockPosBelow = "BlockPosBelow";
            String height = "Height";
            String cd = "cd";

            BlockPos onPos = BlockPos.containing(
                    livingEntity.position().add(0,-1,0)
            );


            BlockPos.MutableBlockPos mutableBlockPos = onPos.mutable();
            ICFEHandler handler = stack.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance);
            if (handler instanceof DummyCFEHandler || handler.getCFE() < 1) return;
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
            ICFEHandler playerCap = entity.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance);
            if (!type.equals(Type.LEGGINGS)) {
                ICFEHandler leggings = DummyCFEHandler.instance;
                for (ItemStack armor : entity.getArmorSlots()) {
                    if (armor.getItem() instanceof ArmorItem armorItem
                            && armorItem.getType().equals(Type.LEGGINGS)) {
                        leggings = armor.getCapability(TCCapabilities.CFE).orElse(DummyCFEHandler.instance);
                    }
                }
                if (cfe < 1) {
                    if (leggings.takeCFE(1,true)>0) {
                        int cfe1 = leggings.takeCFE(1, false);
                        icfeHandler.addCFE(
                                cfe1,false);
                    } else if (playerCap.takeCFE(1,true)>0) {
                        icfeHandler.addCFE(
                                playerCap.takeCFE(1,false), false);
                    }
                }
            } else {
                if (playerCap.takeCFE(icfeHandler.getFreeSpace(),true)>0) {
                    icfeHandler.addCFE(
                            playerCap.takeCFE(icfeHandler.getFreeSpace(),false),false);
                }
            }
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
        return (ICapabilityProvider) new CFEItemWrapper(stack)
                .setMaxCFE(switch (type) {
            case LEGGINGS -> 100;
            case HELMET -> 10;
            default -> 1;
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);
        if (pPlayer.isCrouching() && !isPlayerLookingAtBlock(pPlayer,pLevel)) {
            setCrownMode(stack,getCrownMode(stack).next());
            if (!stack.hasTag()) {
                stack.setTag(new CompoundTag());
            }
            if (stack.getTag() != null) {
                stack.getTag().putInt(crownModeTag, getCrownMode(stack).ordinal());
            }
            pPlayer.displayClientMessage(Component.translatable("message.terracompositio.changed_tool_mode",
                    getCrownMode(stack).getDisplayName()), true);
            return InteractionResultHolder.sidedSuccess(stack,pLevel.isClientSide);
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }
    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        CrownMode mode = CrownMode.fromOrdinal(pStack.getOrCreateTag().getInt(crownModeTag));
        pTooltipComponents.add(Component.translatable("item.terracompositio.tool_mode", mode.getDisplayName()).withStyle(ChatFormatting.GRAY));
    }

    public static CrownMode getCrownMode(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains(crownModeTag)) {
            return CrownMode.fromOrdinal(tag.getInt(crownModeTag));
        }
        return CrownMode.NONE;
    }

    public static void setCrownMode(ItemStack stack, CrownMode mode) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(crownModeTag, mode.ordinal());
    }
    public enum CrownMode {
        NONE,RECEIVE,SEND,ALL;

        public Component getDisplayName() {
            return Component.translatable("item.terracompositio.technetium_crown." + name().toLowerCase());
        }
        public CrownMode next() {
            return values()[(this.ordinal() + 1) % values().length];
        }
        public static CrownMode fromOrdinal(int ordinal) {
            return values()[ordinal % values().length];
        }

    }
}
