package dev.thomasglasser.mineraculous.world.level.storage;

import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.tommylib.api.network.NetworkUtils;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

public record FlattenedSuitLookData(ResourceKey<Miraculous> miraculous, String look, Optional<String> model, byte[] pixels, Optional<byte[]> glowmaskPixels, List<byte[]> frames, List<byte[]> glowmaskFrames) {
    public static final StreamCodec<ByteBuf, FlattenedSuitLookData> CODEC = NetworkUtils.composite(
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), FlattenedSuitLookData::miraculous,
            ByteBufCodecs.STRING_UTF8, FlattenedSuitLookData::look,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), FlattenedSuitLookData::model,
            ByteBufCodecs.BYTE_ARRAY, FlattenedSuitLookData::pixels,
            ByteBufCodecs.optional(ByteBufCodecs.BYTE_ARRAY), FlattenedSuitLookData::glowmaskPixels,
            ByteBufCodecs.BYTE_ARRAY.apply(ByteBufCodecs.list()), FlattenedSuitLookData::frames,
            ByteBufCodecs.BYTE_ARRAY.apply(ByteBufCodecs.list()), FlattenedSuitLookData::glowmaskFrames,
            FlattenedSuitLookData::new);
}
