package net.sinedkadis.terracompositio.compat.patchouli;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.item.custom.CreationFlowJournalItem;
import vazkii.patchouli.api.PatchouliAPI;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.util.ItemStackUtil;

public class TCPatchouliCompat {


    public static void registerMultiblocks() {
        PatchouliAPI.get().registerMultiblock(TerraCompositio.modLoc("multiblocks/matter_infuser"),TCMultiblocks.MATTER_INFUSER_MB.get());
        PatchouliAPI.get().registerMultiblock(TerraCompositio.modLoc("multiblocks/creation_altar"), TCMultiblocks.CREATION_ALTAR_MB.get());

    }

    public static void reloadBookContents(Object book, Level level) {
        if (!CreationFlowJournalItem.patchouliLoaded.get()) return;


        if (book instanceof ItemStack itemStack) {
            book = ItemStackUtil.getBookFromStack(itemStack);
            CreationFlowJournalItem.setFlags(CreationFlowJournalItem.getDay(itemStack));
        }
        if (book instanceof Book book1) {
            book1.reloadContents(level, true);
        }


    }
}
