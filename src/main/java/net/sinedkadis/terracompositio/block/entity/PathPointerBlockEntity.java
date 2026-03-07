package net.sinedkadis.terracompositio.block.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import net.sinedkadis.terracompositio.block.behaviours.CFEHandlerBehaviour;
import net.sinedkadis.terracompositio.block.behaviours.pp.*;
import net.sinedkadis.terracompositio.block.custom.PathPointerBlock;
import net.sinedkadis.terracompositio.cfe.CFEContainer;
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

    public List<PPPart> parts = NonNullList.withSize(2,PPPart.NONE);

    @Setter
    private boolean updateScheduled = false;
    @Setter
    private boolean ppUpdateScheduled = false;


    public PathPointerBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.PATH_POINTER_BE.get(), pos, state);
        if (state.getBlock() instanceof PathPointerBlock pathPointerBlock) {
            parts.set(0,pathPointerBlock.getBasePart());
        }
    }

    @Override
    public void addBEBehaviours(List<IBEBehaviour> list) {
        list.add(new CFEHandlerBehaviour(this){
            @Override
            public int getPriority() {
                if (parts.contains(PPPart.EMITTER) && parts.contains(PPPart.COLLECTOR)) return 0;
                if (parts.contains(PPPart.EMITTER)) return -100;
                if (parts.contains(PPPart.COLLECTOR)) return 100;
                return 0;
            }

            @Override
            public boolean isActive() {
                return !(parts.contains(PPPart.SENDER) && parts.contains(PPPart.RECEIVER));
            }

            @Override
            public void scheduleMemberUpdate() {
                super.scheduleMemberUpdate();
                setPpUpdateScheduled(true);
            }
        }
                .cfeHandler(cfeHandlerBehaviour -> new CFEContainer(cfeHandlerBehaviour){
                    @Override
                    public float getCfeTravelSpeed() {
                        if (parts.contains(PPPart.RECEIVER)) return 5/20f;
                        return 1/20f;
                    }

                    @Override
                    public int addCFE(int cfe, boolean simulate) {
                        int added = super.addCFE(cfe, simulate);
                        if (parts.contains(PPPart.COLLECTOR)) {
                            setPpUpdateScheduled(true);
                        }
                        return added;
                    }
                })
                .maxCFE(100)
                .limit(5)
                );
        list.add(new CollectorBehaviour(this));
        list.add(new EmitterBehaviour(this));
        list.add(new ExtractorBehaviour(this));
        list.add(new InfuserBehaviour(this));
        list.add(new SenderBehaviour(this));
        list.add(new ReceiverBehaviour(this));
    }

    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        super.tick(pLevel, pPos, pState);
        if (updateScheduled) {
            updateScheduled = false;
            setChanged();
            pLevel.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
        }
        if (ppUpdateScheduled) {
            ppUpdateScheduled = false;
            behaviours.stream()
                    .filter(IBEBehaviour::isActive)
                    .forEach(IBEBehaviour::onUpdate);
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

        if (forwardBind) {
            if(!bind(storedPPBE,clickedPPBE)) {
                TCUtil.message(pPlayer, Component.translatable("item.terracompositio.flow_rotating_axe.bind_fail_angle")
                        .withStyle(ChatFormatting.BOLD));
                return false;
            }
        }
        if (backwardBind) {
            if(!bind(clickedPPBE,storedPPBE)) {
                TCUtil.message(pPlayer, Component.translatable("item.terracompositio.flow_rotating_axe.bind_fail_angle")
                        .withStyle(ChatFormatting.BOLD));
                return false;
            }
        }

        storedPPBE.setChanged();
        clickedPPBE.setChanged();

        ((ServerLevel) level).sendBlockUpdated(storedPos,storedPPBE.getBlockState(),storedPPBE.getBlockState(),3);
        ((ServerLevel) level).sendBlockUpdated(clickedPos,clickedPPBE.getBlockState(),clickedPPBE.getBlockState(),3);

        storedPPBE.setUpdateScheduled(true);
        clickedPPBE.setUpdateScheduled(true);

        storedPPBE.setPpUpdateScheduled(true);
        clickedPPBE.setPpUpdateScheduled(true);

        if (pPlayer != null) {
            TCUtil.message(pPlayer, Component.translatable("item.terracompositio.flow_rotating_axe.bind_success").withStyle(ChatFormatting.BOLD));
            wrenchStack.hurtAndBreak(1, pPlayer, player1 -> {
                assert player1 != null;
                player1.broadcastBreakEvent(InteractionHand.MAIN_HAND);
            });
        }
        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean bind(PathPointerBlockEntity firstPPBE, PathPointerBlockEntity secondPPBE) {

        BlockPos firstPos = firstPPBE.getBlockPos();
        BlockPos secondPos = secondPPBE.getBlockPos();

        Vec3 rotInput = calculateRot(ReceiverBehaviour.getSenderPoses(firstPPBE), secondPos, firstPos);
        if (rotInput == null) return false;



        List<BlockPos> senderPoses = ReceiverBehaviour.getSenderPoses(secondPPBE);
        List<BlockPos> senderPosesCopy = new ArrayList<>(senderPoses);
        if (senderPoses.contains(firstPos)) return false;
        senderPosesCopy.add(firstPos);

        Vec3 rotOutput = calculateRot(senderPosesCopy, SenderBehaviour.getBindPos(secondPPBE), secondPos);
        if (rotOutput == null) return false;


        setYawAndPitchFromRot(rotInput, firstPPBE);
        SenderBehaviour.setBindPos(firstPPBE,secondPos);

        setYawAndPitchFromRot(rotOutput, secondPPBE);
        ReceiverBehaviour.setSenderPoses(secondPPBE,senderPosesCopy);
        return true;
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
            SenderBehaviour.setBindPos(be,null);
            ReceiverBehaviour.setSenderPoses(be,List.of());
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
        tag.remove(bindPosTag);
    }

    @Nullable
    private static Vec3 calculateRot(
            List<BlockPos> receiverSenderPoses,
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
                if (r.dot(toBind) > 0) { // угол < 90°
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

        parts = NonNullList.withSize(2,PPPart.NONE);
        parts.set(0,PPPart.fromOrdinal(pTag.getInt("part0")));
        parts.set(1,PPPart.fromOrdinal(pTag.getInt("part1")));
    }

    public void highlightNodes() {
        if (level != null && level.isClientSide) {
            addFireParticles(level, SenderBehaviour.getBindPos(this));

            ReceiverBehaviour.getSenderPoses(this).forEach(blockpos ->
                            addSoulFireParticles(level, blockpos));
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
        COLLECTOR(true, "collector"),
        RECEIVER(true, "receiver"),
        SENDER(false, "sender"),
        EMITTER(false, "emitter"),
        INFUSER(false, "infuser"),
        EXTRACTOR(true, "extractor"),
        NONE(false, "none");

        @Getter
        private final boolean input;
        private final String name;


        PPPart(boolean isInput, String name) {
            this.input = isInput;
            this.name = name;
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
    }
}
