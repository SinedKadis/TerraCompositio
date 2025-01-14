package net.sinedkadis.terracompositio.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.sinedkadis.terracompositio.TerraCompositio;


public class ModTags {
    public static class  Blocks{
        public static final TagKey<Block> FLOW_LEAKABLE = tag("flow_cedar_logs");


        private static TagKey<Block> tag(String name){
            return BlockTags.create(new ResourceLocation(TerraCompositio.MOD_ID, name));
        }
    }
    public static class  Items{
        public static final TagKey<Item> FLOW_CEDAR_LOGS = tag("flow_cedar_logs");

        private static TagKey<Item> tag(String name){
            return ItemTags.create(new ResourceLocation(TerraCompositio.MOD_ID, name));
        }
    }
}
