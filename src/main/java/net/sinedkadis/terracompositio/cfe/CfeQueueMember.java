package net.sinedkadis.terracompositio.cfe;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.util.TCUtil;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CfeQueueMember {
    private final List<Vec3> offsetVec = new ArrayList<>();
    private final ICFEHandler targetHandler;
    private final int cfeCount;
    private double distanceToTarget;
    private double passedDistance = 0;
    private Vec3 startMiddlePoint;
    private Vec3 lastEndPos;
    private final CFENetworkMemberEntity target;
    private boolean isEnded = false;
    private boolean isReached;
    private final boolean isDynamicTarget;
    private final ServerLevel level;
    private final boolean doRender;
    private final float cfeTravelSpeed;
    private double lastPassed = 0;
    private int limit;

    public CfeQueueMember(int cfeCount, ICFEHandler target, ICFEHandler source, ServerLevel level,boolean doRender) {
        this.cfeCount = cfeCount;
        this.isDynamicTarget = target.getAttachedMember() instanceof CFENetworkMemberEntity
                || target.getAttachedMember() instanceof Player;
        this.level = level;
        this.doRender = doRender;
        boolean isDynamicSource = source.getAttachedMember() instanceof CFENetworkMemberEntity;
        this.limit = target.getAttachedMember().getLimit();
        this.cfeTravelSpeed = source.getCfeTravelSpeed();
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
            this.targetHandler = target;
            this.lastEndPos = ((CFENetworkMemberEntity) target.getAttachedMember()).getPosition();
            this.distanceToTarget = startMiddlePoint.distanceTo(lastEndPos);
        } else {
            this.target = null;
            this.targetHandler = null;
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
        this.doRender = true;
        if (isDynamicTarget) {
            this.target = (CFENetworkMemberEntity) target.getAttachedMember();
            this.targetHandler = target;
            this.lastEndPos = ((CFENetworkMemberEntity) target.getAttachedMember()).getPosition();
            this.distanceToTarget = startMiddlePoint.distanceTo(lastEndPos);
        } else {
            this.target = null;
            this.targetHandler = null;
            this.distanceToTarget = startMiddlePoint.distanceTo(target.getOffset().apply(target.getBlockPos().getCenter()));
            TCUtil.sendCFEParticles(level, lastEndPos, startMiddlePoint, cfeCount);
        }
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
                if (target == null) {
                    isEnded = true;
                    isReached = false;
                    return;
                }
                if (target instanceof Player && limit <= 0) limit = 10;
                if (newPassed >= 1) {


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
                    TCUtil.sendCFEParticles(level, endMiddlePoint, startMiddlePoint, cfeCount, offsetVec,cfeTravelSpeed);

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

}
