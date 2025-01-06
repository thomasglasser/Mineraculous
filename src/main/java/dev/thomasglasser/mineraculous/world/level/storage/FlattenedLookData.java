package dev.thomasglasser.mineraculous.world.level.storage;

import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

public record FlattenedLookData(ResourceKey<Miraculous> miraculous, String look, String model, byte[] pixels, List<byte[]> frames) {
    public static final StreamCodec<RegistryFriendlyByteBuf, FlattenedLookData> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), FlattenedLookData::miraculous,
            ByteBufCodecs.STRING_UTF8, FlattenedLookData::look,
            ByteBufCodecs.STRING_UTF8, FlattenedLookData::model,
            ByteBufCodecs.BYTE_ARRAY, FlattenedLookData::pixels,
            ByteBufCodecs.BYTE_ARRAY.apply(ByteBufCodecs.list()), FlattenedLookData::frames,
            FlattenedLookData::new);
}
