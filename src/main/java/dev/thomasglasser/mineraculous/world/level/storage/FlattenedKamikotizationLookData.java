package dev.thomasglasser.mineraculous.world.level.storage;

import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

public record FlattenedKamikotizationLookData(ResourceKey<Kamikotization> kamikotization, Optional<String> model, byte[] pixels) {
    public static final StreamCodec<RegistryFriendlyByteBuf, FlattenedKamikotizationLookData> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.KAMIKOTIZATION), FlattenedKamikotizationLookData::kamikotization,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), FlattenedKamikotizationLookData::model,
            ByteBufCodecs.BYTE_ARRAY, FlattenedKamikotizationLookData::pixels,
            FlattenedKamikotizationLookData::new);
}
