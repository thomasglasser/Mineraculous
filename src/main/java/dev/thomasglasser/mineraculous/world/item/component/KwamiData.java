package dev.thomasglasser.mineraculous.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record KwamiData(UUID uuid, int id, boolean charged) {
    public static final Codec<KwamiData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("uuid").forGetter(KwamiData::uuid),
            Codec.INT.fieldOf("id").forGetter(KwamiData::id),
            Codec.BOOL.fieldOf("charged").forGetter(KwamiData::charged)).apply(instance, KwamiData::new));
    public static final StreamCodec<ByteBuf, KwamiData> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, KwamiData::uuid,
            ByteBufCodecs.INT, KwamiData::id,
            ByteBufCodecs.BOOL, KwamiData::charged,
            KwamiData::new);
}
