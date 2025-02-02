package dev.thomasglasser.mineraculous.world.level.storage;

import com.mojang.datafixers.util.Either;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

public record AbilityData(int powerLevel, Either<ResourceKey<Miraculous>, ResourceKey<Kamikotization>> power) {
    public static final StreamCodec<RegistryFriendlyByteBuf, AbilityData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, AbilityData::powerLevel,
            ByteBufCodecs.either(ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), ResourceKey.streamCodec(MineraculousRegistries.KAMIKOTIZATION)), AbilityData::power,
            AbilityData::new);
}
