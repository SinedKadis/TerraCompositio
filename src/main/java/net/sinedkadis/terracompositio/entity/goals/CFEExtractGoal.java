package net.sinedkadis.terracompositio.entity.goals;

import lombok.Getter;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.cfe.*;
import net.sinedkadis.terracompositio.cfe.CFECapability;
import net.sinedkadis.terracompositio.entity.custom.FlowCedarEntEntity;
import net.sinedkadis.terracompositio.registries.TCBlockStateProperties;
import net.sinedkadis.terracompositio.util.TCUtil;

import java.util.EnumSet;
import java.util.Optional;

public class CFEExtractGoal extends Goal {
    private final FlowCedarEntEntity mob;
    private final Level level;
    @Getter
    private int extractAnimationTick;
    private Vec3 targetPosition;
    private boolean targetIsAcceptableEnt;

    public CFEExtractGoal(FlowCedarEntEntity pMob) {
        this.mob = pMob;
        this.level = pMob.level();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        Optional<ICFEHandler> capability = mob.getInnerCFEOptional().resolve();

        return capability.filter(icfeHandler -> icfeHandler.getCFE() < 60).isPresent()
                && ForgeEventFactory.getMobGriefingEvent(this.level, this.mob)
                && isSourcePosValid()
                && isCFEQueueEmpty();
    }

    public void start() {
        this.mob.getNavigation().stop();
        this.extractAnimationTick = 6*20;
        assert mob.getSourcePos() != null;
        this.targetPosition = mob.getSourcePos().getCenter();
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
        Optional<ICFEHandler> held = this.mob.getCapability(CFECapability.CFE).resolve();
        Optional<ICFEHandler> inner = this.mob.getInnerCFEOptional().resolve();
        if (held.isPresent() && inner.isPresent()){
            ICFEHandler helded = held.get();
            ICFEHandler innered = inner.get();
            return helded.getCfeQueue().isEmpty()
                    && helded.getCFE() <= 0
                    && innered.getCfeQueue().isEmpty();
        }
        return true;
    }

    private boolean isSourcePosValid(){
        BlockPos blockPos = mob.getSourcePos();
        CFENetworkMember memberAt = TerraCompositioAPI.instance().getCFENetworkInstance().getMemberAt(level, blockPos);
        boolean valid = blockPos != null
                && blockPos.closerThan(mob.blockPosition(), mob.getLimit())
                && memberAt != null;
        if (valid){
            boolean targetIsEnt = memberAt instanceof FlowCedarEntEntity;
            targetIsAcceptableEnt = targetIsEnt && ((FlowCedarEntEntity) memberAt).getCapability(CFECapability.CFE)
                    .filter(icfeHandler -> icfeHandler.getCFE() > 1000).isPresent();
        }
        return valid;
    }


    public void tick() {
        this.extractAnimationTick = Math.max(0, this.extractAnimationTick - 1);
        this.mob.getNavigation().stop();
        this.mob.lookAt(EntityAnchorArgument.Anchor.EYES,targetPosition);
        if (this.extractAnimationTick == 4*20) {
            BlockPos blockPos = mob.blockPosition();
            BlockPos targetPos = BlockPos.containing(targetPosition);
            if (!targetPos.equals(blockPos)) {
                Optional<ICFEHandler> cfeHandler = this.mob.getCapability(CFECapability.CFE).resolve();
                if (cfeHandler.isPresent()) {
                    ICFEHandler icfeHandler = cfeHandler.get();
                    CFENetworkMember targetMember = TerraCompositioAPI.instance().getCFENetworkInstance().getMemberAt(level,targetPos);
                    if (targetMember != null) {
                        TCUtil.tryCFETransferWithParticles(mob, targetMember, targetIsAcceptableEnt ? 100 : icfeHandler.getMaxCFE() - icfeHandler.getCFE());
                        return;
                    }
                    BlockState blockState = level.getBlockState(targetPos);
                    if (blockState.hasProperty(TCBlockStateProperties.INFUSED) && blockState.getValue(TCBlockStateProperties.INFUSED)) {
                        //this.level.levelEvent(2001, blockpos1, Block.getId(Blocks.GRASS_BLOCK.defaultBlockState()));
                        level.setBlockAndUpdate(targetPos, blockState.setValue(TCBlockStateProperties.INFUSED, false));
                        int count = icfeHandler.addCFE(100, targetPos, false);
                        FlowCedarEntEntity target = mob;
                        target.getLevel();
                        if (target.getLevel() instanceof ServerLevel serverLevel) {
                            TCUtil.sendCFEParticles(serverLevel, Vec3.atLowerCornerWithOffset(target.getBlockPos(),
                                    target.particleTargetOffset().x,
                                    target.particleTargetOffset().y,
                                    target.particleTargetOffset().z), targetPos, count);
                        }
                    }
                }
            }
        }
        if (this.extractAnimationTick < 20 && isCFEQueueEmpty()){
            this.mob.setExtracting(false);
        }
    }
}
