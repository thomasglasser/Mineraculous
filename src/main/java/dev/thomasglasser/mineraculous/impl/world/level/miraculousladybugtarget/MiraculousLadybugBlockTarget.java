package dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionBlockData;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record MiraculousLadybugBlockTarget(BlockPos blockPos, UUID cause) implements MiraculousLadybugTarget<MiraculousLadybugBlockTarget> {
    public static final MapCodec<MiraculousLadybugBlockTarget> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockPos.CODEC.fieldOf("block_position").forGetter(MiraculousLadybugBlockTarget::blockPos),
            UUIDUtil.CODEC.fieldOf("cause").forGetter(MiraculousLadybugBlockTarget::cause)).apply(instance, MiraculousLadybugBlockTarget::new));
    public static final Codec<MiraculousLadybugBlockTarget> CODEC = MAP_CODEC.codec();
    public static final StreamCodec<ByteBuf, MiraculousLadybugBlockTarget> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, MiraculousLadybugBlockTarget::blockPos,
            UUIDUtil.STREAM_CODEC, MiraculousLadybugBlockTarget::cause,
            MiraculousLadybugBlockTarget::new);

    @Override
    public MiraculousLadybugTargetType<MiraculousLadybugBlockTarget> type() {
        return MiraculousLadybugTargetTypes.BLOCK.get();
    }

    @Override
    public @Nullable MiraculousLadybugTarget<MiraculousLadybugBlockTarget> revert(ServerLevel level, boolean instant) {
        AbilityReversionBlockData.get(level).revert(cause, level, blockPos);
        spawnParticles(level);
        return null;
    }

    private void spawnParticles(ServerLevel level) {
        MineraculousMathUtils.spawnBlockParticles(level, blockPos, MineraculousParticleTypes.REVERTING_LADYBUG.get(), 22); // hey! idk about you, but i'm feeling 22!
    }

    @Override
    public Vec3 position() {
        return blockPos.getCenter();
    }

    @Override
    public List<Vec3> getControlPoints() {
        return ReferenceArrayList.of(position());
    }

    @Override
    public boolean shouldExpandMiraculousLadybug() {
        return true;
    }
}
