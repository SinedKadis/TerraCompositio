package net.sinedkadis.terracompositio.registries;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.sinedkadis.terracompositio.TerraCompositio;

import java.util.Objects;


public class TCTags {
    public static class  Blocks{
        public static final TagKey<Block> FLOW_CEDAR_LOGS = tag("flow_cedar_logs_blocks");
        public static final TagKey<Block> CREATE_WRENCH_PICKUP = BlockTags.create(Objects.requireNonNull(
                ResourceLocation.tryBuild("create", "wrench_pickup")));




        private static TagKey<Block> tag(String name){
            return BlockTags.create(Objects.requireNonNull(ResourceLocation.tryBuild(TerraCompositio.MOD_ID, name)));
        }

    }
    public static class  Items{
        public static final TagKey<Item> FLOW_CEDAR_LOGS = tag("flow_cedar_logs_items");
        public static final TagKey<Item> UNSTABLE_TECHNETIUM = tag("unstable_technetium");
        public static final TagKey<Item> WRENCHES = ItemTags.create(Objects.requireNonNull(
                ResourceLocation.tryBuild("forge","tools/wrench")));
        public static final TagKey<Item> TORCHES = tag("torches");



        private static TagKey<Item> tag(String name){
            return ItemTags.create(Objects.requireNonNull(ResourceLocation.tryBuild(TerraCompositio.MOD_ID, name)));
        }
    }
}
