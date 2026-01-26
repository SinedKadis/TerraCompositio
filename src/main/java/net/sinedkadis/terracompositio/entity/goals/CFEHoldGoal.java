package net.sinedkadis.terracompositio.entity.goals;

import net.minecraft.world.entity.ai.goal.Goal;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.entity.custom.FlowCedarEntEntity;

import java.util.EnumSet;
import java.util.Optional;

public class CFEHoldGoal extends Goal {
    private final FlowCedarEntEntity mob;

    public CFEHoldGoal(FlowCedarEntEntity pMob) {
        this.mob = pMob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return !isCFEQueueEmpty();
    }

    public void start() {
        this.mob.getNavigation().stop();
        mob.setExtracting(false);
        mob.setHolding(true);
    }

    public void stop() {
        this.mob.setHolding(false);
    }

    private boolean isCFEQueueEmpty() {
        Optional<ICFEHandler> held = this.mob.getCapability(TCCapabilities.CFE).resolve();
        Optional<ICFEHandler> inner = this.mob.getInnerCFEOptional().resolve();
        if (held.isPresent() && inner.isPresent()){
            ICFEHandler helded = held.get();
            ICFEHandler innered = inner.get();
            return helded.getCFE()+helded.getQueued()+innered.getQueued() <= 0;
        }
        return true;
    }


    public void tick() {
        this.mob.getNavigation().stop();
        if (isCFEQueueEmpty()){
            this.mob.setHolding(false);
        }
    }
}
