package net.sinedkadis.terracompositio.block.entity;

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
import net.sinedkadis.terracompositio.compat.jade.JadeTerraCompositioPlugin;
import net.sinedkadis.terracompositio.network.TCPackets;
import net.sinedkadis.terracompositio.network.packets.S2CHighLightNodesSync;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.util.BindException;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Slf4j
@ParametersAreNonnullByDefault
public class PathPointerBlockEntity extends TCBlockEntity implements Nameable, CFENetworkMember {

    public static final String RECEIVER_POS_TAG = "receiver_pos";
    public static final String OUTPUT_POS_TAG = "output_pos";
    public static final String SENDER_POSES_TAG = "sender_poses";
    public static final String INPUT_POSES_TAG = "input_poses";

    public float rotationYaw, rotationPitch, rotationRoll;

    public List<PPPart> parts = NonNullList.withSize(2, PPPart.NONE);

    @Setter
    private boolean updateScheduled = false;

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

    private final Set<BlockPos> senderPoses = new HashSet<>() {
    };

    private BlockPos outputPos = null;

    public void setOutputPos(@Nullable BlockPos emitterPos) {
        this.outputPos = emitterPos;
    }

    private final Set<BlockPos> inputPoses = new HashSet<>();


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
            setChanged();
            pLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void setRemoved() {
        TerraCompositioAPI.INSTANCE.getCFENetworkInstance().fireCFENetworkEvent(this, NetworkAction.REMOVE);
        assert this.level != null;
        if (!this.level.getBlockState(this.getBlockPos()).equals(this.getBlockState())) {
            clearAnyBindings(null,this);
        }
        super.setRemoved();
    }

    public static boolean ppWrenchInteraction(@Nullable Player pPlayer, LevelAccessor level, BlockPos clickedPos, ItemStack wrenchStack) {

        try {
            wrenchInteraction(pPlayer, level, clickedPos, wrenchStack);
            if (pPlayer != null) {
                TCUtil.message(pPlayer, Component.translatable("item.terracompositio.flow_rotating_axe.bind_success").withStyle(ChatFormatting.BOLD));
                wrenchStack.hurtAndBreak(1, pPlayer, player1 -> {
                    assert player1 != null;
                    player1.broadcastBreakEvent(InteractionHand.MAIN_HAND);
                });
            }
            return true;
        } catch (BindException e) {
            String message = e.getMessage();
            if (!message.equals(BindException.emptyMessage))
                sendBindMessage(pPlayer, message);
            return false;
        }


    }

    private static void wrenchInteraction(@Nullable Player pPlayer, LevelAccessor level, BlockPos clickedPos, ItemStack wrenchStack) throws BindException {
        PathPointerBlockEntity clickedPPBE = (PathPointerBlockEntity) level.getBlockEntity(clickedPos);
        assert clickedPPBE != null;

        tryRotate(pPlayer, clickedPPBE);

        CompoundTag tag = wrenchStack.getOrCreateTag();

        tryStorePosToTag(pPlayer, clickedPos, tag);

        BlockPos storedPos = TCUtil.loadBlockPos(tag.getCompound(RECEIVER_POS_TAG));
        assert storedPos != null;

        clearBindTags(tag);

        tryClearBindings(pPlayer, level, clickedPos, storedPos);

        PathPointerBlockEntity storedPPBE = (PathPointerBlockEntity) level.getBlockEntity(storedPos);
        assert storedPPBE != null;

        List<PPPart> clickedPartsList = clickedPPBE.parts;
        List<PPPart> storedPartsList = storedPPBE.parts;

        boolean isClickedSender = clickedPartsList.contains(PPPart.SENDER);
        boolean isClickedReceiver = clickedPartsList.contains(PPPart.RECEIVER);
        boolean isStoredSender = storedPartsList.contains(PPPart.SENDER);
        boolean isStoredReceiver = storedPartsList.contains(PPPart.RECEIVER);

        boolean forwardBind = isClickedReceiver && isStoredSender;
        boolean backwardBind = isClickedSender && isStoredReceiver;

        if (!(forwardBind || backwardBind)) {
            clearBindTags(tag);
            throw new BindException("item.terracompositio.flow_rotating_axe.bind_fail_incompatible");
        }

        if (forwardBind) backwardBind = false;

        if (!clickedPos.closerThan(storedPos, 7)) {
            throw new BindException("item.terracompositio.flow_rotating_axe.bind_fail_too_far");
        }

        PathPointerBlockEntity firstPPBE = null;
        PathPointerBlockEntity secondPPBE = null;

        if (forwardBind) {
            firstPPBE = storedPPBE;
            secondPPBE = clickedPPBE;
        }
        if (backwardBind) {
            firstPPBE = clickedPPBE;
            secondPPBE = storedPPBE;
        }

        tryBind(firstPPBE, secondPPBE);

        fullUpdateBE(pPlayer, (ServerLevel) level,firstPPBE);
        fullUpdateBE(pPlayer, (ServerLevel) level,secondPPBE);

        PathPointerBlockEntity outputPPBE = getOutputOf(secondPPBE);
        Set<PathPointerBlockEntity> inputs = getInputOf(firstPPBE);

        tryBindInputsAndOutput(inputs, outputPPBE);

        inputs.forEach(inputPPBE -> fullUpdateBE(pPlayer, (ServerLevel) level, inputPPBE));
        fullUpdateBE(pPlayer, (ServerLevel) level, outputPPBE);
    }

