package net.sinedkadis.terracompositio.entity.goals;

import lombok.Getter;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ForgeEventFactory;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.entity.custom.FlowCedarEntEntity;
import net.sinedkadis.terracompositio.registries.TCBlockStateProperties;
import net.sinedkadis.terracompositio.registries.TCTags;
import net.sinedkadis.terracompositio.util.helpers.CFEHelper;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

import static net.sinedkadis.terracompositio.registries.TCBlockStateProperties.INFUSED;

public class CFEExtractGoal extends Goal {
    private final FlowCedarEntEntity mob;
    private final Level level;
    private final int extractRange;

    @Getter
    private int extractAnimationTick;

    private BlockPos targetPosition;
    private CFENetworkMember targetMember;

    private int searchCooldown = 0;
    private static final int SEARCH_INTERVAL = 20;

    private @Nullable ICFEHandler cachedHeld;
    private @Nullable ICFEHandler cachedInner;

    public CFEExtractGoal(FlowCedarEntEntity pMob, int extractRange) {
        this.mob = pMob;
        this.level = pMob.level();
        this.extractRange = extractRange;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (searchCooldown-- > 0) return false;
        searchCooldown = SEARCH_INTERVAL;

        if (!ForgeEventFactory.getMobGriefingEvent(this.level, this.mob)) return false;

        cachedHeld = mob.getCapability(TCCapabilities.CFE).resolve().orElse(null);
        cachedInner = mob.getInnerCFEOptional().resolve().orElse(null);

        if (cachedInner == null || cachedInner.getCFE() >= 60) return false;
        if (!isCFEQueueEmpty()) return false;

        targetMember = searchMember();
        if (targetMember != null) return true;

        targetPosition = searchLog();
        return targetPosition != null;
    }

    private @Nullable BlockPos searchLog() {
        BlockPos mobPos = mob.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
                mobPos.offset(-extractRange, -extractRange, -extractRange),
                mobPos.offset(extractRange, extractRange, extractRange)
        )) {
            if (!pos.closerThan(mobPos, extractRange)) continue;
            BlockState blockState = level.getBlockState(pos);
            if (blockState.is(TCTags.Blocks.FLOW_CEDAR_LOGS) && blockState.getValue(INFUSED)) {
                return pos.immutable();
            }
        }
        return null;
    }

    private @Nullable CFENetworkMember searchMember() {
        BlockPos mobPos = mob.blockPosition();

        for (CFENetworkMember member : TerraCompositioAPI.instance()
                .getCFENetworkInstance()
                .getAllCFENetworkMembers(level)) {

            if (!CFEHelper.validMember(member)) continue;
            if (!member.getPos().closerThan(mobPos, extractRange)) continue;
            if (member.getEntity().equals(mob)) continue;
            if (member.getMainHandler().getCFE() <= 0) continue;

            if (member instanceof FlowCedarEntEntity ent) {
                boolean hasEnough = ent.getCapability(TCCapabilities.CFE)
                        .filter(h -> h.getCFE() > 1000)
                        .isPresent();
                if (!hasEnough) continue;
            }

            return member;
        }
        return null;
    }

    @Override
    public void start() {
        this.mob.getNavigation().stop();
        this.extractAnimationTick = 6 * 20;
        mob.setExtracting(true);
    }

    @Override
    public void stop() {
        this.extractAnimationTick = 0;
        this.mob.setExtracting(false);

        this.targetPosition = null;
        this.targetMember = null;
        this.cachedHeld = null;
        this.cachedInner = null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.extractAnimationTick > 0 || !isCFEQueueEmpty();
    }

    private boolean isCFEQueueEmpty() {
        ICFEHandler held = cachedHeld != null
                ? cachedHeld
                : mob.getCapability(TCCapabilities.CFE).resolve().orElse(null);
        ICFEHandler inner = cachedInner != null
                ? cachedInner
                : mob.getInnerCFEOptional().resolve().orElse(null);

        if (held == null || inner == null) return true;
        return held.getCFE() + held.getQueued() + inner.getQueued() <= 0;
    }

    @Override
    public void tick() {
        this.extractAnimationTick = Math.max(0, this.extractAnimationTick - 1);
        this.mob.getNavigation().stop();

        if (targetPosition == null) {
            if (targetMember != null) {
                targetPosition = targetMember.getPos();
            } else {
                return;
            }
        }

        this.mob.lookAt(EntityAnchorArgument.Anchor.EYES, targetPosition.getCenter());

        if (this.extractAnimationTick < 4 * 20) {
            if (!targetPosition.equals(mob.blockPosition())) {
                if (targetMember != null) {
                    CFEHelper.CFETransferBuilder.create()
                            .fromMembers(mob, targetMember)
                            .maxTransfer(1000)
                            .build();
                } else {
                    extractFromLog();
                }
            }
        }

        if (this.extractAnimationTick < 20 && isCFEQueueEmpty()) {
            this.mob.setExtracting(false);
        }
    }

    private void extractFromLog() {
        if (cachedHeld == null) return;
        BlockState blockState = level.getBlockState(targetPosition);
        if (blockState.hasProperty(TCBlockStateProperties.INFUSED)
                && blockState.getValue(TCBlockStateProperties.INFUSED)) {
            level.setBlockAndUpdate(targetPosition,
                    blockState.setValue(TCBlockStateProperties.INFUSED, false));
            cachedHeld.addCFE(100, false);
        }
    }
}
