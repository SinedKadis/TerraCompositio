package net.sinedkadis.terracompositio.compat.jade;

import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.sinedkadis.terracompositio.block.custom.*;
import net.sinedkadis.terracompositio.block.entity.*;
import net.sinedkadis.terracompositio.compat.jade.providers.MatterInfuserIOComponentProvider;
import net.sinedkadis.terracompositio.compat.jade.providers.TCCFEBlockEntityComponentProvider;
import net.sinedkadis.terracompositio.compat.jade.providers.TCItemIOComponentProvider;
import net.sinedkadis.terracompositio.compat.jade.providers.TimePassageDesorberComponentProvider;
import snownee.jade.api.*;

@WailaPlugin
public class JadeTerraCompositioPlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {

        registration.registerBlockDataProvider(TCCFEBlockEntityComponentProvider.INSTANCE, TCCFEBlockEntity.class);
        registration.registerBlockDataProvider(TCCFEBlockEntityComponentProvider.INSTANCE, FlowInfuserBlockEntity.class);
       // registration.registerBlockDataProvider(ModCFEBlockEntityComponentProvider.INSTANCE, MatterInfuserIOBlockEntity.class);
        registration.registerBlockDataProvider(TCCFEBlockEntityComponentProvider.INSTANCE, AbstractDesorberBlockEntity.class);
        registration.registerBlockDataProvider(TCCFEBlockEntityComponentProvider.INSTANCE, AbstractFurnaceBlockEntity.class);

        registration.registerBlockDataProvider(TCItemIOComponentProvider.INSTANCE, TCItemIOCFEBlockEntity.class);

        registration.registerBlockDataProvider(MatterInfuserIOComponentProvider.INSTANCE, MatterInfuserIOBlockEntity.class);

        registration.registerBlockDataProvider(TimePassageDesorberComponentProvider.INSTANCE,TimePassageDesorberBlockEntity.class);

    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {


        registration.registerBlockComponent(TCCFEBlockEntityComponentProvider.INSTANCE, TCCFEBaseEntityBlock.class);
        registration.registerBlockComponent(TCCFEBlockEntityComponentProvider.INSTANCE, FlowInfuserBlock.class);
        registration.registerBlockComponent(TCCFEBlockEntityComponentProvider.INSTANCE, MatterInfuserIOBlock.class);
        registration.registerBlockComponent(TCCFEBlockEntityComponentProvider.INSTANCE, AbstractFurnaceBlock.class);

        registration.registerBlockComponent(TCItemIOComponentProvider.INSTANCE, TCIOBaseEntityBlock.class);
        //registration.registerBlockComponent(ModItemIOComponentProvider.INSTANCE, FlowInfuserBlock.class);
        //registration.registerBlockComponent(ModItemIOComponentProvider.INSTANCE, FlowCedarCasingBlock.class);
        //registration.registerBlockComponent(ModItemIOComponentProvider.INSTANCE, MatterInfuserIOBlock.class);

        registration.registerBlockComponent(MatterInfuserIOComponentProvider.INSTANCE, MatterInfuserIOBlock.class);
        registration.addConfig(MatterInfuserIOComponentProvider.INSTANCE.catalystConfigRL(),true);
        registration.addConfig(MatterInfuserIOComponentProvider.INSTANCE.cfeTickConfigRL(),true);
        registration.addConfig(MatterInfuserIOComponentProvider.INSTANCE.decayChanceConfigRL(),true);
        registration.addConfig(MatterInfuserIOComponentProvider.INSTANCE.durationConfigRL(),true);

        registration.registerBlockComponent(TimePassageDesorberComponentProvider.INSTANCE,TimePassageDesorberBlock.class);
        registration.addConfig(TimePassageDesorberComponentProvider.INSTANCE.timeConfigRL(),true);
        registration.addConfig(TimePassageDesorberComponentProvider.INSTANCE.chanceConfigRL(),true);

    }


}
