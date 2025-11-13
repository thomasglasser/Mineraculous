package dev.thomasglasser.mineraculous.impl.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionBlockData;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import java.util.UUID;

public record NewMLBBlockTarget(BlockPos blockPos, UUID cause) implements NewMLBTarget {
    public static final Codec<NewMLBBlockTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("block_position").forGetter(NewMLBBlockTarget::blockPos),
            UUIDUtil.CODEC.fieldOf("cause").forGetter(NewMLBBlockTarget::cause)).apply(instance, NewMLBBlockTarget::new));
    public static final StreamCodec<ByteBuf, NewMLBBlockTarget> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, NewMLBBlockTarget::blockPos,
            UUIDUtil.STREAM_CODEC, NewMLBBlockTarget::cause,
            NewMLBBlockTarget::new);

    @Override
    public Vec3 getPosition() {
        return blockPos.getCenter();
    }

    @Override
    public NewMLBTargetType type() {
        return NewMLBTargetType.BLOCK;
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
    public NewMLBTarget startReversion(ServerLevel level) {
        return instantRevert(level);
    }

    @Override
    public NewMLBTarget instantRevert(ServerLevel level) {
        AbilityReversionBlockData.get(level).revert(cause == null ? Util.NIL_UUID : cause, level, blockPos);
        spawnParticles(level);
        return this;
    }

    @Override
    public NewMLBTarget tick(ServerLevel level) {
        return this;
    }

    @Override
    public void spawnParticles(ServerLevel level) {
        MineraculousMathUtils.spawnBlockParticles(level, blockPos, MineraculousParticleTypes.REVERTING_LADYBUG.get(), 22); // hey! idk about you, but i am feeling 22!
    }
}
