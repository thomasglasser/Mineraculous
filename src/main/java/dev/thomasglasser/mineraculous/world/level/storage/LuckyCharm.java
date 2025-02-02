package dev.thomasglasser.mineraculous.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record LuckyCharm(Optional<UUID> target, int id) {
    public static final Codec<LuckyCharm> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("target").forGetter(LuckyCharm::target),
            Codec.INT.fieldOf("id").forGetter(LuckyCharm::id)).apply(instance, LuckyCharm::new));

    public static final StreamCodec<ByteBuf, LuckyCharm> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), LuckyCharm::target,
            ByteBufCodecs.INT, LuckyCharm::id,
            LuckyCharm::new);
}
