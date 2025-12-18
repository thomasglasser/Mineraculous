package dev.thomasglasser.mineraculous.api.world.miraculous;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/// Holds all {@link MiraculousData}s for an entity
public class MiraculousesData {
    public static final UnboundedMapCodec<Holder<Miraculous>, MiraculousData> MAP_CODEC = Codec.unboundedMap(Miraculous.CODEC, MiraculousData.CODEC);
    public static final Codec<MiraculousesData> CODEC = MAP_CODEC.xmap(MiraculousesData::new, data -> data.map);
    public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousesData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(
                    Object2ObjectOpenHashMap::new,
                    Miraculous.STREAM_CODEC,
                    MiraculousData.STREAM_CODEC),
            set -> set.map,
            MiraculousesData::new);

    private final Map<Holder<Miraculous>, MiraculousData> map;

    public MiraculousesData() {
        this.map = new Object2ObjectOpenHashMap<>();
    }

    public MiraculousesData(Map<Holder<Miraculous>, MiraculousData> map) {
        this.map = new Object2ObjectOpenHashMap<>(map);
    }

    @ApiStatus.Internal
    public void tick(LivingEntity entity, ServerLevel level) {
        forEach((miraculous, data) -> data.tick(entity, level, miraculous));
    }

    /**
     * Gets the {@link MiraculousData} for the provided {@link Miraculous}
     *
     * @param key The miraculous to get the data for
     * @return The miraculous data
     */
    public MiraculousData get(Holder<Miraculous> key) {
        return map.getOrDefault(key, new MiraculousData());
    }

    @ApiStatus.Internal
    /// @see MiraculousData#save(Holder, Entity)
    public MiraculousData put(Entity entity, Holder<Miraculous> key, MiraculousData value) {
        MiraculousData data = map.put(key, value);
        save(entity);
        return data;
    }

    /**
     * Returns an immutable set of miraculous keys.
     *
     * @return An immutable set of miraculous keys
     */
    public Set<Holder<Miraculous>> keySet() {
        return ImmutableSet.copyOf(map.keySet());
    }

    /**
     * Returns an immutable set of miraculous data.
     *
     * @return An immutable set of miraculous data
     */
    public Set<MiraculousData> values() {
        return ImmutableSet.copyOf(map.values());
    }

    /**
     * Executes the provided consumer for each miraculous data entry.
     *
     * @param consumer The consumer to execute for each miraculous data entry
     */
    public void forEach(BiConsumer<Holder<Miraculous>, MiraculousData> consumer) {
        map.forEach(consumer);
    }

    /**
     * Collects all miraculous keys that are currently transformed.
     *
     * @return An immutable list of miraculous keys that are currently transformed
     */
    public List<Holder<Miraculous>> getTransformed() {
        ImmutableList.Builder<Holder<Miraculous>> keys = new ImmutableList.Builder<>();
        for (Holder<Miraculous> key : map.keySet()) {
            if (get(key).transformed()) {
                keys.add(key);
            }
        }
        return keys.build();
    }

    /**
     * Determines if any miraculous is currently transformed.
     *
     * @return Whether any miraculous is currently transformed
     */
    public boolean isTransformed() {
        for (Holder<Miraculous> key : keySet()) {
            if (get(key).transformed()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the first miraculous that is transformed in the provided tag.
     *
     * @param tag The tag to check miraculouses in
     * @return The first miraculous that is transformed in the provided tag, or {@code null} if none are
     */
    public @Nullable Holder<Miraculous> getFirstTransformedIn(TagKey<Miraculous> tag) {
        for (Holder<Miraculous> miraculous : getTransformed()) {
            if (miraculous.is(tag))
                return miraculous;
        }
        return null;
    }

    /**
     * Determines if any miraculous in the provided tag has stored entities.
     *
     * @param tag The tag to check miraculouses in
     * @return Whether any miraculous in the provided tag has stored entities
     */
    public boolean hasStoredEntities(TagKey<Miraculous> tag) {
        Holder<Miraculous> miraculous = getFirstTransformedIn(tag);
        if (miraculous != null) {
            return !get(miraculous).storedEntities().isEmpty();
        }
        return false;
    }

    /**
     * Determines the highest power level of all currently transformed miraculouses.
     *
     * @return The highest power level of all currently transformed miraculouses
     */
    public int getMaxTransformedPowerLevel() {
        int powerLevel = 0;
        for (Holder<Miraculous> miraculous : getTransformed()) {
            powerLevel = Math.max(powerLevel, get(miraculous).powerLevel());
        }
        return powerLevel;
    }

    /**
     * Saves the miraculouses data to the provided entity and syncs it to clients.
     *
     * @param entity The entity to save the miraculouses data to
     */
    public void save(Entity entity) {
        entity.setData(MineraculousAttachmentTypes.MIRACULOUSES, this);
    }
}
