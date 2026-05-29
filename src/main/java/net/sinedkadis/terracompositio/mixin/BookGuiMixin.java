package net.sinedkadis.terracompositio.mixin;



import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.compat.patchouli.TCPatchouliCompat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.common.book.Book;

@Mixin(GuiBook.class)
@OnlyIn(Dist.CLIENT)
public abstract class BookGuiMixin {

    @Final
    @Shadow(remap = false)
    public Book book;

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        ItemStack bookStack = book.getBookItem();

        if (!TCPatchouliCompat.isMyBook(bookStack)) return;

        terraCompositio$updateBookContent(book, level);

    }

    @Unique
    private void terraCompositio$updateBookContent(Book book, Level level) {
        try {
            TCPatchouliCompat.reloadBookContents(book, level);
        } catch (Exception e) {
            TerraCompositio.LOGGER.error("Failed to update book content", e);
        }
    }
}
