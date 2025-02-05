package dev.thomasglasser.mineraculous.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record KamikoData(UUID uuid, UUID owner, int nameColor) {
    public static final Codec<KamikoData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("uuid").forGetter(KamikoData::uuid),
            Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("owner").forGetter(KamikoData::owner),
            Codec.INT.fieldOf("name_color").forGetter(KamikoData::nameColor)).apply(instance, KamikoData::new));
    public static final StreamCodec<ByteBuf, KamikoData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), KamikoData::uuid,
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), KamikoData::owner,
            ByteBufCodecs.INT, KamikoData::nameColor,
            KamikoData::new);
}
