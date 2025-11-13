package dev.thomasglasser.mineraculous.impl.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public record NewMLBBlockClusterTarget(List<List<NewMLBBlockTarget>> blockLayers, Vec3 center, double width, double height, int tick, int currentLayer) implements NewMLBTarget {

    public static final Codec<NewMLBBlockClusterTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NewMLBBlockTarget.CODEC.listOf().listOf().fieldOf("block_layers").forGetter(NewMLBBlockClusterTarget::blockLayers),
            Vec3.CODEC.fieldOf("center_position").forGetter(NewMLBBlockClusterTarget::center),
            Codec.DOUBLE.fieldOf("width").forGetter(NewMLBBlockClusterTarget::width),
            Codec.DOUBLE.fieldOf("height").forGetter(NewMLBBlockClusterTarget::height),
            Codec.INT.fieldOf("tick").forGetter(NewMLBBlockClusterTarget::tick),
            Codec.INT.fieldOf("current_layer").forGetter(NewMLBBlockClusterTarget::currentLayer)).apply(instance, NewMLBBlockClusterTarget::new));
    public static final StreamCodec<ByteBuf, NewMLBBlockClusterTarget> STREAM_CODEC = StreamCodec.composite(
            NewMLBBlockTarget.STREAM_CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs.list()), NewMLBBlockClusterTarget::blockLayers,
            TommyLibExtraStreamCodecs.VEC_3, NewMLBBlockClusterTarget::center,
            ByteBufCodecs.DOUBLE, NewMLBBlockClusterTarget::width,
            ByteBufCodecs.DOUBLE, NewMLBBlockClusterTarget::height,
            ByteBufCodecs.INT, NewMLBBlockClusterTarget::tick,
            ByteBufCodecs.INT, NewMLBBlockClusterTarget::currentLayer,
            NewMLBBlockClusterTarget::new);
    @Override
    public Vec3 getPosition() {
        return this.center;
    }

    @Override
    public NewMLBTargetType type() {
        return NewMLBTargetType.BLOCK_CLUSTER;
    }

    @Override
    public boolean isReverting() {
        return tick != -1;
    }

    @Override
    public List<Vec3> getControlPoints() {
        Vec3 pos = center.add(0, -height / 2, 0);
        return MineraculousMathUtils.spinAround(
                pos,
                width,
                width,
                height,
                Math.PI / 2d,
                height / 16d);
    }

    //TODO implement the following:
    @Override
    public NewMLBTarget startReversion(ServerLevel level) {
        return null;
    }

    @Override
    public NewMLBTarget instantRevert(ServerLevel level) {
        for (List<NewMLBBlockTarget> layer : blockLayers)
            for (NewMLBBlockTarget blockTarget : layer)
                blockTarget.instantRevert(level);
        return null;
    }

    @Override
    public NewMLBTarget tick(ServerLevel level) {
        return this;
    }

    @Override
    public void spawnParticles(ServerLevel level) { // No actual usage ever.
        for (List<NewMLBBlockTarget> layer : blockLayers)
            for (NewMLBBlockTarget blockTarget : layer)
                blockTarget.spawnParticles(level);
    }
}
