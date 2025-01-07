package dev.thomasglasser.mineraculous.world.level.storage;

import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

public record FlattenedSuitLookData(ResourceKey<Miraculous> miraculous, String look, String model, byte[] pixels, List<byte[]> frames) {
    public static final StreamCodec<RegistryFriendlyByteBuf, FlattenedSuitLookData> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), FlattenedSuitLookData::miraculous,
            ByteBufCodecs.STRING_UTF8, FlattenedSuitLookData::look,
            ByteBufCodecs.STRING_UTF8, FlattenedSuitLookData::model,
            ByteBufCodecs.BYTE_ARRAY, FlattenedSuitLookData::pixels,
            ByteBufCodecs.BYTE_ARRAY.apply(ByteBufCodecs.list()), FlattenedSuitLookData::frames,
            FlattenedSuitLookData::new);
}
