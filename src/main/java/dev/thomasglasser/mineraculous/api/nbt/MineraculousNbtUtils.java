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
    /**
     * Creates an encoder for the provided {@link Codec} and (typically registry-aware) {@link DynamicOps}.
     *
     * @param codec The codec to encode with
     * @param ops   The ops to encode with
     * @return The encoder {@link Function}
     * @param <T> The type of object to encode
     */
    public static <T> Function<T, Tag> codecEncoder(Codec<T> codec, DynamicOps<Tag> ops) {
        return t -> codec.encodeStart(ops, t).getOrThrow();
    }

    /**
     * Creates a decoder for the provided {@link Codec} and (typically registry-aware) {@link DynamicOps}.
     *
     * @param codec The codec to decode with
     * @param ops   The ops to decode with
     * @return The decoder {@link Function}
     * @param <T> The type of object to decode
     */
    public static <T> Function<Tag, T> codecDecoder(Codec<T> codec, DynamicOps<Tag> ops) {
        return tag -> codec.parse(ops, tag).getOrThrow();
    }

    /**
     * Encodes a {@link Collection} to a {@link ListTag} with the provided encoder.
     *
     * @param collection The collection to encode
     * @param encoder    The encoder to use for the objects in the collection
     * @return The encoded {@link ListTag}
     * @param <T> The type of object in the collection
     */
    public static <T> ListTag writeCollection(Collection<T> collection, Function<T, Tag> encoder) {
        ListTag tag = new ListTag();
        for (T value : collection) {
            tag.add(encoder.apply(value));
        }
        return tag;
    }

    /**
     * Decodes a {@link ListTag} to a {@link Collection} with the provided decoder.
     *
     * @param collectionSupplier The supplier for the collection creation
     * @param tag                The {@link ListTag} to decode
     * @param decoder            The decoder to use for the objects in the collection
     * @return The decoded {@link Collection}
     * @param <T> The type of object in the collection
     * @param <C> The type of collection
     */
    public static <T, C extends Collection<T>> C readCollection(Supplier<C> collectionSupplier, ListTag tag, Function<Tag, T> decoder) {
        C collection = collectionSupplier.get();
        for (Tag entry : tag) {
            collection.add(decoder.apply(entry));
        }
        return collection;
    }

    /**
     * Encodes a {@link Map} to a {@link CompoundTag} with the provided encoders.
     *
     * @param map          The map to encode
     * @param keyEncoder   The encoder to use for the map keys
     * @param valueEncoder The encoder to use for the map values
     * @return The encoded {@link CompoundTag}
     * @param <K> The type of object of the map keys
     * @param <V> The type of object of the map values
     */
    public static <K, V> CompoundTag writeStringKeyedMap(Map<K, V> map, Function<K, String> keyEncoder, Function<V, Tag> valueEncoder) {
        CompoundTag tag = new CompoundTag();
        for (K key : map.keySet()) {
            tag.put(keyEncoder.apply(key), valueEncoder.apply(map.get(key)));
        }
        return tag;
    }

    /**
     * Decodes a {@link CompoundTag} to a {@link Map} with the provided decoders.
     *
     * @param mapSupplier  The supplier for the map creation
     * @param tag          The {@link CompoundTag} to decode
     * @param keyDecoder   The decoder to use for the map keys
     * @param valueDecoder The decoder to use for the map values
     * @return The decoded map
     * @param <K> The type of object of the map keys
     * @param <V> The type of object of the map values
     * @param <M> The type of map
     */
    public static <K, V, M extends Map<K, V>> M readStringKeyedMap(Supplier<M> mapSupplier, CompoundTag tag, Function<String, K> keyDecoder, Function<Tag, V> valueDecoder) {
        M map = mapSupplier.get();
        for (String key : tag.getAllKeys()) {
            map.put(keyDecoder.apply(key), valueDecoder.apply(tag.get(key)));
        }
        return map;
    }

    /**
     * Encodes a {@link Multimap} to a {@link CompoundTag} with the provided encoders.
     *
     * @param map          The map to encode
     * @param keyEncoder   The encoder to use for the map keys
     * @param valueEncoder The encoder to use for the map values
     * @return The encoded {@link CompoundTag}
     * @param <K> The type of object of the map keys
     * @param <V> The type of object of the map values
     */
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

    /**
     * Decodes a {@link CompoundTag} to a {@link Multimap} with the provided decoders.
     *
     * @param mapSupplier  The supplier for the map creation
     * @param tag          The {@link CompoundTag} to decode
     * @param keyDecoder   The decoder to use for the map keys
     * @param valueDecoder The decoder to use for the map values
     * @return The decoded {@link Multimap}
     * @param <K> The type of object of the map keys
     * @param <V> The type of object of the map values
     * @param <M> The type of multimap
     */
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

    /**
     * Encodes a {@link Table} to a {@link CompoundTag} with the provided encoders.
     *
     * @param table         The table to encode
     * @param rowEncoder    The encoder to use for the table rows
     * @param columnEncoder The encoder to use for the table columns
     * @param valueEncoder  The encoder to use for the table values
     * @return The encoded {@link CompoundTag}
     * @param <R> The type of object of the table rows
     * @param <C> The type of object of the table columns
     * @param <V> The type of object of the table values
     */
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

    /**
     * Decodes a {@link CompoundTag} to a {@link Table} with the provided decoders.
     *
     * @param tableSupplier The supplier for table creation
     * @param tag           The {@link CompoundTag} to decode
     * @param rowDecoder    The decoder for the table rows
     * @param columnDecoder The decoder for the table columns
     * @param valueDecoder  The decoder for the table values
     * @return The decoded {@link Table}
     * @param <R> The type of object of the table rows
     * @param <C> The type of object of the table columns
     * @param <V> The type of object of the table values
     * @param <T> The type of table
     */
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
