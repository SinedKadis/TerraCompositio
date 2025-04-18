package net.sinedkadis.terracompositio.compat.jade;

import net.sinedkadis.terracompositio.block.custom.MatterInfuserIOBlock;
import net.sinedkadis.terracompositio.block.entity.MatterInfuserIOBlockEntity;
import net.sinedkadis.terracompositio.compat.jade.providers.MatterInfuserIOComponentProvider;
import snownee.jade.api.*;

@WailaPlugin
public class JadeTerraCompositioPlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(MatterInfuserIOComponentProvider.INSTANCE, MatterInfuserIOBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(MatterInfuserIOComponentProvider.INSTANCE, MatterInfuserIOBlock.class);
    }


}
