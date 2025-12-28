package net.sinedkadis.terracompositio.block.entity;

import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.*;
import net.sinedkadis.terracompositio.block.custom.PathPointerBlock;
import net.sinedkadis.terracompositio.cfe.CFEContainer;
import net.sinedkadis.terracompositio.cfe.LimitlessCFEContainer;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.registries.TCItems;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class PathPointerBlockEntity extends TCCFEBlockEntity implements Nameable {

    // rotationYaw: вокруг Y (в горизонтальной плоскости — блок "смотрит")
    // rotationPitch: вокруг X (наклон вверх/вниз)
    // rotationRoll: вокруг Z (вокруг взгляда, для кручения)
    public float rotationYaw, rotationPitch, rotationRoll;
    public @Nullable BlockPos nextNode;
    public @Nullable BlockPos lastNode;
    public @Nullable BlockPos collectorPos;
    public @Nullable LivingEntity toSendEntity;
    public @Nullable LivingEntity toReceiveEntity;
    private List<LivingEntity> lastToSendScan = List.of();

    public PathPointerBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.PATH_POINTER_BE.get(), pos, state, BlockMode.CONTAINER);
        this.setCfeContainer(new CFEContainer(this).setCfeTravelSpeed((float) 5 /20));
    }

    @Override
    public int getPriority() {
        if (level == null) return 100;
        BlockState pState = level.getBlockState(worldPosition);
        List<PathPointerBlock.PPPart> parts = Arrays.asList(PathPointerBlock.getParts(pState));
        if (parts.contains(PathPointerBlock.PPPart.EMITTER)) {
            return -100;
        }
        return 100;
    }

    int timer = 100;
    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        CFENetwork cfeNetworkInstance = TerraCompositioAPI.INSTANCE.getCFENetworkInstance();
        if (!pLevel.isClientSide) {
            boolean inNetwork = cfeNetworkInstance.isIn(pLevel, this);
            if (!inNetwork && !this.isRemoved()) {
                cfeNetworkInstance.fireCFENetworkEvent(this, NetworkAction.ADD);
            }
        }
        timer--;
        if (timer <= 0) {
            scheduleMemberUpdate();
        }
        if (toSendEntity != null && !(toSendEntity.blockPosition().equals(nextNode)) && toSendEntity.blockPosition() != this.getBlockPos()) {
            scheduleMemberUpdate();
        }
        if (toReceiveEntity != null && !(toReceiveEntity.blockPosition().equals(lastNode)) && toReceiveEntity.blockPosition() != this.getBlockPos()) {
            scheduleMemberUpdate();
        }
        if (toSendEntity == null || toReceiveEntity == null) {
            lastToSendScan = scanCompatibleEntities(pLevel);
            if (!lastToSendScan.isEmpty()) {
                scheduleMemberUpdate();
            }
        }
        updateIfScheduled();
    }

    @Override
    public void onCFENetworkMemberUpdate() {
        if (level == null) return;
        BlockState pState = level.getBlockState(worldPosition);
        List<PathPointerBlock.PPPart> parts = Arrays.asList(PathPointerBlock.getParts(pState));

        verifyNodes();
        computeSender(parts);
        computeReceiver(parts);
        computeCollector(parts);
        computeEmitter(parts);

        computeMax();

        transferCFE(level, parts);
        timer = 100;
    }

    private void computeMax() {
        if (level == null) return;
        PathPointerBlockEntity blockEntity = null;
        if (toReceiveEntity == null){
            if (collectorPos == null) {
                collectorPos = getCollector();
            }
            if (collectorPos != null) {
                blockEntity = ((PathPointerBlockEntity) level.getBlockEntity(collectorPos));
            }
            if (blockEntity != null) {
                blockEntity.updateMax();
            }
        } else {
            this.updateMax();
        }
    }

    private void computeEmitter(List<PathPointerBlock.PPPart> parts) {
        if (parts.contains(PathPointerBlock.PPPart.EMITTER)) {
            this.connectRange = 7;
        }
    }

    private void computeCollector(List<PathPointerBlock.PPPart> parts) {
        if (level == null) return;
        if (parts.contains(PathPointerBlock.PPPart.COLLECTOR)) {
            this.connectRange = 7;
            int toAdd = this.cfeContainer.getMaxCFE() - this.getInSystem();
            if (toAdd > 0) {
                if (!level.isClientSide()){

                    CFENetwork cfeNetwork = TerraCompositioAPI.instance().getCFENetworkInstance();
                    CFENetworkMember source = cfeNetwork.getClosestSourceWithCFE(worldPosition, level, connectRange * 2, getPriority());
                    if (source != null) {
                        TCUtil.tryCFETransfer(this, source, toAdd);

                    }
                }
            }
        }
    }

    private void computeReceiver(List<PathPointerBlock.PPPart> parts) {
        if (parts.contains(PathPointerBlock.PPPart.RECEIVER)) {
            if (toReceiveEntity == null && lastNode == null) {
                searchAndBindToEntity(level,false);
            }
            if (toReceiveEntity != null && lastNode != null ) {
                BlockPos currentEntityPos = toReceiveEntity.blockPosition();
                if (!(lastNode.equals(currentEntityPos))) {
                    if (toReceiveEntity.getItemBySlot(EquipmentSlot.HEAD).is(TCItems.TECHNETIUM_CROWN.get())
                            && simulateUpdateRotation(nextNode,getBlockPos(), currentEntityPos,false)) {
                        lastNode = currentEntityPos;
                    } else {
                        lastNode = null;
                        toReceiveEntity = null;
                    }
                    updateRotation(false);
                }
                if (toReceiveEntity instanceof CFENetworkMemberEntity member) {
                    ICFEHandler handler = toReceiveEntity.getCapability(CFECapability.CFE)
                            .filter(icfeHandler -> icfeHandler.getCFE() > 0).orElse(DummyCFEHandler.instance);
                    if (!(handler instanceof DummyCFEHandler)) {
                        TCUtil.tryCFETransfer(this,member,100);
                    }
                }
            }
        }
    }

    private void computeSender(List<PathPointerBlock.PPPart> parts) {
        if (parts.contains(PathPointerBlock.PPPart.SENDER)) {
            if (toSendEntity == null && nextNode == null) {
                searchAndBindToEntity(level,true);
            }
            if (nextNode != null && toSendEntity != null ) {
                BlockPos currentEntityPos = toSendEntity.blockPosition();
                if (!(nextNode.equals(currentEntityPos))) {
                    if (toSendEntity.getItemBySlot(EquipmentSlot.HEAD).is(TCItems.TECHNETIUM_CROWN.get())
                            && simulateUpdateRotation(lastNode,getBlockPos(), currentEntityPos,false)) {
                        nextNode = currentEntityPos;
                    } else {
                        nextNode = null;
                        toSendEntity = null;
                    }
                    updateRotation(false);
                }
                if (toSendEntity instanceof CFENetworkMemberEntity member) {
                    ICFEHandler handler = toSendEntity.getCapability(CFECapability.CFE)
                            .filter(icfeHandler -> icfeHandler.getFreeSpace() > 0).orElse(DummyCFEHandler.instance);
                    if (!(handler instanceof DummyCFEHandler)) {
                        TCUtil.tryCFETransfer(member,this,100);
                    }
                }
            }
        }
    }

    private void verifyNodes() {
        if (level == null) return;
        if (nextNode != null) {
            boolean nextNodeIsPP = level.getBlockState(nextNode).getBlock() instanceof PathPointerBlock;
            if (toSendEntity != null && (toSendEntity.isRemoved() || !toSendEntity.blockPosition().closerThan(worldPosition,getLimit())))
                toSendEntity = null;
            if (!nextNodeIsPP && toSendEntity == null) {
                nextNode = null;
            }
        }
        if (lastNode != null) {
            boolean lastNodeIsPP = level.getBlockState(lastNode).getBlock() instanceof PathPointerBlock;
            if (toReceiveEntity != null && (toReceiveEntity.isRemoved() || !toReceiveEntity.blockPosition().closerThan(worldPosition,getLimit())))
                toReceiveEntity = null;
            if (!lastNodeIsPP && toReceiveEntity == null) {
                lastNode = null;
            }
        }
    }

    private void searchAndBindToEntity(Level level,boolean isToSend) {
        List<LivingEntity> canBindTo = scanCompatibleEntities(level, isToSend);
        if (!canBindTo.isEmpty()) {
            Collections.shuffle(canBindTo);
            LivingEntity chosen = canBindTo.get(0);
            if (isToSend) {
                toSendEntity = chosen;
                nextNode = chosen.blockPosition();
            } else {
                toReceiveEntity = chosen;
                lastNode = chosen.blockPosition();
            }
            updateRotation(false);
            this.updateContainer();
        }
    }


    private @NotNull List<LivingEntity> scanCompatibleEntities(Level level) {
        return scanCompatibleEntities(level, true, true);
    }

    private @NotNull List<LivingEntity> scanCompatibleEntities(Level level, boolean isToSend) {
        return scanCompatibleEntities(level, isToSend, false);
    }

    private @NotNull List<LivingEntity> scanCompatibleEntities(Level level, boolean isToSend, boolean recalk) {
        if (isToSend && !lastToSendScan.isEmpty() && !recalk) return lastToSendScan;

        List<Entity> entities = level.getEntities(null,
                new AABB(this.worldPosition
                                .relative(Direction.WEST, 7)
                                .relative(Direction.SOUTH, 7)
                                .relative(Direction.DOWN, 7),
                        this.worldPosition
                                .relative(Direction.EAST,7)
                                .relative(Direction.NORTH,7)
                                .relative(Direction.UP,7)));
        return entities.stream()
                .map(entity -> entity instanceof LivingEntity livingEntity ? livingEntity : null)
                .filter(Objects::nonNull)
                .filter(livingEntity -> isToSend || !livingEntity.equals(toSendEntity))
                .filter(livingEntity -> isToSend || !(livingEntity instanceof Player))
                .filter(livingEntity -> livingEntity.hasItemInSlot(EquipmentSlot.HEAD))
                .filter(livingEntity -> livingEntity.getItemBySlot(EquipmentSlot.HEAD)
                        .is(TCItems.TECHNETIUM_CROWN.get()))
                .filter(livingEntity ->
                        simulateUpdateRotation(isToSend ? lastNode : nextNode,getBlockPos(), livingEntity.blockPosition(),false))
                .collect(Collectors.toList());
    }

    private void transferCFE(Level level, List<PathPointerBlock.PPPart> currentParts) {
        if (lastNode != null) {
            BlockState lastNodeBlockState = level.getBlockState(lastNode);
            List<PathPointerBlock.PPPart> lastParts = Arrays.asList(PathPointerBlock.getParts(lastNodeBlockState));
            if (lastParts.contains(PathPointerBlock.PPPart.SENDER) && currentParts.contains(PathPointerBlock.PPPart.RECEIVER)) {
                PathPointerBlockEntity lastNodeBE = ((PathPointerBlockEntity) level.getBlockEntity(lastNode));
                if (lastNodeBE != null) {
                    if (level.isClientSide) return;
                    TCUtil.tryCFETransfer(this, lastNodeBE, Integer.MAX_VALUE);
                }
            }
        }
    }


    public void updateContainer() {
        if (level != null){
            BlockState pState = level.getBlockState(getBlockPos());

            List<PathPointerBlock.PPPart> currentParts = Arrays.asList(PathPointerBlock.getParts(pState));

            if (currentParts.contains(PathPointerBlock.PPPart.SENDER)
                    && currentParts.contains(PathPointerBlock.PPPart.RECEIVER)
                    && toSendEntity == null
                    && toReceiveEntity == null) {
                this.setCfeContainer(new LimitlessCFEContainer(this).setCfeTravelSpeed((float) 5 / 20));
            } else {
                this.setCfeContainer(new CFEContainer(this).setCfeTravelSpeed((float) 5 / 20));
            }
        }
    }

    private int getMaxToEmit() {
        List<CFENetworkMember> members = TerraCompositioAPI.instance().getCFENetworkInstance().getAllCFENetworkMembers(level).stream()
                .filter(cfeNetworkMember -> cfeNetworkMember.getPos().closerThan(this.getBlockPos(),this.getLimit()))
                .filter(cfeNetworkMember -> cfeNetworkMember.getPriority() > this.getPriority())
                .filter(cfeNetworkMember -> {
                    if (cfeNetworkMember instanceof PathPointerBlockEntity){
                        List<PathPointerBlock.PPPart> parts = Arrays.asList(PathPointerBlock
                                .getParts(cfeNetworkMember.getLevel().getBlockState(cfeNetworkMember.getPos())));
                        return parts.contains(PathPointerBlock.PPPart.COLLECTOR);
                    }
                    return true;
                })
                .filter(cfeNetworkMember -> CFENetwork.getCFEHandler(cfeNetworkMember)
                        .filter(icfeHandler -> icfeHandler.getFreeSpace() > 0).isPresent())
                .filter(cfeNetworkMember -> cfeNetworkMember.getPos() != collectorPos)
                .toList();
        int max = 0;
        for (CFENetworkMember member : members){
            @SuppressWarnings("OptionalGetWithoutIsPresent")
            ICFEHandler handler = CFENetwork.getCFEHandler(member).get();
            max += handler.getFreeSpace();
        }
        return max;
    }

    private @Nullable BlockPos getCollector() {
        Queue<PathPointerBlockEntity> blockEntityQueue = new LinkedList<>();
        blockEntityQueue.add(this);
        while (!blockEntityQueue.isEmpty()){
            PathPointerBlockEntity currentBE = blockEntityQueue.poll();

            if (currentBE != null) {
                List<PathPointerBlock.PPPart> parts = Arrays.asList(PathPointerBlock.getParts(currentBE.getBlockState()));
                if (parts.contains(PathPointerBlock.PPPart.COLLECTOR)){
                    return currentBE.getBlockPos();
                }
                BlockPos lastNode = currentBE.lastNode;
                if (lastNode != null && !currentBE.getBlockPos().equals(lastNode) ) {
                    if (level == null) break;
                    PathPointerBlockEntity nextBE = (PathPointerBlockEntity) level.getBlockEntity(lastNode);
                    if (nextBE != null && nextBE.nextNode != null && !nextBE.nextNode.equals(currentBE.getBlockPos()))
                        blockEntityQueue.add(nextBE);
                }
            }
        }
        return null;
    }

    // Call from first member of chain
    public void updateMax() {
        Queue<List<PathPointerBlock.PPPart>> partQueue = new LinkedList<>();
        Queue<BlockEntity> blockEntityQueue = new LinkedList<>();

        List<PathPointerBlock.PPPart> parts = Arrays.asList(PathPointerBlock.getParts(getBlockState()));
        partQueue.add(parts);

        blockEntityQueue.add(this);

        while (!partQueue.isEmpty() && !blockEntityQueue.isEmpty()){
            List<PathPointerBlock.PPPart> currentPart = partQueue.poll();
            BlockEntity currentBE = blockEntityQueue.poll();
            if (currentBE instanceof PathPointerBlockEntity currentPPBE
                    && currentPart.contains(PathPointerBlock.PPPart.SENDER)
                    && currentPPBE.nextNode != null) {
                boolean bendToItself = currentPPBE.getBlockPos().equals(currentPPBE.nextNode);
                if (!bendToItself || currentPPBE.toSendEntity != null) {
                    if (level == null) break;
                    if (currentPPBE.toSendEntity != null) {
                        int max = currentPPBE.toSendEntity.getCapability(CFECapability.CFE).orElseGet(() -> DummyCFEHandler.instance).getFreeSpace();
                        this.cfeContainer.setMaxCFE(max);
                        currentPPBE.cfeContainer.setMaxCFE(max).setCfeTravelSpeed(5/20f);
                        break;
                    }
                    BlockState state = level.getBlockState(currentPPBE.nextNode);
                    partQueue.add(Arrays.asList(PathPointerBlock.getParts(state)));
                    blockEntityQueue.add(level.getBlockEntity(currentPPBE.nextNode));
                }
            }
            if (currentBE instanceof PathPointerBlockEntity currentPPBE && currentPart.contains(PathPointerBlock.PPPart.EMITTER)){
                if (level == null) break;
                currentPPBE.connectRange = 7;
                int max = currentPPBE.getMaxToEmit();
                this.cfeContainer.setMaxCFE(max);
                currentPPBE.cfeContainer.setMaxCFE(max).setCfeTravelSpeed(5/20f);
                currentPPBE.scheduleMemberUpdate();
                break;
            }
        }
    }

    private int getInSystem() {
        int inSystem = 0;

        if (level != null) {
            Queue<BlockEntity> blockEntityQueue = new LinkedList<>();
            if (toReceiveEntity == null){
                if (collectorPos == null) collectorPos = getCollector();
            } else {
                blockEntityQueue.add(this);
            }

            if (collectorPos != null) {
                PathPointerBlockEntity collector = (PathPointerBlockEntity) level.getBlockEntity(collectorPos);
                if (collector != null) {
                    blockEntityQueue.add(collector);
                }
            }

            while (!blockEntityQueue.isEmpty()) {
                BlockEntity currentBE = blockEntityQueue.poll();
                if (currentBE instanceof CFENetworkMember member) {
                    inSystem += CFENetwork.getCFEHandler(member).stream().mapToInt(ICFEHandler::getCFEWithQueue).sum();
                    if (currentBE instanceof PathPointerBlockEntity pp
                            && pp.nextNode != null
                            && !pp.nextNode.equals(pp.getBlockPos())) {
                        blockEntityQueue.add(level.getBlockEntity(pp.nextNode));
                    }
                }
            }
        }

        return inSystem;
    }

    public List<PathPointerBlockEntity> getNodes() {
        List<PathPointerBlockEntity> nodes = new ArrayList<>();


        if (level != null) {
            Queue<PathPointerBlockEntity> blockEntityQueue = new LinkedList<>();
            if (toReceiveEntity == null){
                if (collectorPos == null) collectorPos = getCollector();
            } else {
                blockEntityQueue.add(this);
            }
            if (collectorPos != null) {
                PathPointerBlockEntity collector = (PathPointerBlockEntity) level.getBlockEntity(collectorPos);
                if (collector != null) {
                    blockEntityQueue.add(collector);
                }
            }
            while (!blockEntityQueue.isEmpty()) {
                PathPointerBlockEntity currentBE = blockEntityQueue.poll();
                if (currentBE != null) {
                    nodes.add(currentBE);
                    if (currentBE.nextNode != null && !currentBE.nextNode.equals(currentBE.getBlockPos())) {
                        blockEntityQueue.add((PathPointerBlockEntity) level.getBlockEntity(currentBE.nextNode));
                    }
                }
            }
        }
        return nodes;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        PathPointerBlockEntity blockEntity = null;
        if (level != null) {
            if (toReceiveEntity == null){
                if (collectorPos == null) collectorPos = getCollector();
            } else {
                blockEntity = this;
            }
            if (collectorPos != null) {
                blockEntity = ((PathPointerBlockEntity) level.getBlockEntity(collectorPos));
            }
        }
        if (blockEntity != null) {
            blockEntity.updateMax();
        }
        updateRotation(false);
        updateContainer();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        pTag.putFloat("rot_y", rotationYaw);
        pTag.putFloat("rot_x", rotationPitch);
        pTag.putFloat("rot_z", rotationRoll);
        if (nextNode != null) {
            pTag.putLong("next", nextNode.asLong());
        }
        if (lastNode != null) {
            pTag.putLong("last", lastNode.asLong());
        }
        if (collectorPos != null) {
            pTag.putLong("collector", collectorPos.asLong());
        }
        super.saveAdditional(pTag);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        rotationYaw = pTag.getFloat("rot_y");
        rotationPitch = pTag.getFloat("rot_x");
        rotationRoll = pTag.getFloat("rot_z");
        if (pTag.contains("next"))
            nextNode = BlockPos.of(pTag.getLong("next"));
        if (pTag.contains("last"))
            lastNode = BlockPos.of(pTag.getLong("last"));
        if (pTag.contains("collector"))
            collectorPos = BlockPos.of(pTag.getLong("collector"));
    }

    public void highlightNodes() {
        if (level != null && level.isClientSide) {
            // Подсветка lastNode оранжевыми частицами (обычный огонь)
            if (lastNode != null) {
                addFireParticles(level, lastNode); // Оранжевый цвет
            }
            // Подсветка nextNode синими частицами (огонь душ)
            if (nextNode != null) {
                addSoulFireParticles(level, nextNode); // Голубой цвет
            }
        }
    }

    private static void addFireParticles(Level level, BlockPos pos) {
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
        RandomSource rand = level.random;
        for (int i = 0; i < 3; i++) {
            double x = pos.getX() + 0.5 + (rand.nextDouble() - 0.5) * 0.5;
            double y = pos.getY() + 0.5 + (rand.nextDouble() - 0.5) * 0.5;
            double z = pos.getZ() + 0.5 + (rand.nextDouble() - 0.5) * 0.5;

            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z,
                    0,0,0);
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public void updateRotation(boolean simulate) {
        if (nextNode == null && lastNode == null) return;

        BlockPos blockPos = getBlockPos();
        boolean ignorAngle = Objects.equals(nextNode, blockPos) || Objects.equals(lastNode, blockPos);
        ignorAngle |= toSendEntity != null;
        ignorAngle |= toReceiveEntity != null;
        Vec3 pos = Vec3.atCenterOf(blockPos);
        Vec3 dirForward;
        if (nextNode == null) {
            assert lastNode != null;
            dirForward = pos.subtract(Vec3.atCenterOf(lastNode)).normalize();
        } else {
            dirForward = Vec3.atCenterOf(nextNode).subtract(pos).normalize();
        }

        Vec3 dirBackward = lastNode == null ?
                dirForward :
                pos.subtract(Vec3.atCenterOf(lastNode)).normalize();

        float forward = ((float) Mth.atan2(dirForward.x, dirForward.z));
        float backward = ((float) Mth.atan2(dirBackward.x, dirBackward.z));

        float diff = ((float) Math.toDegrees(forward - backward));
        float abs_diff = Math.abs(diff);

        if ((abs_diff > 91 && abs_diff - 360 < -91) && !ignorAngle) return;

        if (!simulate) {

            Vec3 lookDir = dirForward.add(dirBackward).normalize();

            //noinspection SuspiciousNameCombination
            float yaw = (float) Math.toDegrees(Mth.atan2(lookDir.x,lookDir.z));


            float pitch = (float) Math.toDegrees(Mth.atan2(lookDir.y,Mth.sqrt((float) (Mth.square(lookDir.x)+Mth.square(lookDir.z)))));

            Vec3 up = dirForward.cross(dirBackward).normalize();
            Vec3 right = lookDir.cross(up).normalize();

            float roll = (float) Math.toDegrees(Mth.atan2(right.y,right.x)) + diff/2;

            this.rotationYaw = yaw;   // YAW (вокруг Y) → "куда смотрит"
            this.rotationPitch = pitch;      // PITCH (наклон)
            this.rotationRoll = roll;       // ROLL (кручение вокруг направления взгляда)

            this.setChanged();

            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(blockPos, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            }
        }
    }
    @SuppressWarnings("SuspiciousNameCombination")
    public static boolean simulateUpdateRotation(BlockPos lastNode,BlockPos currentPos, BlockPos nextNode,boolean ignorAngle) {
        if (nextNode == null && lastNode == null) return false;

        boolean ignorAngle1 = Objects.equals(nextNode, currentPos) || Objects.equals(lastNode, currentPos);
        ignorAngle1 |= ignorAngle;
        Vec3 pos = Vec3.atCenterOf(currentPos);
        Vec3 dirForward;
        if (nextNode == null) {
            dirForward = pos.subtract(Vec3.atCenterOf(lastNode)).normalize();
        } else {
            dirForward = Vec3.atCenterOf(nextNode).subtract(pos).normalize();
        }

        Vec3 dirBackward = lastNode == null ?
                dirForward :
                pos.subtract(Vec3.atCenterOf(lastNode)).normalize();

        float forward = ((float) Mth.atan2(dirForward.x, dirForward.z));
        float backward = ((float) Mth.atan2(dirBackward.x, dirBackward.z));

        float diff = ((float) Math.toDegrees(Math.abs(forward - backward)));

        return (!(diff > 91) || !(diff - 360 < -91)) || ignorAngle1;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null) {
            if (this.lastNode != null) {
                BlockEntity last = level.getBlockEntity(this.lastNode);
                if (last instanceof PathPointerBlockEntity last1) {
                    last1.nextNode = null;
                    last1.updateRotation(false);
                }
            }
            if (this.nextNode != null) {
                BlockEntity next = level.getBlockEntity(this.nextNode);
                if (next instanceof PathPointerBlockEntity next1) {
                    next1.lastNode = null;
                    next1.updateRotation(false);
                }
            }
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        rotationYaw = tag.getFloat("rotationX");
        rotationPitch = tag.getFloat("rotationY");
        rotationRoll = tag.getFloat("rotationZ");
    }

    @Override
    public @NotNull Component getName() {
        return Component.translatable("block.terracompositio.path_pointer");
    }

    @Override
    public @Nullable Component getCustomName() {
        return Component.translatable("block.terracompositio.path_pointer");
    }
}
