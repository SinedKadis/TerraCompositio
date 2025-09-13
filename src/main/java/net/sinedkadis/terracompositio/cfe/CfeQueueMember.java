package net.sinedkadis.terracompositio.cfe;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.api.dummies.DummyCFEHandler;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.util.TCUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class CfeQueueMember {
    private final List<Vec3> offsetVec = new ArrayList<>();
    private ICFEHandler targetHandler = null;
    @Setter
    private int cfeCount = 0;
    private double distanceToTarget;
    private double passedDistance = 0;
    private Vec3 startMiddlePoint;
    private Vec3 lastEndPos;
    private CFENetworkMemberEntity target;
    private boolean isEnded = false;
    private boolean isReached;
    private boolean isDynamicTarget = false;
    private final ServerLevel level;
    private boolean doRender;
    private float cfeTravelSpeed;
    private double lastPassed = 0;
    private int limit;
    private UUID targetUUID;
    private int handlerIndex;

    public CfeQueueMember(int cfeCount, ICFEHandler target, ICFEHandler source, ServerLevel level,boolean doRender) {
        this.cfeCount = cfeCount;
        this.isDynamicTarget = target.getAttachedMember() instanceof CFENetworkMemberEntity
                || target.getAttachedMember() instanceof Player;
        this.level = level;
        this.doRender = doRender;
        boolean isDynamicSource = source.getAttachedMember() instanceof CFENetworkMemberEntity;
        this.limit = source.getAttachedMember().getLimit();
        this.cfeTravelSpeed = target.getCfeTravelSpeed();
        if (isDynamicSource) {
            this.startMiddlePoint = source.getOffset().apply(((CFENetworkMemberEntity) source.getAttachedMember()).getPosition());
        } else {
            this.startMiddlePoint = source.getOffset().apply(source.getBlockPos().getCenter());
        }
        if (doRender) {
            genOffsets(cfeCount, level);
        }
        if (isDynamicTarget) {
            this.target = (CFENetworkMemberEntity) target.getAttachedMember();
            this.targetUUID = ((Entity) target.getAttachedMember()).getUUID();
            this.targetHandler = target;
            this.lastEndPos = ((CFENetworkMemberEntity) target.getAttachedMember()).getPosition();
            this.distanceToTarget = startMiddlePoint.distanceTo(lastEndPos);
        } else {
            this.distanceToTarget = startMiddlePoint.distanceTo(target.getOffset().apply(target.getBlockPos().getCenter()));
            this.lastEndPos = target.getBlockPos().getCenter();
            if (doRender) {
                TCUtil.sendCFEParticles(level, lastEndPos, startMiddlePoint, cfeCount);
            }
        }
    }

    private void genOffsets(int cfeCount, ServerLevel level) {
        RandomSource random = level.random;
        for (int i = 0; i < cfeCount; i++) {
            offsetVec.add(TCUtil.getSpreadParticleOffset(random, cfeCount));
        }
    }

    public CfeQueueMember(int cfeCount, ICFEHandler target, BlockPos sourcePos, ServerLevel level) {
        this.cfeCount = cfeCount;
        this.isDynamicTarget = target.getAttachedMember() instanceof CFENetworkMemberEntity
                || target.getAttachedMember() instanceof Player;
        this.limit = target.getAttachedMember().getLimit();
        this.cfeTravelSpeed = target.getCfeTravelSpeed();
        this.startMiddlePoint = sourcePos.getCenter();
        this.level = level;

        if (isDynamicTarget) {
            this.target = (CFENetworkMemberEntity) target.getAttachedMember();
            this.targetHandler = target;
            this.handlerIndex = targetHandler.getIndex();
            this.lastEndPos = ((CFENetworkMemberEntity) target.getAttachedMember()).getPosition();
            this.distanceToTarget = startMiddlePoint.distanceTo(lastEndPos);
        } else {
            this.distanceToTarget = startMiddlePoint.distanceTo(target.getOffset().apply(target.getBlockPos().getCenter()));
            if (doRender) {
                TCUtil.sendCFEParticles(level, lastEndPos, startMiddlePoint, cfeCount);
            }
        }
    }

    public CfeQueueMember(ServerLevel level) {
        this.level = level;
    }



    public boolean isEnded() {
        if (!isDynamicTarget)
            return distanceToTarget <= 0;
        return isEnded;
    }


    public void memberTick() {
        if (level != null){
            if (isDynamicTarget) {
                double newPassed = passedDistance - lastPassed;
                if (target == null) target = (CFENetworkMemberEntity) level.getEntity(targetUUID);
                if (target == null) {
                    isEnded = true;
                    isReached = false;
                    return;
                }
                if (target instanceof Player && limit <= 0) limit = 10;
                if (newPassed >= 1) {

                    if (targetHandler == null) {
                        List<ICFEHandler> list = target.getCfeHandlers().stream()
                                .map(icfeHandlerLazyOptional -> icfeHandlerLazyOptional.orElse(DummyCFEHandler.instance))
                                .filter(icfeHandler -> icfeHandler.getIndex() == handlerIndex).toList();
                        if (!list.isEmpty())
                            targetHandler = list.get(0);
                    }


                    startMiddlePoint = startMiddlePoint.lerp(lastEndPos, newPassed / distanceToTarget);
                    lastEndPos = targetHandler.getOffset().apply(target.getPosition());
                    distanceToTarget = startMiddlePoint.distanceTo(lastEndPos);

                    if (distanceToTarget <= 1) {
                        isEnded = true;
                        isReached = true;
                        return;
                    }

                    Vec3 endMiddlePoint = startMiddlePoint.lerp(lastEndPos, 1 / distanceToTarget);
                    if (offsetVec.isEmpty()) genOffsets(cfeCount, level);
                    TCUtil.sendCFEParticles(level, endMiddlePoint, startMiddlePoint, cfeCount, offsetVec);

                    lastPassed = passedDistance;
                }
                if (passedDistance >= limit) {
                    isEnded = true;
                    isReached = false;
                }
                passedDistance += cfeTravelSpeed;
            } else {
                distanceToTarget -= cfeTravelSpeed;
                if (distanceToTarget <= 1) {
                    isEnded = true;
                    isReached = true;
                }
            }
        }

    }


    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("cfe", cfeCount);
        nbt.putDouble("dist", distanceToTarget);
        nbt.putFloat("cfe_ts", cfeTravelSpeed);
        nbt.putBoolean("do_render",doRender);

        if (isDynamicTarget) {
            nbt.putDouble("passed", passedDistance);
            nbt.putDouble("recomp", lastPassed);

            nbt.putDouble("smp_x", startMiddlePoint.x);
            nbt.putDouble("smp_y", startMiddlePoint.y);
            nbt.putDouble("smp_z", startMiddlePoint.z);

            nbt.putDouble("lep_x", lastEndPos.x);
            nbt.putDouble("lep_y", lastEndPos.y);
            nbt.putDouble("lep_z", lastEndPos.z);

            nbt.putUUID("targetUUID", targetUUID);
            nbt.putInt("handler_index",handlerIndex);
            nbt.putInt("limit", limit);
        }
        return nbt;
    }


    public void deserializeNBT(CompoundTag nbt) {
        cfeCount = nbt.getInt("cfe");
        distanceToTarget = nbt.getDouble("dist");
        cfeTravelSpeed = nbt.getFloat("cfe_ts");
        doRender = nbt.getBoolean("do_render");
        if (nbt.contains("passed")) {
            passedDistance = nbt.getDouble("passed");
            lastPassed = nbt.getDouble("recomp");

            double x = nbt.getDouble("smp_x");
            double y = nbt.getDouble("smp_y");
            double z = nbt.getDouble("smp_z");

            startMiddlePoint = new Vec3(x, y, z);

            x = nbt.getDouble("lep_x");
            y = nbt.getDouble("lep_y");
            z = nbt.getDouble("lep_z");

            lastEndPos = new Vec3(x, y, z);

            targetUUID = nbt.getUUID("targetUUID");

            handlerIndex = nbt.getInt("handler_index");

            limit = nbt.getInt("limit");

            isDynamicTarget = true;
        }
    }

}
