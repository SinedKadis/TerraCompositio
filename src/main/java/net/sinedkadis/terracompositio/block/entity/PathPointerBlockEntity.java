package net.sinedkadis.terracompositio.block.entity;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBEBehaviour;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IBECFEBehaviour;
import net.sinedkadis.terracompositio.api.behaviors.blockentity.IPPBEBehaviour;
import net.sinedkadis.terracompositio.api.dummies.DummyBehaviour;
import net.sinedkadis.terracompositio.block.behaviours.pp_behaviours.*;
import net.sinedkadis.terracompositio.block.custom.PathPointerBlock;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.naming.Name;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class PathPointerBlockEntity extends TCBlockEntity implements Nameable {

    private static final String bindPosTag = "BindPos";

    public float rotationYaw, rotationPitch, rotationRoll;

    public List<PPPart> parts = new PPPartList();

    public PathPointerBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.PATH_POINTER_BE.get(), pos, state);
        if (state.getBlock() instanceof PathPointerBlock pathPointerBlock) {
            parts.set(0,pathPointerBlock.getBasePart());
        }
    }

    @Override
    public void addBEBehaviours(@NotNull List<IBEBehaviour> list) {
        //recursive behavior adding and init
    }


    public static boolean ppWrenchInteraction(@Nullable Player pPlayer, LevelAccessor level, BlockPos clickedPos, ItemStack wrenchStack) {

        BlockEntity blockEntity = level.getBlockEntity(clickedPos);
        if (!(blockEntity instanceof PathPointerBlockEntity clickedPPBE)) {
            return false;
        }

        if (pPlayer != null && pPlayer.isShiftKeyDown()) {
            rotatePP(clickedPPBE);
            return true;
        }

        CompoundTag tag = wrenchStack.getOrCreateTag();
        if (!tag.contains(bindPosTag)) {
            storeToTag(pPlayer, clickedPos, tag);
            return true;
        }

        BlockPos storedPos = TCUtil.loadBlockPos(tag.getCompound(bindPosTag));
        clearBindTags(tag);
        if (storedPos.equals(clickedPos)) {
            clearAnyBindings(pPlayer,level, clickedPos);
            return true;
        }

        BlockEntity storedBE = level.getBlockEntity(storedPos);
        if (!(storedBE instanceof PathPointerBlockEntity storedPPBE)) {
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

        Optional<SenderBehaviour> clickedSenderOpt = clickedPPBE.getBehaviours().stream()
                .map(ibeBehaviour -> ibeBehaviour instanceof SenderBehaviour senderBehaviour ? senderBehaviour : null)
                .filter(Objects::nonNull)
                .findAny();
        Optional<ReceiverBehaviour> clickedReceiverOpt = clickedPPBE.getBehaviours().stream()
                .map(ibeBehaviour -> ibeBehaviour instanceof ReceiverBehaviour receiverBehaviour ? receiverBehaviour : null)
                .filter(Objects::nonNull)
                .findAny();

        Optional<SenderBehaviour> storedSenderOpt = storedPPBE.getBehaviours().stream()
                .map(ibeBehaviour -> ibeBehaviour instanceof SenderBehaviour senderBehaviour ? senderBehaviour : null)
                .filter(Objects::nonNull)
                .findAny();
        Optional<ReceiverBehaviour> storedReceiverOpt = storedPPBE.getBehaviours().stream()
                .map(ibeBehaviour -> ibeBehaviour instanceof ReceiverBehaviour receiverBehaviour ? receiverBehaviour : null)
                .filter(Objects::nonNull)
                .findAny();


        Optional<SenderBehaviour> inputSenderOpt;
        if (inputSenderOpt.isEmpty() || outputReceiverOpt.isEmpty()) {
            return false;
        }

        List<BlockPos> inputReceiverSenderPoses = new ArrayList<>();
        inputReceiverOpt.ifPresent(receiverBehaviour -> inputReceiverSenderPoses.addAll(receiverBehaviour.getSenderPoses()));
        AtomicReference<BlockPos> outputSenderBindPos = new AtomicReference<>(null);
        outputSenderOpt.ifPresent(senderBehaviour -> outputSenderBindPos.set(senderBehaviour.getBindPos()));


        Vec3 rotInput = calculateRot(inputReceiverSenderPoses, outputPos, inputPos);
        if (rotInput == null) return false;

        setYawAndPitchFromRot(rotInput, inputBE);
        inputSenderOpt.get().setBindPos(outputPos);
        BlockState inpitBlockState = level.getBlockState(inputPos);
        ((Level) level).sendBlockUpdated(inputPos,inpitBlockState,inpitBlockState,3);



        List<BlockPos> senderPoses = outputReceiverOpt.get().getSenderPoses();
        List<BlockPos> senderPosesCopy = new ArrayList<>(senderPoses);
        senderPosesCopy.add(inputPos);

        Vec3 rotOutput = calculateRot(senderPosesCopy, outputSenderBindPos.get(), outputPos);
        if (rotOutput == null) return false;

        setYawAndPitchFromRot(rotOutput, outputBE);
        senderPoses.add(inputPos);
        BlockState outputBlockState = level.getBlockState(outputPos);
        ((Level) level).sendBlockUpdated(outputPos,outputBlockState,outputBlockState,3);



        if (pPlayer != null) {
            TCUtil.message(pPlayer, Component.translatable("item.terracompositio.flow_rotating_axe.bind_success").withStyle(ChatFormatting.BOLD));
            wrenchStack.hurtAndBreak(1, pPlayer, player1 -> {
                assert player1 != null;
                player1.broadcastBreakEvent(InteractionHand.MAIN_HAND);
            });
        }
        return true;
    }

    private static void setYawAndPitchFromRot(Vec3 rotInput, PathPointerBlockEntity inputBE) {
        @SuppressWarnings("SuspiciousNameCombination")
        double yaw = Mth.atan2(rotInput.x, rotInput.z);
        double pitch = Mth.atan2(rotInput.y, rotInput.z * Math.cos(yaw));
        inputBE.rotationYaw = (float) Math.toDegrees(yaw);
        inputBE.rotationPitch = (float) Math.toDegrees(pitch);
    }

    private static void clearAnyBindings(@Nullable Player pPlayer, LevelAccessor level, BlockPos inputPos) {
        sendBindMessage(pPlayer, "item.terracompositio.flow_rotating_axe.bind_cleared");
        PathPointerBlockEntity be = ((PathPointerBlockEntity) level.getBlockEntity(inputPos));
        if (be != null) {
            be.rotationPitch = 90;
            be.rotationYaw = 0;
            be.rotationRoll = 0;
            be.setChanged();
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 1);
            }
        }
    }

    private static void storeToTag(@Nullable Player pPlayer, BlockPos pos, CompoundTag tag) {
        tag.put(bindPosTag,TCUtil.saveBlockPos(pos));

        if (pPlayer != null) {
            TCUtil.message(pPlayer, Component.translatable("item.terracompositio.flow_rotating_axe.bind_begin").withStyle(ChatFormatting.BOLD));
        }
    }

    private static void rotatePP(PathPointerBlockEntity pathPointerBlockEntity) {
        if (pathPointerBlockEntity.getLevel() != null) {
            pathPointerBlockEntity.rotationRoll += 25.5F;
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
        tag.remove(bindPosTag);
    }

    private static Vec3 calculateRot(List<BlockPos> receiverSenderPoses, BlockPos senderBindPos,BlockPos origin) {
        //if (receiverSenderPoses.isEmpty()) return senderBindPos.getCenter().reverse();
        List<Vec3> oSenders = receiverSenderPoses.stream().map(blockPos -> blockPos.subtract(origin).getCenter()).toList();
        boolean sendersInBindRange = true;
        Vec3 addedSenders = Vec3.ZERO;
        Vec3 roBind = null;
        if (senderBindPos != null) {
            BlockPos oBind = senderBindPos.subtract(origin);

            roBind = oBind.getCenter().reverse();

            for (Vec3 vec3 : oSenders) {
                sendersInBindRange &= angle(vec3,roBind) < Math.PI/2;
            }
            addedSenders = addedSenders.add(roBind);
        }
        if (sendersInBindRange) {
            Optional<Vec3> addedSendersOpt = oSenders.stream().reduce(Vec3::add);
            if (addedSendersOpt.isPresent()) {
                addedSenders = addedSenders.add(addedSendersOpt.get()).normalize();
            }

            boolean sendersInAddedRange = true;
            for (Vec3 vec3 : oSenders) {
                sendersInAddedRange &= angle(vec3,addedSenders) < Math.PI/2;
            }

            if (sendersInAddedRange)
                return addedSenders.reverse();
            if (roBind != null)
                return roBind.reverse();
        }
        return null;
    }

    private static double angle(Vec3 a, Vec3 b) {
        return Math.acos(a.dot(b)/a.length()/b.length());
    }



    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        pTag.putFloat("rot_y", rotationYaw);
        pTag.putFloat("rot_x", rotationPitch);
        pTag.putFloat("rot_z", rotationRoll);

        pTag.putInt("part0",parts.get(0).ordinal());
        pTag.putInt("part1",parts.get(1).ordinal());
        super.saveAdditional(pTag);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        rotationYaw = pTag.getFloat("rot_y");
        rotationPitch = pTag.getFloat("rot_x");
        rotationRoll = pTag.getFloat("rot_z");

        parts = new PPPartList();
        parts.set(0,PPPart.fromOrdinal(pTag.getInt("part0")));
        parts.set(1,PPPart.fromOrdinal(pTag.getInt("part1")));
    }

    public void highlightNodes() {
        if (level != null && level.isClientSide) {
            getBehaviours().forEach(ibeBehaviour -> {
                if (ibeBehaviour instanceof SenderBehaviour senderBehaviour) {
                    addFireParticles(level, senderBehaviour.getBindPos());
                }
                if (ibeBehaviour instanceof ReceiverBehaviour receiverBehaviour) {
                    receiverBehaviour.getSenderPoses().forEach(blockPos ->
                            addSoulFireParticles(level, blockPos));
                }
            });
        }
    }

    private static void addFireParticles(Level level, BlockPos pos) {
        if (pos == null) return;
        RandomSource rand = level.random;
        for (int i = 0; i < 3; i++) {
            double x = pos.getX() + 0.5 + (rand.nextDouble() - 0.5) * 0.5;
            double y = pos.getY() + 0.5 + (rand.nextDouble() - 0.5) * 0.5;
            double z = pos.getZ() + 0.5 + (rand.nextDouble() - 0.5) * 0.5;

            level.addParticle(ParticleTypes.FLAME, x, y, z,
                    0,0,0);
        }
    }

    private static void addSoulFireParticles(Level level, BlockPos pos) {
        if (pos == null) return;
        RandomSource rand = level.random;
        for (int i = 0; i < 3; i++) {
            double x = pos.getX() + 0.5 + (rand.nextDouble() - 0.5) * 0.5;
            double y = pos.getY() + 0.5 + (rand.nextDouble() - 0.5) * 0.5;
            double z = pos.getZ() + 0.5 + (rand.nextDouble() - 0.5) * 0.5;

            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z,
                    0,0,0);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        rotationYaw = tag.getFloat("rot_y");
        rotationPitch = tag.getFloat("rot_x");
        rotationRoll = tag.getFloat("rot_z");
        behaviours.forEach(ibeBehaviour -> {
            if (ibeBehaviour instanceof IPPBEBehaviour ippbeBehaviour) {
                ippbeBehaviour.onTagUpdate(tag);
            }
        });
    }

    @Override
    public @NotNull Component getName() {
        return Component.translatable("block.terracompositio.path_pointer");
    }

    @Override
    public @Nullable Component getCustomName() {
        return Component.translatable("block.terracompositio.path_pointer");
    }



    public void scheduleMemberUpdate() {
        cfeBehaviour().scheduleMemberUpdate();
    }

    public IBECFEBehaviour cfeBehaviour() {
        IBEBehaviour ibeBehaviour = behaviours.get(behaviours.size() - 1);
        if (ibeBehaviour instanceof IBECFEBehaviour ibecfeBehaviour)
            return ibecfeBehaviour;
        return null;
    }

    public enum PPPart implements StringRepresentable {
        COLLECTOR(true, "collector", CollectorBehaviour::new),
        RECEIVER(true, "receiver", ReceiverBehaviour::new),
        SENDER(false, "sender", SenderBehaviour::new),
        EMITTER(false, "emitter", EmitterBehaviour::new),
        INFUSER(false, "infuser", InfuserBehaviour::new),
        EXTRACTOR(true, "extractor", ExtractorBehaviour::new),
        NONE(false, "none", (be) -> DummyBehaviour.instance);

        @Getter
        private final boolean input;
        private final String name;
        @Getter
        private final BehaviourFactory behaviourFactory;

        PPPart(boolean isInput, String name, BehaviourFactory behaviourFactory) {
            this.input = isInput;
            this.name = name;
            this.behaviourFactory = behaviourFactory;
        }

        @Override
        public String toString() {
            return "PPPart{" +
                    "name='" + name + '\'' +
                    '}';
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }

        public static PPPart fromOrdinal(int ordinal) {
            for (PPPart part : PPPart.values()) {
                if (part.ordinal() == ordinal) return part;
            }
            return PPPart.NONE;
        }



        @FunctionalInterface
        interface BehaviourFactory {
            IBEBehaviour getBehaviour(PathPointerBlockEntity blockEntity);
        }
    }

    private class PPPartList extends ArrayList<PPPart> {
        public PPPartList() {
            super(2);
            this.add(PPPart.NONE);
            this.add(PPPart.NONE);
        }

        @Override
        public PPPart get(int index) {

            return super.get(index);
        }

        @Override
        public PPPart remove(int index) {
            IBEBehaviour ibeBehaviour = behaviours.get(index);
            ibeBehaviour.onRemoved();
            behaviours.set(index, PPPart.NONE.behaviourFactory.getBehaviour(PathPointerBlockEntity.this));
            return super.set(index, PPPart.NONE);
        }

        @Override
        public PPPart set(int index, PPPart element) {
            while (behaviours.size() <= index) behaviours.add(DummyBehaviour.instance);
            behaviours.set(index,element.behaviourFactory.getBehaviour(PathPointerBlockEntity.this));
            behaviours.get(index).init();
            return super.set(index, element);
        }
    }
}