    private static void tryBindInputsAndOutput(Set<PathPointerBlockEntity> inputs,@Nullable PathPointerBlockEntity outputPPBE) {
        if (outputPPBE != null && !inputs.isEmpty()) {
            inputs.forEach(inputPPBE -> {
                inputPPBE.setOutputPos(outputPPBE.getBlockPos());
                outputPPBE.getInputPoses().add(inputPPBE.getBlockPos());
            });
        }
    }

    private static void fullUpdateBE(@Nullable Player pPlayer, ServerLevel level, @Nullable PathPointerBlockEntity inputPPBE) {
        if (inputPPBE == null) return;
        inputPPBE.setChanged();
        level.sendBlockUpdated(inputPPBE.getBlockPos(), inputPPBE.getBlockState(), inputPPBE.getBlockState(), 3);
        inputPPBE.setUpdateScheduled(true);
        updateClientHighLight(pPlayer, inputPPBE);
    }

    private static void tryClearBindings(@Nullable Player pPlayer, LevelAccessor level, BlockPos clickedPos, BlockPos storedPos) throws BindException {
        if (storedPos.equals(clickedPos)) {
            clearAnyBindings(pPlayer, level, clickedPos);
            throw new BindException("item.terracompositio.flow_rotating_axe.bind_cleared");
        }
    }

    private static void tryStorePosToTag(@Nullable Player pPlayer, BlockPos clickedPos, CompoundTag tag) throws BindException {
        if (!tag.contains(RECEIVER_POS_TAG)) {
            storeToTag(pPlayer, clickedPos, tag);
            throw new BindException();
        }
    }

    private static void tryRotate(@Nullable Player pPlayer, PathPointerBlockEntity clickedPPBE) throws BindException {
        if (pPlayer != null && pPlayer.isShiftKeyDown()) {
            rotatePP(clickedPPBE);
            throw new BindException();
        }
    }

    private static void tryBind(PathPointerBlockEntity firstPPBE, PathPointerBlockEntity secondPPBE) throws BindException {
        BindException angleException = new BindException("item.terracompositio.flow_rotating_axe.bind_fail_angle");

        BlockPos firstPos = firstPPBE.getBlockPos();
        BlockPos secondPos = secondPPBE.getBlockPos();

        Vec3 rotInput = calculateRot(firstPPBE.getSenderPoses(), secondPos, firstPos);

        if (rotInput == null) throw angleException;


        Set<BlockPos> senderPoses = secondPPBE.getSenderPoses();
        Set<BlockPos> senderPosesCopy = new HashSet<>(senderPoses);
        senderPosesCopy.add(firstPos);

        Vec3 rotOutput = calculateRot(senderPosesCopy, secondPPBE.getReceiverPos(), secondPos);
        if (rotOutput == null) throw angleException;


        setYawAndPitchFromRot(rotInput, firstPPBE);
        firstPPBE.setReceiverPos(secondPos);

        setYawAndPitchFromRot(rotOutput, secondPPBE);
        secondPPBE.getSenderPoses().add(firstPos);
    }

