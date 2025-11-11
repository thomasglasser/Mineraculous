package dev.thomasglasser.mineraculous.impl.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum NewMLBTargetType {
    BLOCK(NewMLBBlockTarget.CODEC.fieldOf("block_target"), NewMLBBlockTarget.STREAM_CODEC),
    BLOCK_CLUSTER(NewMLBBlockClusterTarget.CODEC.fieldOf("block_cluster_target"), NewMLBBlockClusterTarget.STREAM_CODEC),
    ENTITY(NewMLBEntityTarget.CODEC.fieldOf("entity_target"), NewMLBEntityTarget.STREAM_CODEC);

    public static final Codec<NewMLBTargetType> CODEC = Codec.STRING.xmap(NewMLBTargetType::valueOf, NewMLBTargetType::name);
    public static final Codec<NewMLBTarget> TARGET_CODEC = CODEC.dispatch(NewMLBTarget::type, NewMLBTargetType::codec);

    public static final StreamCodec<ByteBuf, NewMLBTargetType> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(NewMLBTargetType::of, NewMLBTargetType::getSerializedName);
    public static final StreamCodec<ByteBuf, NewMLBTarget> TARGET_STREAM_CODEC = STREAM_CODEC.dispatch(NewMLBTarget::type, NewMLBTargetType::streamCodec);

    private final MapCodec<? extends NewMLBTarget> codec;
    private final StreamCodec<ByteBuf, ? extends NewMLBTarget> streamCodec;

    NewMLBTargetType(MapCodec<? extends NewMLBTarget> codec, StreamCodec<ByteBuf, ? extends NewMLBTarget> streamCodec) {
        this.codec = codec;
        this.streamCodec = streamCodec;
    }

    MapCodec<? extends NewMLBTarget> codec() {
        return codec;
    }

    public StreamCodec<ByteBuf, ? extends NewMLBTarget> streamCodec() {
        return streamCodec;
    }

    public String getSerializedName() {
        return name().toLowerCase();
    }

    public static NewMLBTargetType of(String name) {
        return valueOf(name.toUpperCase());
    }
}
