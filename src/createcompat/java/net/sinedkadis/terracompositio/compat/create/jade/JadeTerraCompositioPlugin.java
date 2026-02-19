package net.sinedkadis.terracompositio.compat.create.jade;

import net.sinedkadis.terracompositio.compat.create.block.custom.CedarGearboxBlock;
import net.sinedkadis.terracompositio.compat.create.block.entity.CedarGearboxBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class JadeTerraCompositioPlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {


        registration.registerBlockDataProvider(CedarGearboxBlockEntityComponentProvider.INSTANCE, CedarGearboxBlockEntity.class);



    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {



        registration.registerBlockComponent(CedarGearboxBlockEntityComponentProvider.INSTANCE, CedarGearboxBlock.class);

    }


}
