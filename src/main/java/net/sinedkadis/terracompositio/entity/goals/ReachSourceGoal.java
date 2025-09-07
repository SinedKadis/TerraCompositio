package net.sinedkadis.terracompositio.entity.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.api.networks.cfe.CFECapability;
import net.sinedkadis.terracompositio.entity.custom.FlowCedarEntEntity;
import net.sinedkadis.terracompositio.registries.TCTags;
import net.sinedkadis.terracompositio.util.TCUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static net.sinedkadis.terracompositio.registries.TCBlockStateProperties.INFUSED;

public class ReachSourceGoal extends Goal {
    private final FlowCedarEntEntity mob;
    private final double speedModifier;
    private Vec3 targetPosition;
    private final int stopDistance;
    private final int searchLimit;

    public ReachSourceGoal(FlowCedarEntEntity mob, double speed, int searchLimit, int stopDistance) {
        this.mob = mob;
        this.speedModifier = speed;
        this.searchLimit = searchLimit;
        this.stopDistance = stopDistance;
    }

    @Override
    public boolean canUse() {
        Optional<ICFEHandler> cfeHandler = mob.getInnerCFEOptional().resolve();
        if (cfeHandler.isPresent() && cfeHandler.get().getCFE() <= 60){
            BlockPos sourcePos = mob.getSourcePos();
            if (sourcePos != null && sourcePos.closerThan(mob.blockPosition(),mob.getLimit()-1)) {
                return false;
            }
            CFENetworkMember randomSourceInRange = TerraCompositioAPI.instance().getCFENetworkInstance().getRandomSourceInRange(mob.blockPosition(), mob.level(), searchLimit);
            if (randomSourceInRange != null){
                boolean targetIsEnt = randomSourceInRange instanceof FlowCedarEntEntity;
                boolean targetIsAcceptableEnt = targetIsEnt && ((FlowCedarEntEntity) randomSourceInRange).getCapability(CFECapability.CFE)
                        .filter(icfeHandler -> icfeHandler.getCFE() > 1000).isPresent();
                if (!targetIsEnt || targetIsAcceptableEnt) {
                    BlockPos blockPos = randomSourceInRange.getBlockPos();
                    mob.setSourcePos(blockPos);
                    if (TCUtil.distSqr(blockPos, mob.blockPosition()) > (long) stopDistance * stopDistance) {
                        targetPosition = blockPos.getCenter();
                        return true;
                    }
                }
            }
            List<BlockPos> list = new ArrayList<>(TCUtil.getNearBlocks(mob.getBlockPos(), 10).stream()
                    .filter(pos1 -> mob.level().getBlockState(pos1).is(TCTags.Blocks.FLOW_CEDAR_LOGS))
                    .filter(pos2 -> mob.level().getBlockState(pos2).getValue(INFUSED))
                    .toList());
            if (!list.isEmpty()) {
                Collections.shuffle(list);
                BlockPos pos = list.get(0);
                mob.setSourcePos(pos);
                if (TCUtil.distSqr(pos, mob.blockPosition()) > (long) stopDistance *stopDistance) {
                    targetPosition = pos.getCenter();
                    return true;
                }
            }

        }
        return false;
    }

    @Override
    public void start() {

        PathNavigation navigation = this.mob.getNavigation();


        Vec3 direction = this.mob.position().subtract(this.targetPosition).normalize();
        Vec3 adjustedTarget = this.targetPosition.add(direction.scale(stopDistance));

        Path path = navigation.createPath(adjustedTarget.x, adjustedTarget.y, adjustedTarget.z, 0);

        navigation.moveTo(path, this.speedModifier);
    }

    @Override
    public boolean canContinueToUse() {
        boolean flag1 = !this.mob.getNavigation().isDone();
        boolean flag2 = !isInStopRange();
        return flag1 && flag2;
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
    }

    private boolean isInStopRange() {
        double distance = this.mob.distanceToSqr(this.targetPosition.x, this.targetPosition.y, this.targetPosition.z);
        long stop = (long) (stopDistance-1) * (stopDistance-1);
        return distance <= stop;
    }
}
