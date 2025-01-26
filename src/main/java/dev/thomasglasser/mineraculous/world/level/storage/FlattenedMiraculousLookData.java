package dev.thomasglasser.mineraculous.world.level.storage;

import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

public record FlattenedMiraculousLookData(ResourceKey<Miraculous> miraculous, String look, Optional<String> model, byte[] pixels, Optional<byte[]> glowmaskPixels, Optional<String> transforms) {
    public static final StreamCodec<RegistryFriendlyByteBuf, FlattenedMiraculousLookData> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), FlattenedMiraculousLookData::miraculous,
            ByteBufCodecs.STRING_UTF8, FlattenedMiraculousLookData::look,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), FlattenedMiraculousLookData::model,
            ByteBufCodecs.BYTE_ARRAY, FlattenedMiraculousLookData::pixels,
            ByteBufCodecs.optional(ByteBufCodecs.BYTE_ARRAY), FlattenedMiraculousLookData::glowmaskPixels,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), FlattenedMiraculousLookData::transforms,
            FlattenedMiraculousLookData::new);
}
