package net.sinedkadis.terracompositio.compat.jade;

import net.minecraft.resources.ResourceLocation;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.custom.*;
import net.sinedkadis.terracompositio.block.entity.*;
import net.sinedkadis.terracompositio.cfe.burst.CFEBurstProjectileEntity;
import net.sinedkadis.terracompositio.compat.jade.providers.*;
import net.sinedkadis.terracompositio.entity.custom.CFECloudEntity;
import net.sinedkadis.terracompositio.entity.custom.FlowCedarEntEntity;
import snownee.jade.api.*;

@WailaPlugin
public class JadeTerraCompositioPlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {

        registration.registerBlockDataProvider(TCBlockEntityComponentProvider.INSTANCE, TCBlockEntity.class);

        registration.registerBlockDataProvider(MatterInfuserIOComponentProvider.INSTANCE, MatterInfuserUnitBlockEntity.class);

        registration.registerBlockDataProvider(TimePassageDesorberComponentProvider.INSTANCE,TimePassageDesorberBlockEntity.class);
        
        registration.registerEntityDataProvider(TCCFEEntityComponentProvider.INSTANCE, FlowCedarEntEntity.class);
        registration.registerEntityDataProvider(TCCFEEntityComponentProvider.INSTANCE, CFECloudEntity.class);
        registration.registerEntityDataProvider(TCCFEEntityComponentProvider.INSTANCE, CFEBurstProjectileEntity.class);

        registration.registerBlockDataProvider(PPComponentProvider.INSTANCE, PathPointerBlockEntity.class);


    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {


        registration.registerBlockComponent(TCBlockEntityComponentProvider.INSTANCE, TCBaseEntityBlock.class);

        registration.registerBlockComponent(MatterInfuserIOComponentProvider.INSTANCE, MatterInfuserIOBlock.class);
        registration.addConfig(MatterInfuserIOComponentProvider.INSTANCE.catalystConfigRL(),true);
        registration.addConfig(MatterInfuserIOComponentProvider.INSTANCE.cfeTickConfigRL(),true);
        registration.addConfig(MatterInfuserIOComponentProvider.INSTANCE.decayChanceConfigRL(),true);
        registration.addConfig(MatterInfuserIOComponentProvider.INSTANCE.durationConfigRL(),true);

        registration.registerBlockComponent(TimePassageDesorberComponentProvider.INSTANCE,TimePassageDesorberBlock.class);
        registration.addConfig(TimePassageDesorberComponentProvider.INSTANCE.timeConfigRL(),true);
        registration.addConfig(TimePassageDesorberComponentProvider.INSTANCE.chanceConfigRL(),true);

        registration.registerEntityComponent(TCCFEEntityComponentProvider.INSTANCE, FlowCedarEntEntity.class);
        registration.registerEntityComponent(TCCFEEntityComponentProvider.INSTANCE, CFECloudEntity.class);
        registration.registerEntityComponent(TCCFEEntityComponentProvider.INSTANCE, CFEBurstProjectileEntity.class);

        registration.registerBlockComponent(PPComponentProvider.INSTANCE, PathPointerBlock.class);
        registration.addConfig(PPComponentProvider.partsConfigRL(),true);
        registration.addConfig(debugConfigRL(),false);
    }

    public static ResourceLocation debugConfigRL() {
        return TerraCompositio.modLoc("debug_config");
    }



}
