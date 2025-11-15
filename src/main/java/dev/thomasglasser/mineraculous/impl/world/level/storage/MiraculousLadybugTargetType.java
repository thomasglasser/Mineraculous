package dev.thomasglasser.mineraculous.impl.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum MiraculousLadybugTargetType {
    BLOCK(MiraculousLadybugBlockTarget.CODEC.fieldOf("block_target"), MiraculousLadybugBlockTarget.STREAM_CODEC),
    BLOCK_CLUSTER(MiraculousLadybugBlockClusterTarget.CODEC.fieldOf("block_cluster_target"), MiraculousLadybugBlockClusterTarget.STREAM_CODEC),
    ENTITY(MiraculousLadybugEntityTarget.CODEC.fieldOf("entity_target"), MiraculousLadybugEntityTarget.STREAM_CODEC);

    public static final Codec<MiraculousLadybugTargetType> CODEC = Codec.STRING.xmap(MiraculousLadybugTargetType::valueOf, MiraculousLadybugTargetType::name);
    public static final Codec<MiraculousLadybugTarget> TARGET_CODEC = CODEC.dispatch(MiraculousLadybugTarget::type, MiraculousLadybugTargetType::codec);

    public static final StreamCodec<ByteBuf, MiraculousLadybugTargetType> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(MiraculousLadybugTargetType::of, MiraculousLadybugTargetType::getSerializedName);
    public static final StreamCodec<ByteBuf, MiraculousLadybugTarget> TARGET_STREAM_CODEC = STREAM_CODEC.dispatch(MiraculousLadybugTarget::type, MiraculousLadybugTargetType::streamCodec);

    private final MapCodec<? extends MiraculousLadybugTarget> codec;
    private final StreamCodec<ByteBuf, ? extends MiraculousLadybugTarget> streamCodec;

    MiraculousLadybugTargetType(MapCodec<? extends MiraculousLadybugTarget> codec, StreamCodec<ByteBuf, ? extends MiraculousLadybugTarget> streamCodec) {
        this.codec = codec;
        this.streamCodec = streamCodec;
    }

    MapCodec<? extends MiraculousLadybugTarget> codec() {
        return codec;
    }

    public StreamCodec<ByteBuf, ? extends MiraculousLadybugTarget> streamCodec() {
        return streamCodec;
    }

    public String getSerializedName() {
        return name().toLowerCase();
    }

    public static MiraculousLadybugTargetType of(String name) {
        return valueOf(name.toUpperCase());
    }
}
