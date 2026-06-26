package net.sinedkadis.terracompositio.entity.goals;

import lombok.Getter;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ForgeEventFactory;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.helpers.ECFHelper;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMember;
import net.sinedkadis.terracompositio.api.networks.ecf.IECFHandler;
import net.sinedkadis.terracompositio.api.registries.TCBlockStateProperties;
import net.sinedkadis.terracompositio.api.registries.TCCapabilities;
import net.sinedkadis.terracompositio.entity.custom.FlowCedarEntEntity;
import net.sinedkadis.terracompositio.registries.TCTags;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

import static net.sinedkadis.terracompositio.api.registries.TCBlockStateProperties.INFUSED;

public class ECFExtractGoal extends Goal {
    private final FlowCedarEntEntity mob;
    private final Level level;
    private final int extractRange;

    @Getter
    private int extractAnimationTick;

    private BlockPos targetPosition;
    private ECFNetworkMember targetMember;

    private int searchCooldown = 0;
    private static final int SEARCH_INTERVAL = 20;

    private @Nullable IECFHandler cachedHeld;
    private @Nullable IECFHandler cachedInner;

    public ECFExtractGoal(FlowCedarEntEntity pMob, int extractRange) {
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

        cachedHeld = mob.getCapability(TCCapabilities.ECF).resolve().orElse(null);
        cachedInner = mob.getInnerECFOptional().resolve().orElse(null);

        if (cachedInner == null || cachedInner.getECF() >= 6) return false;
        if (!isECFQueueEmpty()) return false;

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

    private @Nullable ECFNetworkMember searchMember() {
        BlockPos mobPos = mob.blockPosition();

        for (ECFNetworkMember member : TerraCompositioAPI.instance()
                .getECFNetworkInstance()
                .getAllECFNetworkMembers(level)) {

            if (!ECFHelper.validMember(member)) continue;
            if (!member.getPos().closerThan(mobPos, extractRange)) continue;
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
        return this.extractAnimationTick > 0 || !isECFQueueEmpty();
    }

    private boolean isECFQueueEmpty() {
        IECFHandler held = cachedHeld != null
                ? cachedHeld
                : mob.getCapability(TCCapabilities.ECF).resolve().orElse(null);
        IECFHandler inner = cachedInner != null
                ? cachedInner
                : mob.getInnerECFOptional().resolve().orElse(null);

        if (held == null || inner == null) return true;
        return held.getECF() + held.getQueued() + inner.getQueued() <= 0;
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

        if (this.extractAnimationTick < 5 * 20) {
            if (!targetPosition.equals(mob.blockPosition())) {
                if (targetMember != null) {
                    ECFHelper.newTransfer()
                            .targetAndSource(mob, targetMember)
                            .maxTransfer(1000)
                            .speed(2 / 20f)
                            .build();
                } else {
                    extractFromLog();
                }
            }
        }

        if (this.extractAnimationTick < 20 && isECFQueueEmpty()) {
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
            cachedHeld.addECF(100, false);
        }
    }
}
