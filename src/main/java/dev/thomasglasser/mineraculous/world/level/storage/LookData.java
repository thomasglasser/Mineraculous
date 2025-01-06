package dev.thomasglasser.mineraculous.world.level.storage;

import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

public record LookData(ResourceKey<Miraculous> miraculous, String look, String model, byte[] pixels) {
    public static final StreamCodec<RegistryFriendlyByteBuf, LookData> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), LookData::miraculous,
            ByteBufCodecs.STRING_UTF8, LookData::look,
            ByteBufCodecs.STRING_UTF8, LookData::model,
            ByteBufCodecs.BYTE_ARRAY, LookData::pixels,
            LookData::new);
}
