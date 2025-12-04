package dev.thomasglasser.mineraculous.api.nbt;

import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class MineraculousNbtUtils {
    public static <T> Function<T, Tag> codecEncoder(Codec<T> codec, DynamicOps<Tag> ops) {
        return t -> codec.encodeStart(ops, t).getOrThrow();
    }

    public static <T> Function<Tag, T> codecDecoder(Codec<T> codec, DynamicOps<Tag> ops) {
        return tag -> codec.parse(ops, tag).getOrThrow();
    }

    public static <T> ListTag writeCollection(Collection<T> collection, Function<T, Tag> encoder) {
        ListTag tag = new ListTag();
        for (T value : collection) {
            tag.add(encoder.apply(value));
        }
        return tag;
    }

    public static <T, C extends Collection<T>> C readCollection(Supplier<C> collectionSupplier, ListTag tag, Function<Tag, T> decoder) {
        C collection = collectionSupplier.get();
        for (Tag entry : tag) {
            collection.add(decoder.apply(entry));
        }
        return collection;
    }

    public static <K, V> CompoundTag writeStringKeyedMap(Map<K, V> map, Function<K, String> keyEncoder, Function<V, Tag> valueEncoder) {
        CompoundTag tag = new CompoundTag();
        for (K key : map.keySet()) {
            tag.put(keyEncoder.apply(key), valueEncoder.apply(map.get(key)));
        }
        return tag;
    }

    public static <K, V, M extends Map<K, V>> M readStringKeyedMap(Supplier<M> mapSupplier, CompoundTag tag, Function<String, K> keyDecoder, Function<Tag, V> valueDecoder) {
        M map = mapSupplier.get();
        for (String key : tag.getAllKeys()) {
            map.put(keyDecoder.apply(key), valueDecoder.apply(tag.get(key)));
        }
        return map;
    }

    public static <K, V> CompoundTag writeStringKeyedMultimap(Multimap<K, V> map, Function<K, String> keyEncoder, Function<V, Tag> valueEncoder) {
        CompoundTag tag = new CompoundTag();
        for (K key : map.keySet()) {
            ListTag entries = new ListTag();
            for (V value : map.get(key)) {
                entries.add(valueEncoder.apply(value));
            }
            tag.put(keyEncoder.apply(key), entries);
        }
        return tag;
    }

    public static <K, V, M extends Multimap<K, V>> M readStringKeyedMultimap(Supplier<M> mapSupplier, CompoundTag tag, Function<String, K> keyDecoder, Function<Tag, V> valueDecoder) {
        M map = mapSupplier.get();
        for (String key : tag.getAllKeys()) {
            ListTag entries = tag.getList(key, Tag.TAG_COMPOUND);
            for (Tag entry : entries) {
                map.put(keyDecoder.apply(key), valueDecoder.apply(entry));
            }
        }
        return map;
    }

    public static <R, C, V> CompoundTag writeStringRowKeyedTable(Table<R, C, V> table, Function<R, String> rowEncoder, Function<C, Tag> columnEncoder, Function<V, Tag> valueEncoder) {
        CompoundTag tag = new CompoundTag();
        for (R rowKey : table.rowKeySet()) {
            ListTag entries = new ListTag();
            Map<C, V> row = table.row(rowKey);
            for (Map.Entry<C, V> entry : row.entrySet()) {
                CompoundTag pair = new CompoundTag();
                pair.put("Key", columnEncoder.apply(entry.getKey()));
                pair.put("Value", valueEncoder.apply(entry.getValue()));
                entries.add(pair);
            }
            tag.put(rowEncoder.apply(rowKey), entries);
        }
        return tag;
    }

    public static <R, C, V, T extends Table<R, C, V>> T readStringRowKeyedTable(Supplier<T> tableSupplier, CompoundTag tag, Function<String, R> rowDecoder, Function<Tag, C> columnDecoder, Function<Tag, V> valueDecoder) {
        T table = tableSupplier.get();
        for (String rowString : tag.getAllKeys()) {
            ListTag entries = tag.getList(rowString, Tag.TAG_COMPOUND);
            R rowKey = rowDecoder.apply(rowString);
            for (int i = 0; i < entries.size(); i++) {
                CompoundTag entry = entries.getCompound(i);
                Tag keyTag = entry.get("Key");
                Tag valueTag = entry.get("Value");
                table.put(rowKey, columnDecoder.apply(keyTag), valueDecoder.apply(valueTag));
            }
        }
        return table;
    }
}
