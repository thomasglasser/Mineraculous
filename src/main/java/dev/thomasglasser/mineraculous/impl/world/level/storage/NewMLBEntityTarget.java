package dev.thomasglasser.mineraculous.impl.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionEntityData;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public record NewMLBEntityTarget(Vec3 position, UUID cause, double width, double height) implements NewMLBTarget {

    public static final Codec<NewMLBEntityTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Vec3.CODEC.fieldOf("position").forGetter(NewMLBEntityTarget::position),
            UUIDUtil.CODEC.fieldOf("cause").forGetter(NewMLBEntityTarget::cause),
            Codec.DOUBLE.fieldOf("width").forGetter(NewMLBEntityTarget::width),
            Codec.DOUBLE.fieldOf("height").forGetter(NewMLBEntityTarget::height)).apply(instance, NewMLBEntityTarget::new));

    public static final StreamCodec<ByteBuf, NewMLBEntityTarget> STREAM_CODEC = StreamCodec.composite(
            TommyLibExtraStreamCodecs.VEC_3, NewMLBEntityTarget::position,
            UUIDUtil.STREAM_CODEC, NewMLBEntityTarget::cause,
            ByteBufCodecs.DOUBLE, NewMLBEntityTarget::width,
            ByteBufCodecs.DOUBLE, NewMLBEntityTarget::height,
            NewMLBEntityTarget::new);
    @Override
    public Vec3 getPosition() {
        return position;
    }

    @Override
    public NewMLBTargetType type() {
        return NewMLBTargetType.ENTITY;
    }

    @Override
    public boolean isReverting() {
        return false;
    }

    @Override
    public List<Vec3> getControlPoints() {
        return MineraculousMathUtils.spinAround(
                position(),
                width,
                width,
                height,
                Math.PI / 2d,
                height / 16d);
    }

    @Override
    public NewMLBEntityTarget startReversion(ServerLevel level) {
        return instantRevert(level);
    }

    @Override
    public NewMLBEntityTarget instantRevert(ServerLevel level) {
        AbilityReversionEntityData.get(level).revert(cause, level, position);
        return null;
    }

    @Override
    public NewMLBTarget tick(ServerLevel level) {
        return this;
    }

    @Override
    public void spawnParticles(ServerLevel level) {
        for (Vec3 pos : getControlPoints()) {
            double x = pos.x;
            double y = pos.y;
            double z = pos.z;
            level.sendParticles(
                    MineraculousParticleTypes.REVERTING_LADYBUG.get(),
                    x,
                    y,
                    z,
                    5, 0, 0, 0, 0.1);
        }
    }
}
