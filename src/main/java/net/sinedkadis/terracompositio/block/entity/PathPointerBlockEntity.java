package net.sinedkadis.terracompositio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.NetworkAction;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetwork;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.block.custom.PathPointerBlock;
import net.sinedkadis.terracompositio.cfe.CFEContainer;
import net.sinedkadis.terracompositio.cfe.LimitlessCFEContainer;
import net.sinedkadis.terracompositio.particle.CFEParticleData;
import net.sinedkadis.terracompositio.registries.TCBlockEntities;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PathPointerBlockEntity extends TCCFEBlockEntity implements Nameable {

    // rotationYaw: вокруг Y (в горизонтальной плоскости — блок "смотрит")
    // rotationPitch: вокруг X (наклон вверх/вниз)
    // rotationRoll: вокруг Z (вокруг взгляда, для кручения)
    public float rotationYaw, rotationPitch, rotationRoll;
    public @Nullable BlockPos nextNode;
    public @Nullable BlockPos lastNode;
    public @Nullable BlockPos collectorPos;

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
        cfeContainer.containerTick();
        timer--;
        if (timer <= 0) {
            onCFENetworkMemberUpdate(pLevel,pPos);
        }
    }

    @Override
    public void onCFENetworkMemberUpdate(Level level, BlockPos pos) {
        timer = 100;
        BlockState pState = level.getBlockState(pos);
        List<PathPointerBlock.PPPart> parts = Arrays.asList(PathPointerBlock.getParts(pState));
        if (parts.contains(PathPointerBlock.PPPart.COLLECTOR)) {
            this.connectRange = 7;
            int toAdd = this.cfeContainer.getMaxCFE() - this.getInSystem();
            if (toAdd > 0) {
                if (!level.isClientSide()){

                    CFENetwork cfeNetwork = TerraCompositioAPI.instance().getCFENetworkInstance();
                    CFENetworkMember source = cfeNetwork.getClosestSourceWithCFE(pos, level, connectRange * 2, getPriority());
                    if (source != null) {
                        int transferred = TCUtil.tryCFETransfer(this, source, toAdd);
                        if (transferred > 0)
                            TCUtil.sendCFEParticles((ServerLevel) level,
                                    Vec3.atLowerCornerWithOffset(pos,
                                            this.particleTargetOffset().x,
                                            this.particleTargetOffset().y,
                                            this.particleTargetOffset().z),
                                    source.getBlockPos(),
                                    transferred);
                    }

                }
            }

        }
        if (parts.contains(PathPointerBlock.PPPart.EMITTER)) {
            this.connectRange = 7;
        }

        PathPointerBlockEntity blockEntity = null;
        if (collectorPos == null) {
            collectorPos = getCollector();
        }
        if (collectorPos != null) {
            blockEntity = ((PathPointerBlockEntity) level.getBlockEntity(collectorPos));
        }
        if (blockEntity != null) {
            blockEntity.updateMax();
        }

        transferCFE(level, parts);
    }

    private void transferCFE(Level level, List<PathPointerBlock.PPPart> currentParts) {
        if (lastNode != null) {
            PathPointerBlockEntity lastNodeBE = ((PathPointerBlockEntity) level.getBlockEntity(lastNode));
            BlockState lastNodeBlockState = level.getBlockState(lastNode);
            List<PathPointerBlock.PPPart> lastParts = Arrays.asList(PathPointerBlock.getParts(lastNodeBlockState));
            if (lastParts.contains(PathPointerBlock.PPPart.SENDER) && currentParts.contains(PathPointerBlock.PPPart.RECEIVER)) {
                if (lastNodeBE != null) {
                    if (level.isClientSide) return;
                    int cfeTransfer = TCUtil.tryCFETransfer(this, lastNodeBE, Integer.MAX_VALUE);
                    if (cfeTransfer > 0) {
                        Vec3 sourceCenter = lastNodeBE.getBlockPos().getCenter();
                        Vec3 target = this.getBlockPos().getCenter();
                        for (int i = 0; i < cfeTransfer; i++) {

                            double x = (level.random.nextDouble() - 0.3) * 0.4;
                            double y = (level.random.nextDouble() - 0.3) * 0.4;
                            double z = (level.random.nextDouble() - 0.3) * 0.4;

                            double sourceOffsetX = sourceCenter.x + x;
                            double sourceOffsetY = sourceCenter.y + y;
                            double sourceOffsetZ = sourceCenter.z + z;

                            double targetOffsetX = target.x + x;
                            double targetOffsetY = target.y + y;
                            double targetOffsetZ = target.z + z;


                            float speed = this.getCfeContainer().getCfeTravelSpeed();

                            ((ServerLevel) level).sendParticles(new CFEParticleData(speed),
                                    sourceOffsetX, sourceOffsetY, sourceOffsetZ,
                                    0, // count
                                    targetOffsetX - sourceOffsetX, // xd (направление к цели)
                                    targetOffsetY - sourceOffsetY, // yd
                                    targetOffsetZ - sourceOffsetZ,// zd
                                    Math.sqrt(target.distanceToSqr(sourceCenter)) * 5); // speed
                        }
                    }
                }
            }
        }
    }


    public void updateContainer() {
        if (level != null){
            BlockState pState = level.getBlockState(getBlockPos());

            List<PathPointerBlock.PPPart> currentParts = Arrays.asList(PathPointerBlock.getParts(pState));

            if (currentParts.contains(PathPointerBlock.PPPart.SENDER) && currentParts.contains(PathPointerBlock.PPPart.RECEIVER)) {
                this.setCfeContainer(new LimitlessCFEContainer(this).setCfeTravelSpeed((float) 5 / 20));
            } else {
                this.setCfeContainer(new CFEContainer(this).setCfeTravelSpeed((float) 5 / 20));
            }
        }
    }

    private int getMaxToEmit() {
        List<CFENetworkMember> members = TerraCompositioAPI.instance().getCFENetworkInstance().getAllCFENetworkMembers(level).stream()
                .filter(cfeNetworkMember -> cfeNetworkMember.getBlockPos().closerThan(this.getBlockPos(),this.getLimit()))
                .filter(cfeNetworkMember -> cfeNetworkMember.getPriority() > this.getPriority())
                .filter(cfeNetworkMember -> {
                    if (cfeNetworkMember instanceof PathPointerBlockEntity){
                        List<PathPointerBlock.PPPart> parts = Arrays.asList(PathPointerBlock
                                .getParts(cfeNetworkMember.getLevel().getBlockState(cfeNetworkMember.getBlockPos())));
                        return parts.contains(PathPointerBlock.PPPart.COLLECTOR);
                    }
                    return true;
                })
                .filter(cfeNetworkMember -> CFENetwork.getCFEHandler(cfeNetworkMember)
                        .filter(icfeHandler -> icfeHandler.getFreeSpace() > 0).isPresent())
                .filter(cfeNetworkMember -> cfeNetworkMember.getBlockPos() != collectorPos)
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
                    if (nextBE != null && nextBE.nextNode != null && nextBE.nextNode.equals(currentBE.getBlockPos()))
                        blockEntityQueue.add(nextBE);
                }
            }
        }
        return null;
    }

    // Call only from collector instance
    public void updateMax() {
        Queue<List<PathPointerBlock.PPPart>> partQueue = new LinkedList<>();
        Queue<PathPointerBlockEntity> blockEntityQueue = new LinkedList<>();

        List<PathPointerBlock.PPPart> parts = Arrays.asList(PathPointerBlock.getParts(getBlockState()));
        partQueue.add(parts);

        blockEntityQueue.add(this);

        while (!partQueue.isEmpty() && !blockEntityQueue.isEmpty()){
            List<PathPointerBlock.PPPart> currentPart = partQueue.poll();
            PathPointerBlockEntity currentBE = blockEntityQueue.poll();
            if (currentBE != null
                    && currentPart.contains(PathPointerBlock.PPPart.SENDER)
                    && currentBE.nextNode != null
                    && !currentBE.getBlockPos().equals(currentBE.nextNode) ){
                if (level == null) break;
                BlockState state = level.getBlockState(currentBE.nextNode);
                partQueue.add(Arrays.asList(PathPointerBlock.getParts(state)));
                blockEntityQueue.add((PathPointerBlockEntity) level.getBlockEntity(currentBE.nextNode));
            }
            if (currentBE != null && currentPart.contains(PathPointerBlock.PPPart.EMITTER)){
                if (level == null) break;
                int max = currentBE.getMaxToEmit();
                this.cfeContainer.setMaxCFE(max);
                currentBE.cfeContainer.setMaxCFE(max).setCfeTravelSpeed(5/20f);
                break;
            }
        }
    }

    private int getInSystem() {
        int inSystem = 0;

        if (collectorPos == null) collectorPos = getCollector();

        if (collectorPos != null && level != null) {
            PathPointerBlockEntity collector = (PathPointerBlockEntity) level.getBlockEntity(collectorPos);
            if (collector != null) {
                Queue<PathPointerBlockEntity> blockEntityQueue = new LinkedList<>();

                blockEntityQueue.add(collector);


                while (!blockEntityQueue.isEmpty()) {
                    PathPointerBlockEntity currentBE = blockEntityQueue.poll();
                    if (currentBE != null) {
                        inSystem += currentBE.cfeContainer.getCFE() + currentBE.cfeContainer.getQueued();
                        if (currentBE.nextNode != null) {
                            blockEntityQueue.add((PathPointerBlockEntity) level.getBlockEntity(currentBE.nextNode));
                        }
                    }
                }
            }
        }

        return inSystem;
    }

    public List<PathPointerBlockEntity> getNodes() {
        List<PathPointerBlockEntity> nodes = new ArrayList<>();

        if (collectorPos == null) collectorPos = getCollector();
        if (collectorPos != null && level != null) {
            PathPointerBlockEntity collector = (PathPointerBlockEntity) level.getBlockEntity(collectorPos);
            if (collector != null) {
                Queue<PathPointerBlockEntity> blockEntityQueue = new LinkedList<>();

                blockEntityQueue.add(collector);


                while (!blockEntityQueue.isEmpty()) {
                    PathPointerBlockEntity currentBE = blockEntityQueue.poll();
                    if (currentBE != null) {
                        nodes.add(currentBE);
                        if (currentBE.nextNode != null) {
                            blockEntityQueue.add((PathPointerBlockEntity) level.getBlockEntity(currentBE.nextNode));
                        }
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
        if (collectorPos == null) collectorPos = getCollector();
        if (level != null) {
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
    public boolean updateRotation(boolean simulate) {
        if (nextNode == null && lastNode == null) return false;

        BlockPos blockPos = getBlockPos();
        boolean ignorAngle = Objects.equals(nextNode, blockPos) || Objects.equals(lastNode, blockPos);
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

        float diff = ((float) Math.toDegrees(Math.abs(forward - backward)));

        if ((diff > 91 && diff - 360 < -91) && !ignorAngle) return false;

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
        return true;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null) {
            if (this.lastNode != null) {
                PathPointerBlockEntity last = ((PathPointerBlockEntity) level.getBlockEntity(this.lastNode));
                if (last != null) {
                    last.nextNode = null;
                    last.updateRotation(false);
                }
            }
            if (this.nextNode != null) {
                PathPointerBlockEntity next = ((PathPointerBlockEntity) level.getBlockEntity(this.nextNode));
                if (next != null) {
                    next.lastNode = null;
                    next.updateRotation(false);
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
