package dev.thomasglasser.mineraculous.api.world.miraculous;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.tommylib.api.network.ClientboundSyncDataAttachmentPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
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
    public static final Codec<MiraculousesData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MAP_CODEC.fieldOf("map").forGetter(set -> set.map)).apply(instance, MiraculousesData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousesData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(
                    Maps::newHashMapWithExpectedSize,
                    Miraculous.STREAM_CODEC,
                    MiraculousData.STREAM_CODEC),
            set -> set.map,
            MiraculousesData::new);

    private final Map<Holder<Miraculous>, MiraculousData> map;

    public MiraculousesData() {
        this.map = new Reference2ObjectOpenHashMap<>();
    }

    public MiraculousesData(Map<Holder<Miraculous>, MiraculousData> map) {
        this.map = new Reference2ObjectOpenHashMap<>(map);
    }

    @ApiStatus.Internal
    public void tick(LivingEntity entity, ServerLevel level) {
        forEach((miraculous, data) -> data.tick(entity, level, miraculous));
    }

    public MiraculousData get(Holder<Miraculous> key) {
        return map.getOrDefault(key, new MiraculousData());
    }

    public MiraculousData put(Entity entity, Holder<Miraculous> key, MiraculousData value, boolean syncToClient) {
        MiraculousData data = map.put(key, value);
        save(entity, syncToClient);
        return data;
    }

    public Set<Holder<Miraculous>> keySet() {
        return Set.copyOf(map.keySet());
    }

    public List<MiraculousData> values() {
        return List.copyOf(map.values());
    }

    public void forEach(BiConsumer<Holder<Miraculous>, MiraculousData> consumer) {
        map.forEach(consumer);
    }

    public List<Holder<Miraculous>> getTransformed() {
        List<Holder<Miraculous>> keys = new ReferenceArrayList<>();
        for (Holder<Miraculous> key : map.keySet()) {
            if (get(key).transformed()) {
                keys.add(key);
            }
        }
        return keys;
    }

    public boolean isTransformed() {
        for (Holder<Miraculous> key : keySet()) {
            if (get(key).transformed()) {
                return true;
            }
        }
        return false;
    }

    public @Nullable Holder<Miraculous> getFirstTransformedIn(TagKey<Miraculous> tag) {
        for (Holder<Miraculous> miraculous : getTransformed()) {
            if (miraculous.is(tag))
                return miraculous;
        }
        return null;
    }

    public boolean hasStoredEntities(TagKey<Miraculous> tag) {
        Holder<Miraculous> miraculous = getFirstTransformedIn(tag);
        if (miraculous != null) {
            return !get(miraculous).storedEntities().isEmpty();
        }
        return false;
    }

    public int getPowerLevel() {
        int powerLevel = 0;
        for (Holder<Miraculous> miraculous : keySet()) {
            powerLevel = Math.max(powerLevel, get(miraculous).powerLevel());
        }
        return powerLevel;
    }

    public void save(Entity entity, boolean syncToClient) {
        entity.setData(MineraculousAttachmentTypes.MIRACULOUSES, this);
        if (syncToClient)
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncDataAttachmentPayload<>(entity.getId(), MineraculousAttachmentTypes.MIRACULOUSES, this), entity.getServer());
    }
}
