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

public record MiraculousLadybugEntityTarget(Vec3 position, UUID cause, float width, float height) implements MiraculousLadybugTarget {

    public static final Codec<MiraculousLadybugEntityTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Vec3.CODEC.fieldOf("position").forGetter(MiraculousLadybugEntityTarget::position),
            UUIDUtil.CODEC.fieldOf("cause").forGetter(MiraculousLadybugEntityTarget::cause),
            Codec.FLOAT.fieldOf("width").forGetter(MiraculousLadybugEntityTarget::width),
            Codec.FLOAT.fieldOf("height").forGetter(MiraculousLadybugEntityTarget::height)).apply(instance, MiraculousLadybugEntityTarget::new));
    public static final StreamCodec<ByteBuf, MiraculousLadybugEntityTarget> STREAM_CODEC = StreamCodec.composite(
            TommyLibExtraStreamCodecs.VEC_3, MiraculousLadybugEntityTarget::position,
            UUIDUtil.STREAM_CODEC, MiraculousLadybugEntityTarget::cause,
            ByteBufCodecs.FLOAT, MiraculousLadybugEntityTarget::width,
            ByteBufCodecs.FLOAT, MiraculousLadybugEntityTarget::height,
            MiraculousLadybugEntityTarget::new);
    @Override
    public MiraculousLadybugTargetType type() {
        return MiraculousLadybugTargetType.ENTITY;
    }

    @Override
    public MiraculousLadybugEntityTarget revert(ServerLevel level) {
        revertInstantly(level);
        return this;
    }

    @Override
    public void revertInstantly(ServerLevel level) {
        AbilityReversionEntityData.get(level).revert(cause, level, position);
    }

    @Override
    public boolean shouldStartRevert() {
        return true;
    }

    public List<Vec3> getSpiralPoints() {
        return MineraculousMathUtils.spinAround(
                position(),
                width,
                width,
                height,
                Math.PI / 2d,
                height / 16d);
    }

    public void spawnParticles(ServerLevel level) {
        for (Vec3 pos : this.getSpiralPoints()) {
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
