package net.sinedkadis.terracompositio.entity.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMember;
import net.sinedkadis.terracompositio.api.networks.ecf.IECFHandler;
import net.sinedkadis.terracompositio.entity.custom.FlowCedarEntEntity;
import net.sinedkadis.terracompositio.registries.TCTags;
import net.sinedkadis.terracompositio.util.helpers.ECFHelper;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;

import static net.sinedkadis.terracompositio.registries.TCBlockStateProperties.INFUSED;

public class ReachSourceGoal extends Goal {
    private final FlowCedarEntEntity mob;
    private final double speedModifier;
    private final Level level;
    private Vec3 targetPosition;
    private final int stopDistance;
    private final int searchLimit;

    private int searchCooldown = 0;
    private static final int SEARCH_INTERVAL = 40;

    public ReachSourceGoal(FlowCedarEntEntity mob, double speed, int searchLimit, int stopDistance) {
        this.mob = mob;
        this.speedModifier = speed;
        this.searchLimit = searchLimit;
        this.stopDistance = stopDistance;
        this.level = mob.level();

        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (targetPosition != null) return true;
        if (searchCooldown-- > 0) return false;
        searchCooldown = SEARCH_INTERVAL;

        Optional<IECFHandler> cfeHandler = mob.getInnerECFOptional().resolve();
        if (cfeHandler.isEmpty() || cfeHandler.get().getECF() > 6) return false;
        if (mob.isExtracting() || mob.isHolding()) return false;

        ECFNetworkMember member = searchMember();
        if (member != null) {
            if (member.getPos().closerThan(mob.blockPosition(), stopDistance)) return false;
            targetPosition = member.getPos().getCenter();
            return true;
        }

        BlockPos logPos = searchLog();
        if (logPos != null) {
            if (logPos.closerThan(mob.blockPosition(), stopDistance)) return false;
            targetPosition = logPos.getCenter();
            return true;
        }

        return false;
    }

    private @Nullable BlockPos searchLog() {
        BlockPos mobPos = mob.blockPosition();
        int limit = searchLimit;

        for (BlockPos pos : BlockPos.betweenClosed(
                mobPos.offset(-limit, -limit, -limit),
                mobPos.offset(limit, limit, limit)
        )) {
            if (pos.closerThan(mobPos, stopDistance)) continue;
            if (!pos.closerThan(mobPos, limit)) continue;

            BlockState blockState = level.getBlockState(pos);
            if (blockState.is(TCTags.Blocks.FLOW_CEDAR_LOGS) && blockState.getValue(INFUSED)) {
                return pos.immutable();
            }
        }
        return null;
    }

    private @Nullable ECFNetworkMember searchMember() {
        BlockPos mobPos = mob.blockPosition();

        for (ECFNetworkMember member : TerraCompositioAPI.instance()
                .getECFNetworkInstance()
                .getAllECFNetworkMembers(level)) {

            if (!ECFHelper.validMember(member)) continue;

            BlockPos memberPos = member.getPos();
            if (!memberPos.closerThan(mobPos, searchLimit)) continue;
            if (memberPos.closerThan(mobPos, stopDistance)) continue;
            if (member.getEntity().equals(mob)) continue;
            if (member.getMainHandler().getECF() <= 0) continue;

            if (member instanceof FlowCedarEntEntity ent) {
                boolean hasEnough = ent.getCapability(TCCapabilities.ECF)
                        .filter(h -> h.getECF() > 1000)
                        .isPresent();
                if (!hasEnough) continue;
            }

            return member;
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
        return !this.mob.getNavigation().isDone() && !isInStopRange();
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
        this.targetPosition = null;
        this.searchCooldown = SEARCH_INTERVAL / 2;
    }

    private boolean isInStopRange() {
        return mob.position().closerThan(targetPosition, stopDistance);
    }
}
