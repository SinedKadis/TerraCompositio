package net.sinedkadis.terracompositio.compat.jade;

import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.sinedkadis.terracompositio.block.custom.*;
import net.sinedkadis.terracompositio.block.entity.*;
import net.sinedkadis.terracompositio.compat.jade.providers.MatterInfuserIOComponentProvider;
import net.sinedkadis.terracompositio.compat.jade.providers.ModCFEBlockEntityComponentProvider;
import net.sinedkadis.terracompositio.compat.jade.providers.ModItemIOComponentProvider;
import net.sinedkadis.terracompositio.compat.jade.providers.TimePassageDesorberComponentProvider;
import snownee.jade.api.*;

@WailaPlugin
public class JadeTerraCompositioPlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {

        registration.registerBlockDataProvider(ModCFEBlockEntityComponentProvider.INSTANCE, ModCFEBlockEntity.class);
        registration.registerBlockDataProvider(ModCFEBlockEntityComponentProvider.INSTANCE, FlowInfuserBlockEntity.class);
       // registration.registerBlockDataProvider(ModCFEBlockEntityComponentProvider.INSTANCE, MatterInfuserIOBlockEntity.class);
        registration.registerBlockDataProvider(ModCFEBlockEntityComponentProvider.INSTANCE, AbstractDesorberBlockEntity.class);
        registration.registerBlockDataProvider(ModCFEBlockEntityComponentProvider.INSTANCE, AbstractFurnaceBlockEntity.class);

        registration.registerBlockDataProvider(ModItemIOComponentProvider.INSTANCE, ModItemIOCFEBlockEntity.class);

        registration.registerBlockDataProvider(MatterInfuserIOComponentProvider.INSTANCE, MatterInfuserIOBlockEntity.class);

        registration.registerBlockDataProvider(TimePassageDesorberComponentProvider.INSTANCE,TimePassageDesorberBlockEntity.class);

    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {


        registration.registerBlockComponent(ModCFEBlockEntityComponentProvider.INSTANCE, ModCFEBaseEntityBlock.class);
        registration.registerBlockComponent(ModCFEBlockEntityComponentProvider.INSTANCE, FlowInfuserBlock.class);
        registration.registerBlockComponent(ModCFEBlockEntityComponentProvider.INSTANCE, MatterInfuserIOBlock.class);
        registration.registerBlockComponent(ModCFEBlockEntityComponentProvider.INSTANCE, AbstractFurnaceBlock.class);

        registration.registerBlockComponent(ModItemIOComponentProvider.INSTANCE, ModIOBaseEntityBlock.class);
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
