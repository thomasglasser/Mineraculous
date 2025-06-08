package dev.thomasglasser.mineraculous.world.level.storage;

import com.mojang.datafixers.util.Either;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record AbilityData(int powerLevel, Either<Holder<Miraculous>, Holder<Kamikotization>> power, boolean powerActive) {
    public static final StreamCodec<RegistryFriendlyByteBuf, AbilityData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, AbilityData::powerLevel,
            ByteBufCodecs.either(Miraculous.STREAM_CODEC, Kamikotization.STREAM_CODEC), AbilityData::power,
            ByteBufCodecs.BOOL, AbilityData::powerActive,
            AbilityData::new);
}
