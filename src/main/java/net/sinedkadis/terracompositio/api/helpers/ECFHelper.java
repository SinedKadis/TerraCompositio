package net.sinedkadis.terracompositio.api.helpers;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.AnyNetworkMember;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMember;
import net.sinedkadis.terracompositio.api.networks.ecf.IECFHandler;

/**
 * The class with methods, that helps with ECF.
 */
public class ECFHelper {

    /**
     * Checks for validating given member.
     *
     * @param target the member
     * @return the true if member is valid
     */
    public static boolean validMember(AnyNetworkMember target) {
        if (target == null) return false;
        if (target.getEntityInstance() instanceof BlockEntity memberBE) {
            return !memberBE.isRemoved();
        }
        if (target.getEntityInstance() instanceof Entity memberEntity) {
            return !memberEntity.isRemoved();
        }
        return false;
    }

    /**
     * Tries to make transfer between two members.
     * If blocks are close enough, just takes ECF from source and adds it to target, else sends it like burst
     *
     * @param target      the target member. Used for navigation in sending burst, actually receives {@link IECFHandler#getMainHandler()},
     *                    so passing {@link IECFHandler} like argument only make sense when two blocks are close enough
     * @param source      the source member. {@link IECFHandler} can be used as argument
     * @param maxTransfer the max amount of ECF that can be transferred
     * @param speed       the speed of th burst, 1 means 1 block per tick
     */
    public static void doECFTransfer(ECFNetworkMember target,
                                     ECFNetworkMember source,
                                     int maxTransfer,
                                     float speed) {
        if (!validMember(target)) return;
        if (!validMember(source)) return;
        if (target.getMainHandler().getMaxECF() == Integer.MAX_VALUE) maxTransfer = Integer.MAX_VALUE;
        IECFHandler sourceMainHandler = source.getMainHandler();
        int taken = sourceMainHandler.takeECF(maxTransfer, true);
        IECFHandler targetMainHandler = target.getMainHandler();
        int added = targetMainHandler.addECF(taken, true);

        if (added > 0) {
            if (target.getEntityInstance().tc$getBlockPos()
                    .closerThan(source.getEntityInstance().tc$getBlockPos(), 2)
                    && !(target instanceof Entity))
                added = targetMainHandler.addECF(added, false);
            else {
                added = sourceMainHandler.sendECF(target, added, speed);
            }

            sourceMainHandler.takeECF(added, false);
        }

    }

    /**
     * New transfer builder to make transfer.
     *
     * @return the builder
     */
    public static ECFTransferBuilder newTransfer() {
        return new ECFTransferBuilder();
    }

    /**
     * Convenient builder for ECF transfers.
     */
    public static class ECFTransferBuilder {
        /**
         * The Target member.
         */
        ECFNetworkMember target = null;
        /**
         * The Source member.
         */
        ECFNetworkMember source = null;

        /**
         * The max amount of ECF that can be transferred.
         */
        int maxTransfer = TerraCompositioAPI.instance().getECFNetworkInstance().getECFTransferLimit();
        /**
         * The Speed. Default to 1 block per second.
         */
        float speed = 1 / 20f;


        /**
         * Target and source for transfer. Mandatory.
         *
         * @param target the target
         * @param source the source
         * @return the ecf transfer builder
         */
        public ECFTransferBuilder targetAndSource(ECFNetworkMember target, ECFNetworkMember source) {
            this.target = target;
            this.source = source;
            return this;
        }

        /**
         * Max transfer amount. Optional.
         *
         * @param maxTransfer the max transfer
         * @return the ecf transfer builder
         */
        public ECFTransferBuilder maxTransfer(int maxTransfer) {
            this.maxTransfer = maxTransfer;
            return this;
        }

        /**
         * Speed. Optional.
         *
         * @param speed the speed
         * @return the ecf transfer builder
         */
        public ECFTransferBuilder speed(float speed) {
            this.speed = speed;
            return this;
        }

        /**
         * Executes transfer with given in builder data.
         */
        public void build() {
            if (target != null && source != null) {
                doECFTransfer(target, source, maxTransfer, speed);
            }
        }

    }
}
