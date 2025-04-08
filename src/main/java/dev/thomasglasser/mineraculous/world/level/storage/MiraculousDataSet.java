package dev.thomasglasser.mineraculous.world.level.storage;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import dev.thomasglasser.mineraculous.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.network.ClientboundSyncMiraculousDataSetPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class MiraculousDataSet {
    public static final UnboundedMapCodec<ResourceKey<Miraculous>, MiraculousData> MAP_CODEC = Codec.unboundedMap(ResourceKey.codec(MineraculousRegistries.MIRACULOUS), MiraculousData.CODEC);
    public static final Codec<MiraculousDataSet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MAP_CODEC.fieldOf("map").forGetter(set -> set.map)).apply(instance, MiraculousDataSet::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousDataSet> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(
                    Maps::newHashMapWithExpectedSize,
                    ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS),
                    MiraculousData.STREAM_CODEC),
            set -> set.map,
            MiraculousDataSet::new);

    private final Map<ResourceKey<Miraculous>, MiraculousData> map;

    public MiraculousDataSet() {
        this.map = new HashMap<>();
    }

    public MiraculousDataSet(Map<ResourceKey<Miraculous>, MiraculousData> map) {
        this.map = new HashMap<>(map);
    }

    public MiraculousData get(ResourceKey<Miraculous> key) {
        return map.getOrDefault(key, new MiraculousData());
    }

    public MiraculousData put(Entity entity, ResourceKey<Miraculous> key, MiraculousData value, boolean syncToClient) {
        MiraculousData data = map.put(key, value);
        if (value.transformed() && entity instanceof ServerPlayer player)
            MineraculousCriteriaTriggers.TRANSFORMED_MIRACULOUS.get().trigger(player, key);
        save(entity, syncToClient);
        return data;
    }

    public Set<ResourceKey<Miraculous>> keySet() {
        return Set.copyOf(map.keySet());
    }

    public List<MiraculousData> values() {
        return List.copyOf(map.values());
    }

    public List<ResourceKey<Miraculous>> getTransformed() {
        return map.entrySet().stream().filter(entry -> entry.getValue().transformed()).map(Map.Entry::getKey).toList();
    }

    public List<Holder<Miraculous>> getTransformedHolders(HolderLookup.Provider lookup) {
        return getTransformed().stream().map(lookup::holderOrThrow).toList();
    }

    public List<Miraculous> getTransformedDirect(HolderLookup.Provider lookup) {
        return getTransformedHolders(lookup).stream().map(Holder::value).toList();
    }

    public boolean isTransformed() {
        return map.values().stream().anyMatch(MiraculousData::transformed);
    }

    public @Nullable ResourceKey<Miraculous> getFirstKeyIn(TagKey<Miraculous> tag, Level level) {
        return keySet().stream().filter(key -> level.holderOrThrow(key).is(tag)).findFirst().orElse(null);
    }

    public void save(Entity entity, boolean syncToClient) {
        entity.setData(MineraculousAttachmentTypes.MIRACULOUS.get(), this);
        if (syncToClient) TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncMiraculousDataSetPayload(this, entity.getId()), entity.level().getServer());
    }
}
