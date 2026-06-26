package net.sinedkadis.terracompositio.entity.goals;

import net.minecraft.world.entity.ai.goal.Goal;
import net.sinedkadis.terracompositio.api.networks.ecf.IECFHandler;
import net.sinedkadis.terracompositio.api.registries.TCCapabilities;
import net.sinedkadis.terracompositio.entity.custom.FlowCedarEntEntity;

import java.util.EnumSet;
import java.util.Optional;

public class ECFHoldGoal extends Goal {
    private final FlowCedarEntEntity mob;

    public ECFHoldGoal(FlowCedarEntEntity pMob) {
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
        Optional<IECFHandler> held = this.mob.getCapability(TCCapabilities.ECF).resolve();
        Optional<IECFHandler> inner = this.mob.getInnerECFOptional().resolve();
        if (held.isPresent() && inner.isPresent()){
            IECFHandler helded = held.get();
            IECFHandler innered = inner.get();
            return helded.getECF() + helded.getQueued() + innered.getQueued() <= 0;
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
