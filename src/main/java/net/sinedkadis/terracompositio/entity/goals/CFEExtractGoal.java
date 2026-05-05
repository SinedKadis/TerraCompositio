package net.sinedkadis.terracompositio.entity.goals;

import lombok.Getter;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.cfe.*;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.entity.custom.FlowCedarEntEntity;
import net.sinedkadis.terracompositio.registries.TCBlockStateProperties;
import net.sinedkadis.terracompositio.registries.TCTags;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static net.sinedkadis.terracompositio.registries.TCBlockStateProperties.INFUSED;

public class CFEExtractGoal extends Goal {
    private final FlowCedarEntEntity mob;
    private final Level level;
    private final int extractRange;

    @Getter
    private int extractAnimationTick;

    private BlockPos targetPosition;
    private CFENetworkMember targetMember;


    public CFEExtractGoal(FlowCedarEntEntity pMob, int extractRange) {
        this.mob = pMob;
        this.level = pMob.level();
        this.extractRange = extractRange;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        Optional<ICFEHandler> capability = mob.getInnerCFEOptional().resolve();

        boolean cfeQueueEmpty = isCFEQueueEmpty();
        boolean mobGriefingEvent = ForgeEventFactory.getMobGriefingEvent(this.level, this.mob);
        boolean cfePresent = capability.filter(icfeHandler -> icfeHandler.getCFE() < 60).isPresent();
        boolean extractionAllow = cfePresent
                && mobGriefingEvent
                && cfeQueueEmpty;
        if (extractionAllow) {
            targetMember = searchMember();
            if (targetMember != null) return true;
            targetPosition = searchLog();
            return targetPosition != null;
        }
        return false;
    }

    private @Nullable BlockPos searchLog() {
        Optional<BlockPos> log = TCUtil.getNearBlocks(mob.getPos(), extractRange).stream()
                .filter(pos -> {
                    BlockState blockState = level.getBlockState(pos);
                    return blockState.is(TCTags.Blocks.FLOW_CEDAR_LOGS) && blockState.getValue(INFUSED);
                })
                .findAny();

        return log.orElse(null);
    }

    private @Nullable CFENetworkMember searchMember() {
        Optional<CFENetworkMember> source = TerraCompositioAPI.instance().getCFENetworkInstance()
                .getAllCFENetworkMembers(level).stream()
                //Validate: entity is not removed
                .filter(TCUtil::validMember)
                //Distance check: needs to be in search limit
                .filter(member -> member.getPos().closerThan(mob.getPos(),extractRange))
                //Entity check: don`t take from itself
                .filter(member -> !member.getEntity().equals(mob))
                //CFE check: needs to be not empty
                .filter(member -> member.getMainHandler().getCFE() > 0)
                //Ent check: not ent or ent with 1000> cfe
                .filter(member -> {
                    boolean targetIsEnt = member instanceof FlowCedarEntEntity;
                    boolean targetIsAcceptableEnt = targetIsEnt && ((FlowCedarEntEntity) member).getCapability(TCCapabilities.CFE)
                            .filter(icfeHandler -> icfeHandler.getCFE() > 1000).isPresent();
                    return !targetIsEnt || targetIsAcceptableEnt;
                })
                .findAny();
        return source.orElse(null);
    }

    public void start() {
        this.mob.getNavigation().stop();
        this.extractAnimationTick = 6*20;
        mob.setExtracting(true);
    }

    public void stop() {
        this.extractAnimationTick = 0;
        //this.mob.abortCFEConsume();
        this.mob.setExtracting(false);
    }

    public boolean canContinueToUse() {
        return this.extractAnimationTick > 0 || !isCFEQueueEmpty();
    }

    private boolean isCFEQueueEmpty() {
        Optional<ICFEHandler> held = this.mob.getCapability(TCCapabilities.CFE).resolve();
        Optional<ICFEHandler> inner = this.mob.getInnerCFEOptional().resolve();
        if (held.isPresent() && inner.isPresent()){
            ICFEHandler helded = held.get();
            ICFEHandler innered = inner.get();
            return helded.getCFE() + helded.getQueued() + innered.getQueued() <= 0;
        }
        return true;
    }

    public void tick() {
        this.extractAnimationTick = Math.max(0, this.extractAnimationTick - 1);
        this.mob.getNavigation().stop();
        if (targetPosition == null && targetMember != null) {
            targetPosition = targetMember.getPos();
        } else if (targetMember == null) return;
        this.mob.lookAt(EntityAnchorArgument.Anchor.EYES,targetPosition.getCenter());
        if (this.extractAnimationTick < 4*20) {
            BlockPos blockPos = mob.blockPosition();
            if (!targetPosition.equals(blockPos)) {
                if (targetMember != null) {
                    TCUtil.tryCFETransfer(mob, targetMember,1000);
                    return;
                }

                Optional<ICFEHandler> cfeHandler = this.mob.getCapability(TCCapabilities.CFE).resolve();
                if (cfeHandler.isPresent()) {
                    ICFEHandler icfeHandler = cfeHandler.get();
                    BlockState blockState = level.getBlockState(targetPosition);
                    if (blockState.hasProperty(TCBlockStateProperties.INFUSED) && blockState.getValue(TCBlockStateProperties.INFUSED)) {
                        //this.level.levelEvent(2001, blockpos1, Block.getId(Blocks.GRASS_BLOCK.defaultBlockState()));
                        level.setBlockAndUpdate(targetPosition, blockState.setValue(TCBlockStateProperties.INFUSED, false));
                        icfeHandler.addCFE(100, false);
                    }
                }
            }
        }
        if (this.extractAnimationTick < 20 && isCFEQueueEmpty()){
            this.mob.setExtracting(false);
        }
    }
}
