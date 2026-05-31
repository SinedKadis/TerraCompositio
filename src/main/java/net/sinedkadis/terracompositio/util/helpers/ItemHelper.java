package net.sinedkadis.terracompositio.util.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.ArrayList;
import java.util.List;

public class ItemHelper {
    public static List<ItemStack> getContainerContents(ItemStack containerStack) {
        if (containerStack.isEmpty()) {
            return List.of();
        }

        CompoundTag stackTag = containerStack.getTag();
        if (stackTag == null) {
            return List.of();
        }

        CompoundTag blockEntityTag = stackTag.getCompound("BlockEntityTag");
        if (blockEntityTag.contains("Items", CompoundTag.TAG_LIST)) {
            return getItemsFromNBT(blockEntityTag.getList("Items", CompoundTag.TAG_COMPOUND));
        }

        if (stackTag.contains("Items", CompoundTag.TAG_LIST)) {
            return getItemsFromNBT(stackTag.getList("Items", CompoundTag.TAG_COMPOUND));
        }

        if (stackTag.contains("Inventory", CompoundTag.TAG_LIST)) {
            return getItemsFromNBT(stackTag.getList("Inventory", CompoundTag.TAG_COMPOUND));
        }

        return List.of();
    }

    private static List<ItemStack> getItemsFromNBT(ListTag itemsTag) {
        List<ItemStack> items = new ArrayList<>(itemsTag.size());

        for (int i = 0; i < itemsTag.size(); i++) {
            CompoundTag itemTag = itemsTag.getCompound(i);
            ItemStack itemStack = ItemStack.of(itemTag);
            if (!itemStack.isEmpty()) {
                items.add(itemStack);
            }
        }

        return items;
    }

    public static void dropContents(BlockEntity blockEntity, int... slots) {
        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(itemHandler -> {
            SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
            if (slots.length == 0) {
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    inventory.setItem(i, itemHandler.getStackInSlot(i));
                }
            } else {
                for (int i = 0; i < slots.length; i++) {
                    int slot = slots[i];
                    inventory.setItem(i, itemHandler.getStackInSlot(slot));
                }
            }
            Level level = blockEntity.getLevel();
            BlockPos worldPosition = blockEntity.getBlockPos();
            if (level != null) {
                Containers.dropContents(level, worldPosition, inventory);
            }
        });
    }
}
