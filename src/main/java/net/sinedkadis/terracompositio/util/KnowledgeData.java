package net.sinedkadis.terracompositio.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Сырые данные для оверлея знаний.
 * Собирается на сервере через IHaveKnowledge.collectKnowledgeData(),
 * сериализуется в пакет и на клиенте превращается в List<Component>
 * через IHaveKnowledge.addTooltipLines().
 * <p>
 * Поддерживает два типа записей:
 * - текстовые (translation key + аргументы)
 * - предметные (ItemStack)
 */
public final class KnowledgeData {

    // ─── Типы записей ────────────────────────────────────────────

    private final List<Entry> entries = new ArrayList<>();

    public static KnowledgeData fromNetwork(FriendlyByteBuf buf) {
        KnowledgeData data = new KnowledgeData();
        int count = buf.readVarInt();
        for (int i = 0; i < count; i++) {
            CompoundTag tag = buf.readNbt();
            if (tag == null) continue;
            String type = tag.getString("type");
            Entry entry = switch (type) {
                case "text" -> TextEntry.fromNbt(tag);
                case "item" -> ItemEntry.fromNbt(tag);
                default -> null;
            };
            if (entry != null) data.entries.add(entry);
        }
        return data;
    }

    /**
     * Добавить текстовую строку без аргументов.
     */
    public KnowledgeData addText(String translationKey) {
        entries.add(new TextEntry(translationKey));
        return this;
    }

    // ─── Данные ──────────────────────────────────────────────────

    /**
     * Добавить текстовую строку с аргументами (числа, имена и т.д.).
     */
    public KnowledgeData addText(String translationKey, Object... args) {
        String[] strArgs = new String[args.length];
        for (int i = 0; i < args.length; i++) strArgs[i] = String.valueOf(args[i]);
        entries.add(new TextEntry(translationKey, strArgs));
        return this;
    }

    // ─── API для сервера (вызывается в collectKnowledgeData) ─────

    /**
     * Добавить предмет.
     */
    public KnowledgeData addItem(ItemStack stack) {
        if (!stack.isEmpty()) entries.add(new ItemEntry(stack.copy()));
        return this;
    }

    /**
     * Все записи (только для чтения).
     */
    public List<Entry> entries() {
        return Collections.unmodifiableList(entries);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeVarInt(entries.size());
        for (Entry entry : entries) {
            CompoundTag tag = entry.toNbt();
            buf.writeNbt(tag);
        }
    }

    public sealed interface Entry permits TextEntry, ItemEntry {
        CompoundTag toNbt();
    }

    // ─── Сериализация ────────────────────────────────────────────

    /**
     * Текстовая строка. Используй translatable-ключи, не хардкодь текст.
     * args хранятся как строки; если нужно число — String.valueOf(n).
     */
    public record TextEntry(String translationKey, String[] args) implements Entry {

        public TextEntry(String translationKey) {
            this(translationKey, new String[0]);
        }

        static TextEntry fromNbt(CompoundTag tag) {
            String key = tag.getString("key");
            ListTag argsTag = tag.getList("args", Tag.TAG_STRING);
            String[] args = new String[argsTag.size()];
            for (int i = 0; i < argsTag.size(); i++) {
                args[i] = argsTag.getString(i);
            }
            return new TextEntry(key, args);
        }

        public CompoundTag toNbt() {
            CompoundTag tag = new CompoundTag();
            tag.putString("type", "text");
            tag.putString("key", translationKey);
            ListTag argsTag = new ListTag();
            for (String arg : args) {
                net.minecraft.nbt.StringTag st = net.minecraft.nbt.StringTag.valueOf(arg);
                argsTag.add(st);
            }
            tag.put("args", argsTag);
            return tag;
        }
    }

    /**
     * Предмет с количеством. ItemStack уже содержит count внутри себя.
     */
    public record ItemEntry(ItemStack stack) implements Entry {

        static ItemEntry fromNbt(CompoundTag tag) {
            return new ItemEntry(ItemStack.of(tag.getCompound("stack")));
        }

        public CompoundTag toNbt() {
            CompoundTag tag = new CompoundTag();
            tag.putString("type", "item");
            tag.put("stack", stack.save(new CompoundTag()));
            return tag;
        }
    }
}
