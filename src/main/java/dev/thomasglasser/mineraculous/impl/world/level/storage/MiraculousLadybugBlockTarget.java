package dev.thomasglasser.mineraculous.impl.world.level.storage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionBlockData;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record MiraculousLadybugBlockTarget(Vec3 position, Map<BlockPos, UUID> blocksToRevert, int revertingTicks, List<List<BlockPos>> revertLayers, int currentLayerIndex) implements MiraculousLadybugTarget {

    public static final Codec<MiraculousLadybugBlockTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Vec3.CODEC.fieldOf("block_position").forGetter(MiraculousLadybugBlockTarget::position),
            Codec.unboundedMap(Codec.STRING.xmap(
                    s -> {
                        String[] parts = s.split(" ");
                        return new BlockPos(
                                Integer.parseInt(parts[0]),
                                Integer.parseInt(parts[1]),
                                Integer.parseInt(parts[2]));
                    },
                    BlockPos::toShortString), UUIDUtil.CODEC).fieldOf("blocks").forGetter(MiraculousLadybugBlockTarget::blocksToRevert),
            Codec.INT.fieldOf("revertingTicks").forGetter(MiraculousLadybugBlockTarget::revertingTicks),
            BlockPos.CODEC.listOf().listOf().fieldOf("revertLayers").forGetter(MiraculousLadybugBlockTarget::revertLayers),
            Codec.INT.fieldOf("currentLayerIndex").forGetter(MiraculousLadybugBlockTarget::currentLayerIndex))
            .apply(instance, MiraculousLadybugBlockTarget::new));
    public static final StreamCodec<ByteBuf, MiraculousLadybugBlockTarget> STREAM_CODEC = StreamCodec.composite(
            TommyLibExtraStreamCodecs.VEC_3, MiraculousLadybugBlockTarget::position,
            ByteBufCodecs.map(
                    Maps::newHashMapWithExpectedSize,
                    BlockPos.STREAM_CODEC,
                    UUIDUtil.STREAM_CODEC),
            MiraculousLadybugBlockTarget::blocksToRevert,
            ByteBufCodecs.INT, MiraculousLadybugBlockTarget::revertingTicks,
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs.list()), MiraculousLadybugBlockTarget::revertLayers,
            ByteBufCodecs.INT, MiraculousLadybugBlockTarget::currentLayerIndex,
            MiraculousLadybugBlockTarget::new);
    public MiraculousLadybugBlockTarget(BlockPos pos, UUID cause) {
        this(pos.getCenter(), Map.of(pos, cause), -1, ImmutableList.of(), -1);
    }

    public MiraculousLadybugBlockTarget(Vec3 center, Map<BlockPos, UUID> clump) {
        this(center, clump, -1, ImmutableList.of(), -1);
    }

    public static MiraculousLadybugBlockTarget wrap(BlockPos blockPosition) {
        return new MiraculousLadybugBlockTarget(blockPosition, Util.NIL_UUID);
    }

    @Override
    public Vec3 position() {
        return position;
    }

    @Override
    public MiraculousLadybugTargetType type() {
        return MiraculousLadybugTargetType.BLOCK;
    }

    @Override
    public MiraculousLadybugBlockTarget revert(ServerLevel level) {
        if (blocksToRevert.isEmpty()) return this;
        BlockPos origin = MineraculousMathUtils.findNearestBlockPos(position(), blocksToRevert.keySet());
        if (origin == null) return this;

        List<List<BlockPos>> layers = MineraculousMathUtils.buildRevertLayers(origin, blocksToRevert.keySet());
        Map<BlockPos, UUID> remaining = new HashMap<>(blocksToRevert);

        // revert first layer immediately
        if (!layers.isEmpty()) {
            for (BlockPos bp : layers.get(0)) {
                UUID cause = remaining.remove(bp);
                AbilityReversionBlockData.get(level).revert(cause != null ? cause : Util.NIL_UUID, level, bp);
                MineraculousMathUtils.spawnBlockParticles(level, bp, MineraculousParticleTypes.SUMMONING_LADYBUG.get(), 20);
            }
        }

        return new MiraculousLadybugBlockTarget(position, remaining, 0, layers, 0);
    }

    public MiraculousLadybugBlockTarget tick(ServerLevel level) {
        if (revertingTicks >= 0 && !revertLayers.isEmpty()) {
            int newTicks = revertingTicks + 1;
            if (newTicks % 10 == 0) return revertLayer(level, currentLayerIndex + 1);
            return new MiraculousLadybugBlockTarget(position, blocksToRevert, newTicks, revertLayers, currentLayerIndex);
        } else return this; //return new BlockTarget(position, blocksToRevert, -1, revertLayers, -1);
    }

    private MiraculousLadybugBlockTarget revertLayer(ServerLevel level, int layerIndex) {
        if (layerIndex >= revertLayers.size()) return this;
        List<BlockPos> layer = revertLayers.get(layerIndex);
        Map<BlockPos, UUID> remaining = new HashMap<>(blocksToRevert);

        for (BlockPos bp : layer) {
            UUID cause = remaining.remove(bp);
            AbilityReversionBlockData.get(level).revert(cause != null ? cause : Util.NIL_UUID, level, bp);
            MineraculousMathUtils.spawnBlockParticles(level, bp, MineraculousParticleTypes.SUMMONING_LADYBUG.get(), 20);
        }

        return new MiraculousLadybugBlockTarget(position, remaining, revertingTicks, revertLayers, layerIndex);
    }
}