    private static @Nullable PathPointerBlockEntity getOutputOf(PathPointerBlockEntity origin) {
        Level level = origin.level;
        if (level == null) return null;
        BlockPos.MutableBlockPos mutableBlockPos = origin.getBlockPos().mutable();
        if (level.getBlockEntity(mutableBlockPos) == null) {
            if (origin.parts.contains(PPPart.EMITTER) || origin.parts.contains(PPPart.INFUSER)) {
                return origin;
            } else {
                BlockPos originOutputPos = origin.getReceiverPos();
                if (originOutputPos != null)
                    mutableBlockPos.set(originOutputPos);
            }
        }
        while (true) {
            BlockEntity blockEntity = level.getBlockEntity(mutableBlockPos);
            if (blockEntity instanceof PathPointerBlockEntity pathPointerBlockEntity) {
                if (pathPointerBlockEntity.parts.contains(PPPart.EMITTER) || pathPointerBlockEntity.parts.contains(PPPart.INFUSER)) {
                    return pathPointerBlockEntity;
                }
                if (pathPointerBlockEntity.parts.contains(PPPart.SENDER)) {
                    BlockPos receiverPos = pathPointerBlockEntity.getReceiverPos();
                    if (receiverPos != null) {
                        mutableBlockPos.set(receiverPos);
                        continue;
                    }
                }
            }
            return null;
        }
    }

    private static Set<PathPointerBlockEntity> getInputOf(PathPointerBlockEntity firstPPBE) {
        Level level = firstPPBE.level;
        if (level == null) return Set.of();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(firstPPBE.getBlockPos());
        queue.addAll(firstPPBE.senderPoses);
        Set<PathPointerBlockEntity> toReturn = new HashSet<>();
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            BlockEntity blockEntity = level.getBlockEntity(current);
            if (blockEntity instanceof PathPointerBlockEntity pathPointerBlockEntity) {
                if (pathPointerBlockEntity.parts.contains(PPPart.COLLECTOR) || pathPointerBlockEntity.parts.contains(PPPart.EXTRACTOR)) {
                    toReturn.add(pathPointerBlockEntity);
                    continue;
                }
                if (pathPointerBlockEntity.parts.contains(PPPart.RECEIVER)) {
                    queue.addAll(pathPointerBlockEntity.getSenderPoses());
                }
            }
        }
        return toReturn;
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

    public static void clearAnyBindings(@Nullable Player pPlayer, LevelAccessor level, BlockPos blockPos) {
        clearAnyBindings(pPlayer, (PathPointerBlockEntity) level.getBlockEntity(blockPos));
    }
    public static void clearAnyBindings(@Nullable Player pPlayer, @Nullable PathPointerBlockEntity be) {
        if (be != null) {
            Level level = be.level;
            if (level == null) return;
            be.rotationPitch = 90;
            be.rotationYaw = 0;
            //be.rotationRoll = 0;

            Set<PathPointerBlockEntity> inputs = getInputOf(be);
            inputs.forEach(inputPPBE -> {
                if (inputPPBE != null) {
                    inputPPBE.setOutputPos(null);
                    updateClientHighLight(pPlayer, inputPPBE);
                }
            });


            PathPointerBlockEntity output = getOutputOf(be);
            if (output != null) {
                inputs.add(be);
                output.getInputPoses().removeAll(inputs.stream().map(BlockEntity::getBlockPos).collect(Collectors.toSet()));
                inputs.forEach(input -> updateClientHighLight(pPlayer, input));
                be.setOutputPos(null);
                updateClientHighLight(pPlayer, output);
            }
            be.getInputPoses().clear();

            BlockPos receiverPos = be.getReceiverPos();
            if (receiverPos != null) {
                PathPointerBlockEntity receiver = (PathPointerBlockEntity) level.getBlockEntity(receiverPos);
                if (receiver != null) {
                    receiver.getSenderPoses().remove(be.getPos());
                    updateClientHighLight(pPlayer, receiver);
                }

                be.setReceiverPos(null);
            }

            be.getSenderPoses().forEach(senderPos -> {
                PathPointerBlockEntity sender = (PathPointerBlockEntity) level.getBlockEntity(senderPos);
                if (sender != null) {
                    sender.setReceiverPos(null);
                    updateClientHighLight(pPlayer, sender);
                }
            });
            be.getSenderPoses().clear();



            be.setUpdateScheduled(true);
            updateClientHighLight(pPlayer, be);
        }

    }

