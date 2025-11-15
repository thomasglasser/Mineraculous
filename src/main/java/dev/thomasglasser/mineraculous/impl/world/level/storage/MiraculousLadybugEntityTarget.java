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

public record MiraculousLadybugEntityTarget(Vec3 position, UUID cause, double width, double height) implements MiraculousLadybugTarget {

    public static final Codec<MiraculousLadybugEntityTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Vec3.CODEC.fieldOf("position").forGetter(MiraculousLadybugEntityTarget::position),
            UUIDUtil.CODEC.fieldOf("cause").forGetter(MiraculousLadybugEntityTarget::cause),
            Codec.DOUBLE.fieldOf("width").forGetter(MiraculousLadybugEntityTarget::width),
            Codec.DOUBLE.fieldOf("height").forGetter(MiraculousLadybugEntityTarget::height)).apply(instance, MiraculousLadybugEntityTarget::new));

    public static final StreamCodec<ByteBuf, MiraculousLadybugEntityTarget> STREAM_CODEC = StreamCodec.composite(
            TommyLibExtraStreamCodecs.VEC_3, MiraculousLadybugEntityTarget::position,
            UUIDUtil.STREAM_CODEC, MiraculousLadybugEntityTarget::cause,
            ByteBufCodecs.DOUBLE, MiraculousLadybugEntityTarget::width,
            ByteBufCodecs.DOUBLE, MiraculousLadybugEntityTarget::height,
            MiraculousLadybugEntityTarget::new);
    @Override
    public Vec3 getPosition() {
        return position;
    }

    @Override
    public MiraculousLadybugTargetType type() {
        return MiraculousLadybugTargetType.ENTITY;
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
    public MiraculousLadybugEntityTarget startReversion(ServerLevel level) {
        return instantRevert(level);
    }

    @Override
    public MiraculousLadybugEntityTarget instantRevert(ServerLevel level) {
        AbilityReversionEntityData.get(level).revert(cause, level, position);
        return null;
    }

    @Override
    public MiraculousLadybugTarget tick(ServerLevel level) {
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
