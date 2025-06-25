package net.sinedkadis.terracompositio.entity.goals;

import lombok.Getter;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.sinedkadis.terracompositio.api.networks.cfe.*;
import net.sinedkadis.terracompositio.cfe.CFECapability;
import net.sinedkadis.terracompositio.entity.custom.FlowCedarEntEntity;
import net.sinedkadis.terracompositio.registries.TCBlockStateProperties;
import net.sinedkadis.terracompositio.util.TCUtil;

import java.util.Optional;

public class TreeExtractGoal extends Goal {
    private final FlowCedarEntEntity mob;
    private final Level level;
    @Getter
    private int extractAnimationTick;
    private int cd;
    private Vec3 targetPosition;

    public TreeExtractGoal(FlowCedarEntEntity pMob) {
        this.mob = pMob;
        this.level = pMob.level();
        //this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        Optional<ICFEHandler> capability = this.mob.getCapability(CFECapability.CFE).resolve();
        return capability.filter(icfeHandler -> icfeHandler.getCFE() < 60
                && ForgeEventFactory.getMobGriefingEvent(this.level, this.mob)
                && mob.getSourcePos() != null).isPresent();
    }

    public void start() {
        PathNavigation navigation = this.mob.getNavigation();
        navigation.stop();
        this.extractAnimationTick = 13*20;
        this.cd = 5*20;
        assert mob.getSourcePos() != null;
        this.targetPosition = mob.getSourcePos().getCenter();
        mob.setExtracting(true);





    }

    public void stop() {
        this.extractAnimationTick = 0;
        this.mob.abortCFEConsume();
        this.mob.setExtracting(false);
    }

    public boolean canContinueToUse() {
        if (this.extractAnimationTick + cd > 0) {
            return true;
        } else {
            this.mob.setExtracting(false);
            return false;
        }
    }


    public void tick() {
        this.extractAnimationTick = Math.max(0, this.extractAnimationTick - 1);
        if (this.extractAnimationTick == 9 * 20) {
            this.mob.lookAt(EntityAnchorArgument.Anchor.EYES,targetPosition);
            BlockPos blockPos = mob.blockPosition();
            BlockPos targetPos = BlockPos.containing(targetPosition);
            if (!targetPos.equals(blockPos)) {
                Optional<ICFEHandler> cfeHandler = CFENetwork.getCFEHandler(mob);
                BlockEntity blockEntity = level.getBlockEntity(targetPos);
                if (cfeHandler.isPresent()) {
                    ICFEHandler icfeHandler = cfeHandler.get();
                    if (blockEntity instanceof CFENetworkMemberBE member) {
                        TCUtil.tryCFETransferWithParticles(mob, member, icfeHandler.getMaxCFE() - icfeHandler.getCFE());
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
    }
}
