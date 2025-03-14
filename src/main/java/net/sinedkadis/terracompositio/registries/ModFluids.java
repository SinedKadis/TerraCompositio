package net.sinedkadis.terracompositio.registries;



import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.fluid.ModFluidRegistryContainer;


public class ModFluids {

    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, TerraCompositio.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS,TerraCompositio.MOD_ID);

    public static final ModFluidRegistryContainer FLOW_FLUID = new ModFluidRegistryContainer("flow",
            FluidType.Properties.create().canDrown(true).canSwim(true).supportsBoating(true).canPushEntity(true).lightLevel(2).motionScale(0.5f).temperature(100),
            ()-> ModFluidRegistryContainer.createExtension(new ModFluidRegistryContainer.ClientExtensions(TerraCompositio.MOD_ID,"flow")/*.still("flow")*/
                    .renderOverlay(null).fogColor(30f/255f,141f/255f,198f/255f)),
            new ModFluidRegistryContainer.AdditionalProperties().levelDecreasePerBlock(1).slopeFindDistance(4).tickRate(1),
            BlockBehaviour.Properties.copy(Blocks.WATER),
            new Item.Properties().stacksTo(1));

    public static final ModFluidRegistryContainer BIRCH_JUICE_FLUID = new ModFluidRegistryContainer("birch_juice",
            FluidType.Properties.create().canDrown(true).canSwim(true).supportsBoating(true).canPushEntity(true),
            () -> ModFluidRegistryContainer.createExtension(new ModFluidRegistryContainer.ClientExtensions(TerraCompositio.MOD_ID,"birch_juice")
                    .renderOverlay(null).fogColor(176f/255f,173f/255f,150f/255f)),
            new ModFluidRegistryContainer.AdditionalProperties(),
            BlockBehaviour.Properties.copy(Blocks.WATER),
            new Item.Properties().stacksTo(1));
}
