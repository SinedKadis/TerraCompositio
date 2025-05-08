package net.sinedkadis.terracompositio.compat.jade.providers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec2;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.block.entity.MatterInfuserIOBlockEntity;
import net.sinedkadis.terracompositio.block.entity.ModItemIOCFEBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public enum ModItemIOComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;


    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getServerData().contains("input_c")) {
            int inputCount = blockAccessor.getServerData().getInt("input_c");
            if (inputCount > 0) {
                //iTooltip.add(Component.empty());
                iTooltip.add(Component.translatable("block.terracompositio.item_io." + "input").append(" "));
                if (blockAccessor.getServerData().contains("input")) {
                    Item input = Item.byId(blockAccessor.getServerData().getInt("input"));
                    IElementHelper elements = IElementHelper.get();
                    IElement icon = elements.item(new ItemStack(input),0.7f)
                            .translate(new Vec2(-2,-4))
                            ;
                    iTooltip.add(icon);
                    iTooltip.append(Component.literal(inputCount +"x "));
                    iTooltip.append(Component.translatable(input.getDescriptionId()));
                }
                //iTooltip.add(Component.empty());
            }

        }
        if (blockAccessor.getServerData().contains("output_c")) {
            int outputCount = blockAccessor.getServerData().getInt("output_c");
            if (outputCount > 0) {
                iTooltip.add(Component.translatable("block.terracompositio.item_io." + "output").append(" "));
                if (blockAccessor.getServerData().contains("output")) {
                    Item output = Item.byId(blockAccessor.getServerData().getInt("output"));
                    IElementHelper elements = IElementHelper.get();
                    IElement icon = elements.item(new ItemStack(output), 0.7f)
                            .translate(new Vec2(-2,-4))
                            ;
                    iTooltip.add(icon);
                    iTooltip.append(Component.literal(outputCount +"x "));
                    iTooltip.append(Component.translatable(output.getDescriptionId()));
                }
                //iTooltip.add(Component.empty());
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return TerraCompositio.modLoc("mod_item_io_tooltip");
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        BlockEntity blockEntity = blockAccessor.getBlockEntity();
        ItemStack inputSlot = ItemStack.EMPTY;
        ItemStack outputSlot = ItemStack.EMPTY;
        if (blockEntity instanceof ModItemIOCFEBlockEntity entity){
            inputSlot = entity.getFirstSlot();
            outputSlot = entity.getLastSlot();
        }
        if(blockEntity instanceof MatterInfuserIOBlockEntity){
            return;
        }
        compoundTag.putInt("input_c",inputSlot.getCount());
        compoundTag.putInt("input",Item.getId(inputSlot.getItem()));
        if (inputSlot != outputSlot) {
            compoundTag.putInt("output_c", outputSlot.getCount());
            compoundTag.putInt("output", Item.getId(outputSlot.getItem()));
        }
    }

}
