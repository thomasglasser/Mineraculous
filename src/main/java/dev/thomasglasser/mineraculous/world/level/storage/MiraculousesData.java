package dev.thomasglasser.mineraculous.world.level.storage;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import dev.thomasglasser.mineraculous.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.tommylib.api.network.ClientboundSyncDataAttachmentPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
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

public class MiraculousesData {
    public static final UnboundedMapCodec<ResourceKey<Miraculous>, MiraculousData> MAP_CODEC = Codec.unboundedMap(ResourceKey.codec(MineraculousRegistries.MIRACULOUS), MiraculousData.CODEC);
    public static final Codec<MiraculousesData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MAP_CODEC.fieldOf("map").forGetter(set -> set.map)).apply(instance, MiraculousesData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousesData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(
                    Maps::newHashMapWithExpectedSize,
                    ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS),
                    MiraculousData.STREAM_CODEC),
            set -> set.map,
            MiraculousesData::new);

    private final Map<ResourceKey<Miraculous>, MiraculousData> map;

    public MiraculousesData() {
        this.map = new Reference2ObjectOpenHashMap<>();
    }

    public MiraculousesData(Map<ResourceKey<Miraculous>, MiraculousData> map) {
        this.map = new Reference2ObjectOpenHashMap<>(map);
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

    public void forEach(BiConsumer<ResourceKey<Miraculous>, MiraculousData> consumer) {
        map.forEach(consumer);
    }

    public List<ResourceKey<Miraculous>> getTransformed() {
        List<ResourceKey<Miraculous>> keys = new ReferenceArrayList<>();
        for (ResourceKey<Miraculous> key : map.keySet()) {
            if (get(key).transformed()) {
                keys.add(key);
            }
        }
        return keys;
    }

    public List<Holder<Miraculous>> getTransformedHolders(HolderLookup.Provider lookup) {
        List<Holder<Miraculous>> holders = new ReferenceArrayList<>();
        for (ResourceKey<Miraculous> key : getTransformed()) {
            holders.add(lookup.holderOrThrow(key));
        }
        return holders;
    }

    public List<Miraculous> getTransformedDirect(HolderLookup.Provider lookup) {
        List<Miraculous> miraculouses = new ReferenceArrayList<>();
        for (Holder<Miraculous> holder : getTransformedHolders(lookup)) {
            miraculouses.add(holder.value());
        }
        return miraculouses;
    }

    public boolean isTransformed() {
        for (ResourceKey<Miraculous> key : keySet()) {
            if (get(key).transformed()) {
                return true;
            }
        }
        return false;
    }

    public @Nullable ResourceKey<Miraculous> getFirstTransformedKeyIn(TagKey<Miraculous> tag, HolderLookup.Provider lookup) {
        for (Holder<Miraculous> holder : getTransformedHolders(lookup)) {
            if (holder.is(tag))
                return holder.getKey();
        }
        return null;
    }

    public void save(Entity entity, boolean syncToClient) {
        entity.setData(MineraculousAttachmentTypes.MIRACULOUSES, this);
        if (syncToClient)
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncDataAttachmentPayload<>(entity.getId(), MineraculousAttachmentTypes.MIRACULOUSES, this), entity.getServer());
    }
}
