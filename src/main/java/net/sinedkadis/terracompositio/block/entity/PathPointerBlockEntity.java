package net.sinedkadis.terracompositio.block.entity;

import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.custom.PathPointerBlock;
import net.sinedkadis.terracompositio.cfe.pp_network.PathPointerNetwork;
import net.sinedkadis.terracompositio.compat.jade.JadeTerraCompositioPlugin;
import net.sinedkadis.terracompositio.network.TCPackets;
import net.sinedkadis.terracompositio.network.packets.S2CHighLightNodesSync;
import net.sinedkadis.terracompositio.network.packets.S2CPlayerCfeContainerSync;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@ParametersAreNonnullByDefault
public class PathPointerBlockEntity extends TCBlockEntity implements Nameable, CFENetworkMember {

    public static final String RECEIVER_POS_TAG = "receiver_pos";
    public static final String EMITTER_POS_TAG = "emitter_pos";
    public static final String SENDER_POSES_TAG = "sender_poses";

    public float rotationYaw, rotationPitch, rotationRoll;

    public List<PPPart> parts = NonNullList.withSize(2, PPPart.NONE);

    @Setter
    private boolean updateScheduled = false;

    @Getter
    private BlockPos receiverPos = null;

    public static boolean validAngle(PathPointerBlockEntity be, Vec3 burstDir) {

        float yaw = be.rotationYaw;
        float pitch = be.rotationPitch;

        // куда смотрит блок
        Vec3 lookDir = new Vec3(0, 0, 1)
                .yRot(yaw)
                .xRot(pitch)
                .normalize();
        //Todo: fix
        double dot = burstDir.dot(lookDir);
        //return dot > 0;
        return true;
    }

    public void setReceiverPos(@Nullable BlockPos receiverPos) {
        this.receiverPos = receiverPos;
    }

    @Getter
    private final Set<BlockPos> senderPoses = new HashSet<>(){};

    @Getter
    private BlockPos outputPos = null;
    public void setOutputPos(@Nullable BlockPos emitterPos) {
        this.outputPos = emitterPos;
    }