    private static void updateClientHighLight(@Nullable Player pPlayer, PathPointerBlockEntity pathPointerBlockEntity) {
        if (pPlayer instanceof ServerPlayer serverPlayer) {
            TCPackets.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new S2CHighLightNodesSync(pathPointerBlockEntity));
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
            pTag.put(OUTPUT_POS_TAG, TCUtil.saveBlockPos(outputPos));
        saveFromSetToTag(pTag, SENDER_POSES_TAG, senderPoses);
        saveFromSetToTag(pTag, INPUT_POSES_TAG, inputPoses);
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

        outputPos = TCUtil.loadBlockPos(pTag.getCompound(OUTPUT_POS_TAG));

        loadFromTagToSet(pTag, SENDER_POSES_TAG, senderPoses);
        loadFromTagToSet(pTag, INPUT_POSES_TAG, inputPoses);


        setUpdateScheduled(true);
    }

    public static void saveFromSetToTag(CompoundTag pTag, String tag, Set<BlockPos> senderPoses) {
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

        BlockPos receiverPos = getReceiverPos();
        if (receiverPos != null)
            compoundTag.put(RECEIVER_POS_TAG, TCUtil.saveBlockPos(receiverPos));
        BlockPos outputPos = getOutputPos();
            compoundTag.put(OUTPUT_POS_TAG, TCUtil.saveBlockPos(outputPos));

        PathPointerBlockEntity.saveFromSetToTag(compoundTag, PathPointerBlockEntity.SENDER_POSES_TAG, getSenderPoses());
        PathPointerBlockEntity.saveFromSetToTag(compoundTag, PathPointerBlockEntity.INPUT_POSES_TAG, getInputPoses());
    }

    @Override
    public void onAppendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        super.onAppendTooltip(iTooltip, blockAccessor, iPluginConfig);
        CompoundTag serverData = blockAccessor.getServerData();

        if (serverData.contains(RECEIVER_POS_TAG) && iPluginConfig.get(JadeTerraCompositioPlugin.debugConfigRL())) {
            BlockPos pos = TCUtil.loadBlockPos(serverData.getCompound(RECEIVER_POS_TAG));
            if (pos != null) {
                iTooltip.add(Component.literal("ReceiverPos: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
            }
        }

        if (iPluginConfig.get(JadeTerraCompositioPlugin.debugConfigRL())) {
            Set<BlockPos> senderPoses = new HashSet<>();
            PathPointerBlockEntity.loadFromTagToSet(serverData, PathPointerBlockEntity.SENDER_POSES_TAG, senderPoses);
            int i = 1;
            for (BlockPos pos : senderPoses) {
                iTooltip.add(Component.literal("SenderPos " + i + ": " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
                i = i + 1;
            }
        }

        if (iPluginConfig.get(JadeTerraCompositioPlugin.debugConfigRL())) {
            Set<BlockPos> inputPoses = new HashSet<>();
            PathPointerBlockEntity.loadFromTagToSet(serverData, PathPointerBlockEntity.INPUT_POSES_TAG, inputPoses);
            int i = 1;
            for (BlockPos pos : inputPoses) {
                iTooltip.add(Component.literal("InputPos " + i + ": " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
                i = i + 1;
            }
        }

        if (serverData.contains(OUTPUT_POS_TAG) && iPluginConfig.get(JadeTerraCompositioPlugin.debugConfigRL())) {
            BlockPos pos = TCUtil.loadBlockPos(serverData.getCompound(OUTPUT_POS_TAG));
            if (pos != null) {
                iTooltip.add(Component.literal("OutputPos: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
            }
        }
    }

    public void highlightNodes() {
        if (level != null && level.isClientSide) {
            if (receiverPos != null) {
                addParticle(level, receiverPos, ParticleTypes.FLAME);
            }
            if (outputPos != null) {
                addParticle(level, outputPos, ParticleTypes.WAX_OFF);
            }

            senderPoses.forEach(blockpos ->
            {
                if (blockpos == null) return;
                addParticle(level, blockpos, ParticleTypes.SOUL_FIRE_FLAME);
            });
            inputPoses.forEach(blockpos ->
            {
                if (blockpos == null) return;
                addParticle(level, blockpos, ParticleTypes.WAX_ON);
            });
        }
    }

    private static void addParticle(Level level, BlockPos blockPos, SimpleParticleType particleType) {
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
