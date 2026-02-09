package net.sinedkadis.terracompositio.block.entity;

import lombok.Getter;
import lombok.Setter;
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

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@Slf4j
@ParametersAreNonnullByDefault
public class PathPointerBlockEntity extends TCBlockEntity implements Nameable {

    private static final String bindPosTag = "BindPos";

    public float rotationYaw, rotationPitch, rotationRoll;

    public List<PPPart> parts = new PPPartList();

    @Setter
    private boolean updateScheduled = false;

    public PathPointerBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.PATH_POINTER_BE.get(), pos, state);
        if (state.getBlock() instanceof PathPointerBlock pathPointerBlock) {
            parts.set(0,pathPointerBlock.getBasePart());
        }
    }

    @Override
    public void addBEBehaviours(List<IBEBehaviour> list) {
        //recursive behavior adding and init
    }

    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        super.tick(pLevel, pPos, pState);
        if (updateScheduled) {
            updateScheduled = false;
            setChanged();
            pLevel.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
        }
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

        if (!(level.getBlockEntity(storedPos) instanceof PathPointerBlockEntity storedPPBE)) {
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

        SenderBehaviour clickedSender = clickedPPBE.getBehaviours().stream()
                .map(ibeBehaviour -> ibeBehaviour instanceof SenderBehaviour senderBehaviour ? senderBehaviour : null)
                .filter(Objects::nonNull)
                .findAny().orElse(null);
        ReceiverBehaviour clickedReceiver = clickedPPBE.getBehaviours().stream()
                .map(ibeBehaviour -> ibeBehaviour instanceof ReceiverBehaviour receiverBehaviour ? receiverBehaviour : null)
                .filter(Objects::nonNull)
                .findAny().orElse(null);

        SenderBehaviour storedSender = storedPPBE.getBehaviours().stream()
                .map(ibeBehaviour -> ibeBehaviour instanceof SenderBehaviour senderBehaviour ? senderBehaviour : null)
                .filter(Objects::nonNull)
                .findAny().orElse(null);
        ReceiverBehaviour storedReceiver = storedPPBE.getBehaviours().stream()
                .map(ibeBehaviour -> ibeBehaviour instanceof ReceiverBehaviour receiverBehaviour ? receiverBehaviour : null)
                .filter(Objects::nonNull)
                .findAny().orElse(null);

        if (forwardBind) {
            assert storedSender != null;
            assert clickedReceiver != null;
            bind(storedSender,storedReceiver,clickedSender,clickedReceiver);
        }
        if (backwardBind) {
            assert clickedSender != null;
            assert storedReceiver != null;
            bind(clickedSender,clickedReceiver,storedSender,storedReceiver);
        }

        storedPPBE.setChanged();
        clickedPPBE.setChanged();

        ((ServerLevel) level).sendBlockUpdated(storedPos,storedPPBE.getBlockState(),storedPPBE.getBlockState(),3);
        ((ServerLevel) level).sendBlockUpdated(clickedPos,clickedPPBE.getBlockState(),clickedPPBE.getBlockState(),3);

        storedPPBE.setUpdateScheduled(true);
        clickedPPBE.setUpdateScheduled(true);

        if (pPlayer != null) {
            TCUtil.message(pPlayer, Component.translatable("item.terracompositio.flow_rotating_axe.bind_success").withStyle(ChatFormatting.BOLD));
            wrenchStack.hurtAndBreak(1, pPlayer, player1 -> {
                assert player1 != null;
                player1.broadcastBreakEvent(InteractionHand.MAIN_HAND);
            });
        }
        return true;
    }

    private static void bind(SenderBehaviour firstSender,
                             @Nullable ReceiverBehaviour firstReceiver,
                             @Nullable SenderBehaviour secondSender,
                             ReceiverBehaviour secondReceiver) {
        List<BlockPos> firstReceiverSenderPoses = new ArrayList<>();
        if (firstReceiver != null)
            firstReceiverSenderPoses.addAll(firstReceiver.getSenderPoses());
        BlockPos secondSenderBindPos = null;
        if (secondSender != null)
            secondSenderBindPos = secondSender.getBindPos();

        BlockPos firstPos = firstSender.getBlockEntity().getBlockPos();
        BlockPos secondPos = secondReceiver.getBlockEntity().getBlockPos();

        Vec3 rotInput = calculateRot(firstReceiverSenderPoses, secondPos, firstPos);
        if (rotInput == null) return;
        setYawAndPitchFromRot(rotInput, firstSender.getBlockEntity());
        firstSender.setBindPos(secondPos);


        List<BlockPos> senderPoses = secondReceiver.getSenderPoses();
        List<BlockPos> senderPosesCopy = new ArrayList<>(senderPoses);
        if (senderPoses.contains(firstPos)) return;
        senderPosesCopy.add(firstPos);

        Vec3 rotOutput = calculateRot(senderPosesCopy, secondSenderBindPos, secondPos);
        if (rotOutput == null) return;

        setYawAndPitchFromRot(rotOutput, secondReceiver.getBlockEntity());
        senderPoses.add(firstPos);
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
            //be.rotationRoll = 0;
            be.behaviours.forEach(ibeBehaviour -> {
                if (ibeBehaviour instanceof SenderBehaviour senderBehaviour) {
                    senderBehaviour.setBindPos(null);
                }
                if (ibeBehaviour instanceof ReceiverBehaviour receiverBehaviour) {
                    receiverBehaviour.getSenderPoses().clear();
                }
            });
            be.setUpdateScheduled(true);
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

    private static Vec3 calculateRot(List<BlockPos> receiverSenderPoses,@Nullable BlockPos senderBindPos,BlockPos origin) {
        // --- вектор К bind'у (вперёд)
        Vec3 toBind = null;
        if (senderBindPos != null) {
            toBind = Vec3.atCenterOf(senderBindPos.subtract(origin)).normalize();
        }

        // --- векторы К receivers
        List<Vec3> toReceivers = receiverSenderPoses.stream()
                .map(p -> Vec3.atCenterOf(p.subtract(origin)).normalize())
                .toList();

        // --- если receivers нет — смотрим строго на bind
        if (toReceivers.isEmpty()) {
            return toBind;
        }

        // --- усредняем receivers и смотрим ОТ них
        Vec3 receiversAvg = Vec3.ZERO;
        for (Vec3 r : toReceivers) {
            receiversAvg = receiversAvg.add(r);
        }
        receiversAvg = receiversAvg.normalize().scale(-1); // важно

        // --- если есть bind — тянем направление вперёд
        Vec3 lookDir = receiversAvg;
        if (toBind != null) {
            lookDir = lookDir.add(toBind).normalize();
        }

        // --- receivers должны быть сзади
        for (Vec3 r : toReceivers) {
            if (lookDir.dot(r) > 0) { // угол < 90° → спереди → херня
                return toBind; // fallback
            }
        }

        // --- bind должен быть спереди
        if (toBind != null && lookDir.dot(toBind) <= 0) {
            return toBind;
        }

        return lookDir;



// if (receiverSenderPoses.isEmpty()) return senderBindPos.getCenter().reverse();
//        List<Vec3> oSenders = receiverSenderPoses.stream().map(blockPos -> blockPos.subtract(origin).getCenter()).toList();
//        boolean sendersInBindRange = true;
//        boolean sendersInAddedRange = true;
//        Vec3 added;
//        Vec3 roBind = null;
//
//        if (senderBindPos != null) {
//            BlockPos oBind = senderBindPos.subtract(origin);
//
//            roBind = oBind.getCenter().reverse();
//
//            for (Vec3 vec3 : oSenders) {
//                sendersInBindRange &= angle(vec3,roBind) < Math.PI/2;
//            }
//        }
//        if (sendersInBindRange) {
//            Optional<Vec3> addedSendersOpt = oSenders.stream().reduce(Vec3::add);
//            if (addedSendersOpt.isPresent()) {
//                added = addedSendersOpt.get().normalize();
//                if (roBind != null) {
//                    added = added.add(roBind);
//                }
//
//                for (Vec3 vec3 : oSenders) {
//                    sendersInAddedRange &= angle(vec3,added) < Math.PI/2;
//                }
//                if (sendersInAddedRange)
//                    return added.reverse();
//            }
//        }
//        if (roBind != null)
//            return roBind.reverse();
//        return null;
    }
    

    private static double angle(Vec3 a, Vec3 b) {
        return Math.acos(a.dot(b)/a.length()/b.length());
    }



    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.putFloat("rot_y", rotationYaw);
        pTag.putFloat("rot_x", rotationPitch);
        pTag.putFloat("rot_z", rotationRoll);

        pTag.putInt("part0",parts.get(0).ordinal());
        pTag.putInt("part1",parts.get(1).ordinal());
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
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
            CompoundTag persistentData = getPersistentData();
            addFireParticles(level, TCUtil.loadBlockPos(persistentData.getCompound("bindpos")));

            int size = persistentData.getInt("SenderCount");
            for (int i = 0; i < size; i++) {
                addSoulFireParticles(level,TCUtil.loadBlockPos(persistentData.getCompound("SenderPos_"+i)));
            }
        }
    }

    private static void addFireParticles(Level level,@Nullable BlockPos pos) {
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

    private static void addSoulFireParticles(Level level,@Nullable BlockPos pos) {
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
