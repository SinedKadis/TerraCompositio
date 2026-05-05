package net.sinedkadis.terracompositio.entity.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.entity.custom.FlowCedarEntEntity;
import net.sinedkadis.terracompositio.registries.TCTags;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static net.sinedkadis.terracompositio.registries.TCBlockStateProperties.INFUSED;

public class ReachSourceGoal extends Goal {
    private final FlowCedarEntEntity mob;
    private final double speedModifier;
    private final Level level;
    private Vec3 targetPosition;
    private final int stopDistance;
    private final int searchLimit;

    public ReachSourceGoal(FlowCedarEntEntity mob, double speed, int searchLimit, int stopDistance) {
        this.mob = mob;
        this.speedModifier = speed;
        this.searchLimit = searchLimit;
        this.stopDistance = stopDistance;
        this.level = mob.level();
    }

    @Override
    public boolean canUse() {
        Optional<ICFEHandler> cfeHandler = mob.getInnerCFEOptional().resolve();
        if (cfeHandler.isPresent() && cfeHandler.get().getCFE() <= 60 && !mob.isExtracting() && !mob.isHolding()){
            CFENetworkMember cfeNetworkMember = searchMember();
            if (cfeNetworkMember != null) {
                targetPosition = cfeNetworkMember.getPos().getCenter();
                return true;
            }
            BlockPos logPos = searchLog();
            if (logPos != null) {
                targetPosition = logPos.getCenter();
                return true;
            }
        }
        return targetPosition != null;
    }

    private @Nullable BlockPos searchLog() {
        Set<BlockPos> logs = TCUtil.getNearBlocks(mob.getPos(), searchLimit).stream()
                .filter(pos -> {
                    BlockState blockState = level.getBlockState(pos);
                    return blockState.is(TCTags.Blocks.FLOW_CEDAR_LOGS) && blockState.getValue(INFUSED);
                })
                .collect(Collectors.toSet());

        for (BlockPos pos : logs) {
            if (!pos.closerThan(mob.blockPosition(),stopDistance)) {
                return pos;
            } else return null;
        }
        return null;
    }

    private @Nullable CFENetworkMember searchMember() {
        Set<CFENetworkMember> randomSourceInRange = TerraCompositioAPI.instance().getCFENetworkInstance()
                .getAllCFENetworkMembers(level).stream()
                //Validate: entity is not removed
                .filter(TCUtil::validMember)
                //Distance check: needs to be in search limit
                .filter(member -> member.getPos().closerThan(mob.getPos(),searchLimit))
                //Entity check: don`t take from itself
                .filter(member -> !member.getEntity().equals(mob))
                //CFE check: needs to be not empty
                .filter(member -> member.getMainHandler().getCFE() > 0)
                .collect(Collectors.toSet());
        for (CFENetworkMember member : randomSourceInRange) {
            boolean targetIsEnt = member instanceof FlowCedarEntEntity;
            boolean targetIsAcceptableEnt = targetIsEnt && ((FlowCedarEntEntity) member).getCapability(TCCapabilities.CFE)
                    .filter(icfeHandler -> icfeHandler.getCFE() > 1000).isPresent();
            if (!targetIsEnt || targetIsAcceptableEnt) {
                BlockPos blockPos = member.getPos();
                if (!blockPos.closerThan(mob.blockPosition(), stopDistance)) {
                    return member;
                } else return null;
            }
        }
        return null;
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
        return mob.position().closerThan(targetPosition,stopDistance);
    }
}
