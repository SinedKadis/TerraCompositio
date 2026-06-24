package net.sinedkadis.terracompositio.util.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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
            return readItemList(blockEntityTag.getList("Items", CompoundTag.TAG_COMPOUND));
        }

        if (stackTag.contains("Items", CompoundTag.TAG_LIST)) {
            return readItemList(stackTag.getList("Items", CompoundTag.TAG_COMPOUND));
        }

        if (stackTag.contains("Inventory", CompoundTag.TAG_LIST)) {
            return readItemList(stackTag.getList("Inventory", CompoundTag.TAG_COMPOUND));
        }

        return List.of();
    }

    public static List<ItemStack> readItemList(ListTag itemsTag) {
        List<ItemStack> items = new ArrayList<>(itemsTag.size());

        for (int i = 0; i < itemsTag.size(); i++) {
            CompoundTag itemTag = itemsTag.getCompound(i);
            ItemStack itemStack = ItemStack.of(itemTag);
            items.add(itemStack);
        }

        return items;
    }

    public static ListTag writeItemList(Iterable<ItemStack> stacks) {
        return writeCompoundList(stacks, itemStack -> {
            CompoundTag tag = new CompoundTag();
            itemStack.save(tag);
            return tag;
        });
    }

    public static <T> ListTag writeCompoundList(Iterable<T> list, Function<T, CompoundTag> serializer) {
        ListTag listNBT = new ListTag();
        list.forEach(t -> {
            CompoundTag apply = serializer.apply(t);
            if (apply == null)
                return;
            listNBT.add(apply);
        });
        return listNBT;
    }

    public static void dropContents(BlockEntity blockEntity, int... slots) {
        dropContents(blockEntity, ForgeCapabilities.ITEM_HANDLER, slots);
    }

    public static <T extends IItemHandler> void dropContents(BlockEntity blockEntity, Capability<T> cap, int... slots) {
        blockEntity.getCapability(cap).ifPresent(itemHandler -> {
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
