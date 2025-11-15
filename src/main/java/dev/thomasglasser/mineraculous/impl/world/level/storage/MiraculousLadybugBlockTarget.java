package dev.thomasglasser.mineraculous.impl.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionBlockData;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public record MiraculousLadybugBlockTarget(BlockPos blockPos, UUID cause) implements MiraculousLadybugTarget {
    public static final Codec<MiraculousLadybugBlockTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("block_position").forGetter(MiraculousLadybugBlockTarget::blockPos),
            UUIDUtil.CODEC.fieldOf("cause").forGetter(MiraculousLadybugBlockTarget::cause)).apply(instance, MiraculousLadybugBlockTarget::new));
    public static final StreamCodec<ByteBuf, MiraculousLadybugBlockTarget> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, MiraculousLadybugBlockTarget::blockPos,
            UUIDUtil.STREAM_CODEC, MiraculousLadybugBlockTarget::cause,
            MiraculousLadybugBlockTarget::new);

    @Override
    public Vec3 getPosition() {
        return blockPos.getCenter();
    }

    @Override
    public MiraculousLadybugTargetType type() {
        return MiraculousLadybugTargetType.BLOCK;
    }

    @Override
    public boolean isReverting() {
        return false;
    }

    @Override
    public List<Vec3> getControlPoints() {
        return List.of(getPosition());
    }

    @Override
    public MiraculousLadybugTarget startReversion(ServerLevel level) {
        return instantRevert(level);
    }

    @Override
    public MiraculousLadybugTarget instantRevert(ServerLevel level) {
        AbilityReversionBlockData.get(level).revert(cause == null ? Util.NIL_UUID : cause, level, blockPos);
        spawnParticles(level);
        return null;
    }

    @Override
    public MiraculousLadybugTarget tick(ServerLevel level) {
        return this;
    }

    @Override
    public void spawnParticles(ServerLevel level) {
        MineraculousMathUtils.spawnBlockParticles(level, blockPos, MineraculousParticleTypes.REVERTING_LADYBUG.get(), 22); // hey! idk about you, but i am feeling 22!
    }
}
