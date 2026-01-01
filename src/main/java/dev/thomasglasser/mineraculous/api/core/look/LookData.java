package dev.thomasglasser.mineraculous.api.core.look;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Stores a name and map of looks for contexts.
 *
 * @param name  The name to use
 * @param looks The map of looks to use
 */
public record LookData(Optional<String> name, ImmutableMap<ResourceKey<LookContext>, ResourceLocation> looks) {
    public static final LookData DEFAULT = new LookData(Optional.empty(), ImmutableMap.of());
    public static final Codec<LookData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("name").forGetter(LookData::name),
            Codec.unboundedMap(ResourceKey.codec(MineraculousRegistries.LOOK_CONTEXT), ResourceLocation.CODEC).fieldOf("looks").xmap(ImmutableMap::copyOf, Function.identity()).forGetter(LookData::looks)).apply(instance, LookData::new));
    public static final StreamCodec<ByteBuf, LookData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), LookData::name,
            ByteBufCodecs.map(
                    Maps::newHashMapWithExpectedSize,
                    ResourceKey.streamCodec(MineraculousRegistries.LOOK_CONTEXT),
                    ResourceLocation.STREAM_CODEC).map(ImmutableMap::copyOf, HashMap::new),
            LookData::looks,
            LookData::new);
}
