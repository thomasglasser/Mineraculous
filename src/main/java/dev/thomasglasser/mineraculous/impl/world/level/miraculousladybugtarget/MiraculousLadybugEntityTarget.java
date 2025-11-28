package dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.world.level.storage.EntityReversionData;
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
import org.jetbrains.annotations.Nullable;

public record MiraculousLadybugEntityTarget(Vec3 position, UUID cause, double width, double height) implements MiraculousLadybugTarget<MiraculousLadybugEntityTarget> {

    public static final MapCodec<MiraculousLadybugEntityTarget> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Vec3.CODEC.fieldOf("position").forGetter(MiraculousLadybugEntityTarget::position),
            UUIDUtil.CODEC.fieldOf("cause").forGetter(MiraculousLadybugEntityTarget::cause),
            Codec.DOUBLE.fieldOf("width").forGetter(MiraculousLadybugEntityTarget::width),
            Codec.DOUBLE.fieldOf("height").forGetter(MiraculousLadybugEntityTarget::height)).apply(instance, MiraculousLadybugEntityTarget::new));
    public static final Codec<MiraculousLadybugEntityTarget> CODEC = MAP_CODEC.codec();
    public static final StreamCodec<ByteBuf, MiraculousLadybugEntityTarget> STREAM_CODEC = StreamCodec.composite(
            TommyLibExtraStreamCodecs.VEC_3, MiraculousLadybugEntityTarget::position,
            UUIDUtil.STREAM_CODEC, MiraculousLadybugEntityTarget::cause,
            ByteBufCodecs.DOUBLE, MiraculousLadybugEntityTarget::width,
            ByteBufCodecs.DOUBLE, MiraculousLadybugEntityTarget::height,
            MiraculousLadybugEntityTarget::new);
    @Override
    public MiraculousLadybugTargetType<MiraculousLadybugEntityTarget> type() {
        return MiraculousLadybugTargetTypes.ENTITY.get();
    }

    @Override
    public @Nullable MiraculousLadybugTarget<MiraculousLadybugEntityTarget> revert(ServerLevel level, boolean instant) {
        EntityReversionData.get(level).revertRevertibleAndConverted(cause, level, position);
        if (instant)
            spawnParticles(level);
        return null;
    }

    private void spawnParticles(ServerLevel level) {
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
}