    public PathPointerBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.PATH_POINTER_BE.get(), pos, state);
        if (state.getBlock() instanceof PathPointerBlock pathPointerBlock) {
            parts.set(0, pathPointerBlock.getBasePart());
        }
    }

    @Override
    public void addBEBehaviours(List<IBEBehaviour> list) {

    }

    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        super.tick(pLevel, pPos, pState);
        CFENetwork cfeNetworkInstance = TerraCompositioAPI.INSTANCE.getCFENetworkInstance();
        if (!pLevel.isClientSide) {
            boolean inCFENetwork = cfeNetworkInstance.isIn(pLevel, this);
            if (!inCFENetwork && !this.isRemoved()) {
                cfeNetworkInstance.fireCFENetworkEvent(this, NetworkAction.ADD);
            }
        }
        if (updateScheduled) {
            updateScheduled = false;
            if (parts.contains(PPPart.COLLECTOR)) {
                BlockPos emitterPos = this.getOutputPos();
                if (emitterPos == null) updatePPNetwork(this);
            }
            setChanged();
            pLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void scheduleMemberUpdate(CFENetworkMember updated) {
        PathPointerNetwork.INSTANCE.updateMembersAroundCollectors(this, updated);
    }

    @Override
    public void setRemoved() {
        TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(this, NetworkAction.REMOVE);
        super.setRemoved();
    }

    public static boolean ppWrenchInteraction(@Nullable Player pPlayer, LevelAccessor level, BlockPos clickedPos, ItemStack wrenchStack) {

        if (!(level.getBlockEntity(clickedPos) instanceof PathPointerBlockEntity clickedPPBE)) {
            return false;
        }

        if (pPlayer != null && pPlayer.isShiftKeyDown()) {
            rotatePP(clickedPPBE);
            return true;
        }

        CompoundTag tag = wrenchStack.getOrCreateTag();
        if (!tag.contains(RECEIVER_POS_TAG)) {
            storeToTag(pPlayer, clickedPos, tag);
            return true;
        }

        BlockPos storedPos = TCUtil.loadBlockPos(tag.getCompound(RECEIVER_POS_TAG));
        clearBindTags(tag);
        if (storedPos != null && storedPos.equals(clickedPos)) {
            clearAnyBindings(pPlayer, level, clickedPos);
            return true;
        }

        if (storedPos == null || !(level.getBlockEntity(storedPos) instanceof PathPointerBlockEntity storedPPBE)) {
            return false;
        }

        List<PPPart> clickedPartsList = clickedPPBE.parts;
        List<PPPart> storedPartsList = storedPPBE.parts;

        boolean isClickedSender = clickedPartsList.contains(PPPart.SENDER);
        boolean isClickedReceiver = clickedPartsList.contains(PPPart.RECEIVER);
        boolean isStoredSender = storedPartsList.contains(PPPart.SENDER);
        boolean isStoredReceiver = storedPartsList.contains(PPPart.RECEIVER);

        boolean forwardBind = isClickedReceiver && isStoredSender;
        boolean backwardBind = isClickedSender && isStoredReceiver;

        if (!(forwardBind || backwardBind)) {
            sendBindMessage(pPlayer, "item.terracompositio.flow_rotating_axe.bind_fail_incompatible");
            clearBindTags(tag);
            return false;
        }

        if (forwardBind) backwardBind = false;


        boolean distanceTest = clickedPos.closerThan(storedPos, 7);
        if (!distanceTest) {
            sendBindMessage(pPlayer, "item.terracompositio.flow_rotating_axe.bind_fail_too_far");
            return false;
        }

        if (forwardBind) {
            if (!bind(storedPPBE, clickedPPBE)) {
                TCUtil.message(pPlayer, Component.translatable("item.terracompositio.flow_rotating_axe.bind_fail_angle")
                        .withStyle(ChatFormatting.BOLD));
                return false;
            }
        }
        if (backwardBind) {
            if (!bind(clickedPPBE, storedPPBE)) {
                TCUtil.message(pPlayer, Component.translatable("item.terracompositio.flow_rotating_axe.bind_fail_angle")
                        .withStyle(ChatFormatting.BOLD));
                return false;
            }
        }

        storedPPBE.setChanged();
        clickedPPBE.setChanged();

        ((ServerLevel) level).sendBlockUpdated(storedPos, storedPPBE.getBlockState(), storedPPBE.getBlockState(), 3);
        ((ServerLevel) level).sendBlockUpdated(clickedPos, clickedPPBE.getBlockState(), clickedPPBE.getBlockState(), 3);

        storedPPBE.setUpdateScheduled(true);
        clickedPPBE.setUpdateScheduled(true);

        if (pPlayer != null) {
            TCUtil.message(pPlayer, Component.translatable("item.terracompositio.flow_rotating_axe.bind_success").withStyle(ChatFormatting.BOLD));
            wrenchStack.hurtAndBreak(1, pPlayer, player1 -> {
                assert player1 != null;
                player1.broadcastBreakEvent(InteractionHand.MAIN_HAND);
            });
            if (pPlayer instanceof ServerPlayer serverPlayer) {
                TCPackets.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new S2CHighLightNodesSync(clickedPPBE));
                TCPackets.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new S2CHighLightNodesSync(storedPPBE));
            }
        }
        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean bind(PathPointerBlockEntity firstPPBE, PathPointerBlockEntity secondPPBE) {

        BlockPos firstPos = firstPPBE.getBlockPos();
        BlockPos secondPos = secondPPBE.getBlockPos();

        Vec3 rotInput = calculateRot(firstPPBE.getSenderPoses(), secondPos, firstPos);
        if (rotInput == null) return false;


        Set<BlockPos> senderPoses = secondPPBE.getSenderPoses();
        Set<BlockPos> senderPosesCopy = new HashSet<>(senderPoses);
        if (senderPoses.contains(firstPos)) return false;
        senderPosesCopy.add(firstPos);

        Vec3 rotOutput = calculateRot(senderPosesCopy, secondPPBE.getReceiverPos(), secondPos);
        if (rotOutput == null) return false;


        setYawAndPitchFromRot(rotInput, firstPPBE);
        firstPPBE.setReceiverPos(secondPos);

        setYawAndPitchFromRot(rotOutput, secondPPBE);
        secondPPBE.getSenderPoses().add(firstPos);

        updatePPNetwork(firstPPBE);
        return true;
    }

    public static void updatePPNetwork(PathPointerBlockEntity pathPointerBlockEntity) {
        Level level = pathPointerBlockEntity.getLevel();
        if (level != null) {
            Queue<PathPointerBlockEntity> queue = new LinkedList<>();
            queue.add(pathPointerBlockEntity);
            while (!queue.isEmpty()) {
                PathPointerBlockEntity current = queue.poll();
                if (current.parts.contains(PPPart.EMITTER)) {
                    Set<PathPointerBlockEntity> collectors = findAvailableCollectors(level, pathPointerBlockEntity);
                    for (PathPointerBlockEntity collector : collectors) {
                        PathPointerNetwork.INSTANCE.firePPNetworkEvent(Pair.of(current, collector), NetworkAction.ADD);
                    }
                    break;
                }
                if (current.parts.contains(PPPart.SENDER)) {
                    BlockPos bindPos = current.getReceiverPos();
                    if (bindPos == null)
                        break;
                    BlockEntity blockEntity = level.getBlockEntity(bindPos);
                    if (blockEntity instanceof PathPointerBlockEntity ppBE) {
                        queue.add(ppBE);
                    }
                }
            }
        }
    }

    private static Set<PathPointerBlockEntity> findAvailableCollectors(Level level, PathPointerBlockEntity emitterBE) {
        Queue<PathPointerBlockEntity> queue = new LinkedList<>();
        queue.add(emitterBE);
        Set<PathPointerBlockEntity> toReturn = new HashSet<>();
        while (!queue.isEmpty()) {
            PathPointerBlockEntity current = queue.poll();
            if (current.parts.contains(PPPart.COLLECTOR)) {
                toReturn.add(emitterBE);
                continue;
            }
            if (current.parts.contains(PPPart.RECEIVER)) {
                queue.addAll(current.getSenderPoses().stream()
                        .map(level::getBlockEntity)
                        .map(blockEntity -> blockEntity instanceof PathPointerBlockEntity ppBE ? ppBE : null)
                        .filter(Objects::nonNull)
                        .toList());

            }
        }
        return toReturn.stream()
                .filter(ppBE -> ppBE.getOutputPos() == null)
                .collect(Collectors.toSet());
    }

    private static void setYawAndPitchFromRot(Vec3 dir, PathPointerBlockEntity be) {
        // yaw: 0 = +Z (south)
        double yaw = Math.atan2(dir.x, dir.z);

        // горизонтальная длина
        double h = Math.sqrt(dir.x * dir.x + dir.z * dir.z);

        // pitch: вверх +
        double pitch = Math.atan2(dir.y, h);

        be.rotationYaw = (float) Math.toDegrees(yaw);
        be.rotationPitch = (float) Math.toDegrees(pitch);
    }

    private static void clearAnyBindings(@Nullable Player pPlayer, LevelAccessor level, BlockPos inputPos) {
        sendBindMessage(pPlayer, "item.terracompositio.flow_rotating_axe.bind_cleared");
        PathPointerBlockEntity be = ((PathPointerBlockEntity) level.getBlockEntity(inputPos));
        if (be != null) {
            be.rotationPitch = 90;
            be.rotationYaw = 0;
            //be.rotationRoll = 0;

            BlockPos emitterPos = be.getOutputPos();
            if (emitterPos != null) {
                PathPointerBlockEntity emitter = (PathPointerBlockEntity) level.getBlockEntity(emitterPos);
                if (emitter != null)
                    PathPointerNetwork.INSTANCE.firePPNetworkEvent(Pair.of(emitter, be), NetworkAction.REMOVE);

                be.setOutputPos(null);
            }
            BlockPos receiverPos = be.getReceiverPos();
            if (receiverPos != null) {
                PathPointerBlockEntity receiver = (PathPointerBlockEntity) level.getBlockEntity(receiverPos);
                if (receiver != null)
                    receiver.getSenderPoses().remove(be.getPos());

                be.setReceiverPos(null);
            }

            be.getSenderPoses().forEach(senderPos -> {
                PathPointerBlockEntity sender = (PathPointerBlockEntity) level.getBlockEntity(senderPos);
                if (sender != null)
                    sender.setReceiverPos(null);
            });
            be.getSenderPoses().clear();

            be.setUpdateScheduled(true);
            if (pPlayer instanceof ServerPlayer serverPlayer) {
                TCPackets.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new S2CHighLightNodesSync(be));
            }
        }

    }

    private static void storeToTag(@Nullable Player pPlayer, BlockPos pos, CompoundTag tag) {
        tag.put(RECEIVER_POS_TAG, TCUtil.saveBlockPos(pos));

        if (pPlayer != null) {
            TCUtil.message(pPlayer, Component.translatable("item.terracompositio.flow_rotating_axe.bind_begin").withStyle(ChatFormatting.BOLD));
        }
    }

    private static void rotatePP(PathPointerBlockEntity pathPointerBlockEntity) {
        if (pathPointerBlockEntity.getLevel() != null) {
            pathPointerBlockEntity.rotationRoll += 22.5F;
            if (pathPointerBlockEntity.rotationRoll > 360)
                pathPointerBlockEntity.rotationRoll -= 360;
            BlockState blockState = pathPointerBlockEntity.getBlockState();
            pathPointerBlockEntity.getLevel().sendBlockUpdated(
                    pathPointerBlockEntity.worldPosition,
                    blockState,
                    blockState,
                    3);
        }
    }

    public static void sendBindMessage(@Nullable Player player, String messageKey) {
        if (player != null) {
            TCUtil.message(player, Component.translatable(messageKey).withStyle(ChatFormatting.BOLD));
        }
    }

    public static void clearBindTags(CompoundTag tag) {
        tag.remove(RECEIVER_POS_TAG);
    }

    @Nullable
    private static Vec3 calculateRot(
            Set<BlockPos> receiverSenderPoses,
            @Nullable BlockPos senderBindPos,
            BlockPos origin
    ) {
        // --- bind → вперёд
        Vec3 toBind = null;
        if (senderBindPos != null) {
            toBind = Vec3.atCenterOf(senderBindPos.subtract(origin)).normalize();
        }

        // --- receivers → наружу
        List<Vec3> toReceivers = receiverSenderPoses.stream()
                .map(p -> Vec3.atCenterOf(p.subtract(origin)).normalize())
                .toList();

        // --- ВАЛИДАЦИЯ:
        // если receiver и bind смотрят в одну полусферу → хуйня
        if (toBind != null) {
            for (Vec3 r : toReceivers) {
                double dot = r.dot(toBind);
                if (dot > 0) { // угол < 90°
                    return null;
                }
            }
        }

        // --- если receivers нет — строго на bind
        if (toReceivers.isEmpty()) {
            return toBind;
        }

        // --- среднее receivers → смотрим ОТ них
        Vec3 receiversAvg = Vec3.ZERO;
        for (Vec3 r : toReceivers) {
            receiversAvg = receiversAvg.add(r);
        }
        receiversAvg = receiversAvg.normalize().scale(-1);

        // --- тянем к bind
        Vec3 lookDir = receiversAvg;
        if (toBind != null) {
            lookDir = lookDir.add(toBind).normalize();
        }

        // --- финальная проверка: receivers точно сзади
        for (Vec3 r : toReceivers) {
            if (lookDir.dot(r) > 0) {
                return null;
            }
        }

        // --- bind точно спереди
        if (toBind != null && lookDir.dot(toBind) <= 0) {
            return null;
        }

        return lookDir;
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.putFloat("rot_y", rotationYaw);
        pTag.putFloat("rot_x", rotationPitch);
        pTag.putFloat("rot_z", rotationRoll);

        pTag.putInt("part0", parts.get(0).ordinal());
        pTag.putInt("part1", parts.get(1).ordinal());

        if (receiverPos != null)
            pTag.put(RECEIVER_POS_TAG, TCUtil.saveBlockPos(receiverPos));
        if (outputPos != null)
            pTag.put(EMITTER_POS_TAG, TCUtil.saveBlockPos(outputPos));
        saveFromSetToTag(pTag,SENDER_POSES_TAG, senderPoses);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        rotationYaw = pTag.getFloat("rot_y");
        rotationPitch = pTag.getFloat("rot_x");
        rotationRoll = pTag.getFloat("rot_z");

        parts = NonNullList.withSize(2, PPPart.NONE);
        parts.set(0, PPPart.fromOrdinal(pTag.getInt("part0")));
        parts.set(1, PPPart.fromOrdinal(pTag.getInt("part1")));

        receiverPos = TCUtil.loadBlockPos(pTag.getCompound(RECEIVER_POS_TAG));

        outputPos = TCUtil.loadBlockPos(pTag.getCompound(EMITTER_POS_TAG));

        loadFromTagToSet(pTag,SENDER_POSES_TAG, senderPoses);

        setUpdateScheduled(true);
    }

    public static void saveFromSetToTag(CompoundTag pTag,String tag, Set<BlockPos> senderPoses) {
        ListTag listTag = new ListTag();
        listTag.addAll(senderPoses.stream().map(TCUtil::saveBlockPos).toList());
        pTag.put(tag, listTag);
    }

    public static void loadFromTagToSet(CompoundTag pTag, String tag, Set<BlockPos> senderPoses) {
        ListTag tagList = pTag.getList(tag, CompoundTag.TAG_COMPOUND);
        senderPoses.addAll(tagList.stream().map(TCUtil::loadBlockPos).toList());
    }

    @Override
    public void onAppendServerData(CompoundTag compoundTag) {
        super.onAppendServerData(compoundTag);
        //sender
        BlockPos receiverPos = getReceiverPos();
        if (receiverPos != null)
            compoundTag.put("bindPos", TCUtil.saveBlockPos(receiverPos));
        //receiver
        PathPointerBlockEntity.saveFromSetToTag(compoundTag,PathPointerBlockEntity.SENDER_POSES_TAG,getSenderPoses());
    }

    @Override
    public void onAppendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        super.onAppendTooltip(iTooltip, blockAccessor, iPluginConfig);
        CompoundTag serverData = blockAccessor.getServerData();
        //sender
        if (serverData.contains("bindPos") && iPluginConfig.get(JadeTerraCompositioPlugin.debugConfigRL())) {
            BlockPos pos = TCUtil.loadBlockPos(serverData.getCompound("bindPos"));
            iTooltip.add(Component.literal("ReceiverPos: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
        }
        //receiver
        if (iPluginConfig.get(JadeTerraCompositioPlugin.debugConfigRL())) {
            Set<BlockPos> senderPoses = new HashSet<>();
            PathPointerBlockEntity.loadFromTagToSet(serverData,PathPointerBlockEntity.SENDER_POSES_TAG, senderPoses);
            int i = 1;
            for (BlockPos pos : senderPoses) {
                iTooltip.add(Component.literal("SenderPos " + i + ": " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
                i = i + 1;
            }
        }
    }

    public void highlightNodes() {
        if (level != null && level.isClientSide) {
            if (receiverPos != null) {
                addParticle(level,receiverPos, ParticleTypes.FLAME);
            }
            if (outputPos != null) {
                addParticle(level, outputPos, ParticleTypes.WAX_OFF);
            }

            senderPoses.forEach(blockpos ->
            {
                if (blockpos == null) return;
                addParticle(level,blockpos, ParticleTypes.SOUL_FIRE_FLAME);
            });
        }
    }

    private static void addParticle(Level level,BlockPos blockPos, SimpleParticleType particleType) {
        for (int i = 0; i < 3; i++) {
            double x = blockPos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.5;
            double y = blockPos.getY() + 0.5 + (level.random.nextDouble() - 0.5) * 0.5;
            double z = blockPos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.5;


            level.addParticle(particleType, x, y, z,
                    0, 0, 0);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        rotationYaw = tag.getFloat("rot_y");
        rotationPitch = tag.getFloat("rot_x");
        rotationRoll = tag.getFloat("rot_z");
    }

    @Override
    public @NotNull Component getName() {
        return Component.translatable("block.terracompositio.path_pointer");
    }

    @Override
    public @Nullable Component getCustomName() {
        return Component.translatable("block.terracompositio.path_pointer");
    }


    @Override
    public int getRange() {
        return 0;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public ICFEHandler getMainHandler() {
        return DummyCFEHandler.instanceWithSpeed5;
    }

    @Override
    public <T> T getEntity() {
        //noinspection unchecked
        return ((T) this);
    }

    @Override
    public BlockPos getPos() {
        return this.getBlockPos();
    }

    @Override
    public void updateIfScheduled() {

    }

    public void scheduleMemberUpdate() {

    }




    public enum PPPart implements StringRepresentable {
        COLLECTOR(true),
        RECEIVER(true),
        SENDER(false),
        EMITTER(false),
        INFUSER(false),
        EXTRACTOR(true),
        NONE(false);

        @Getter
        private final boolean input;


        PPPart(boolean isInput) {
            this.input = isInput;
        }

        @Override
        public String toString() {
            return "PPPart{" +
                    "name='" + getSerializedName() + '\'' +
                    '}';
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name().toLowerCase();
        }

        public static PPPart fromOrdinal(int ordinal) {
            for (PPPart part : PPPart.values()) {
                if (part.ordinal() == ordinal) return part;
            }
            return PPPart.NONE;
        }
    }
}
