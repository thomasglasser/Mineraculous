package dev.thomasglasser.mineraculous.impl.world.level.storage;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.tommylib.api.network.ClientboundSyncDataAttachmentPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record MiraculousLadybugTargetData(List<BlockTarget> blockTargets, List<EntityTarget> entityTargets, List<Vec3> pathControlPoints, double splinePosition) {

    public static final Codec<MiraculousLadybugTargetData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockTarget.CODEC.listOf().fieldOf("block_targets").forGetter(MiraculousLadybugTargetData::blockTargets),
            EntityTarget.CODEC.listOf().fieldOf("entity_targets").forGetter(MiraculousLadybugTargetData::entityTargets),
            Vec3.CODEC.listOf().fieldOf("path_control_points").forGetter(MiraculousLadybugTargetData::pathControlPoints),
            Codec.DOUBLE.fieldOf("spline_position").forGetter(MiraculousLadybugTargetData::splinePosition)).apply(instance, MiraculousLadybugTargetData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousLadybugTargetData> STREAM_CODEC = StreamCodec.composite(
            BlockTarget.STREAM_CODEC.apply(ByteBufCodecs.list()), MiraculousLadybugTargetData::blockTargets,
            EntityTarget.STREAM_CODEC.apply(ByteBufCodecs.list()), MiraculousLadybugTargetData::entityTargets,
            TommyLibExtraStreamCodecs.VEC_3.apply(ByteBufCodecs.list()), MiraculousLadybugTargetData::pathControlPoints,
            ByteBufCodecs.DOUBLE, MiraculousLadybugTargetData::splinePosition,
            MiraculousLadybugTargetData::new);

    public MiraculousLadybugTargetData() {
        this(ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), 0);
    }

    public MiraculousLadybugTargetData(Collection<BlockTarget> blockTargets, Collection<EntityTarget> entityTargets) {
        this(ImmutableList.copyOf(blockTargets), ImmutableList.copyOf(entityTargets), ImmutableList.of(), 0);
    }

    public void save(Entity entity, boolean syncToClient) {
        entity.setData(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TARGET, this);
        if (syncToClient) {
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncDataAttachmentPayload<>(entity.getId(), MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TARGET, this), entity.getServer());
        }
    }

    public static void remove(Entity entity, boolean syncToClient) {
        MiraculousLadybugTargetData newValue = new MiraculousLadybugTargetData();
        entity.setData(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TARGET, newValue);
        if (syncToClient) {
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncDataAttachmentPayload<>(entity.getId(), MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TARGET, newValue), entity.getServer());
        }
    }

    public MiraculousLadybugTargetData withSplinePosition(double splinePosition) {
        return new MiraculousLadybugTargetData(blockTargets, entityTargets, pathControlPoints, splinePosition);
    }

    public MiraculousLadybugTargetData calculateSpline(List<BlockTarget> updatedBlockTargets) {
        List<Vec3> controlPoints = calculateControlPoints(updatedBlockTargets, entityTargets);
        int controlPointsCount = controlPoints.size();
        Vec3 last = controlPoints.get(controlPointsCount - 1);
        Vec3 secondLast = controlPoints.get(controlPointsCount - 2);
        Vec3 newPoint = last.subtract(secondLast).normalize().scale(15).add(last);
        controlPoints.add(newPoint);
        MineraculousMathUtils.CatmullRom path = new MineraculousMathUtils.CatmullRom(controlPoints);
        return new MiraculousLadybugTargetData(updatedBlockTargets, entityTargets, controlPoints, path.getFirstParameter());
    }

    private static List<Vec3> calculateControlPoints(List<BlockTarget> blockTargets, List<EntityTarget> entityTargets) {
        ArrayList<Target> targets = new ArrayList<>();
        int blockCount = blockTargets.size();
        int entityCount = entityTargets.size();
        if (blockCount >= 3)
            targets.addAll(blockTargets.subList(2, blockCount));
        if (entityCount > 0)
            targets.addAll(entityTargets);
        BlockTarget spawnPos = blockTargets.getFirst();
        BlockTarget circlePos = blockTargets.get(1);
        targets = new ArrayList<>(MineraculousMathUtils.sortTargets(targets, circlePos));
        targets.addFirst(circlePos);
        targets.addFirst(spawnPos);
        int targetsCount = targets.size();
        ArrayList<Vec3> controlPoints = new ArrayList<>();
        if (targetsCount >= 3) {
            for (int i = 2; i < targetsCount; i++) {
                Target target = targets.get(i);
                if (target instanceof EntityTarget entityTarget) { // Spin Target
                    controlPoints.addAll(MineraculousMathUtils.spinAround(target.position(), entityTarget.width, entityTarget.height, Math.PI / 2d, entityTarget.height / 16d));
                } else { // Normal Target
                    controlPoints.add(target.position());
                }
            }
        }
        for (int i = 0; i <= 25; i++) {
            controlPoints.add(i, spawnPos.position().lerp(circlePos.position(), i / 25d));
        }
        return controlPoints;
    }
    public interface Target {
        Vec3 position();
    }

    public record BlockTarget(BlockPos blockPosition, UUID cause) implements Target {
        public static final Codec<BlockTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("block_position").forGetter(BlockTarget::blockPosition),
                UUIDUtil.CODEC.fieldOf("cause").forGetter(BlockTarget::cause)).apply(instance, BlockTarget::new));
        public static final StreamCodec<ByteBuf, BlockTarget> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, BlockTarget::blockPosition,
                UUIDUtil.STREAM_CODEC, BlockTarget::cause,
                BlockTarget::new);

        @Override
        public Vec3 position() {
            return blockPosition.getCenter();
        }

        public static BlockTarget wrap(BlockPos blockPosition) {
            return new BlockTarget(blockPosition, Util.NIL_UUID);
        }
    }

    public record EntityTarget(Vec3 position, UUID cause, float width, float height) implements Target {
        public static final Codec<EntityTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Vec3.CODEC.fieldOf("position").forGetter(EntityTarget::position),
                UUIDUtil.CODEC.fieldOf("cause").forGetter(EntityTarget::cause),
                Codec.FLOAT.fieldOf("width").forGetter(EntityTarget::width),
                Codec.FLOAT.fieldOf("height").forGetter(EntityTarget::height)).apply(instance, EntityTarget::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, EntityTarget> STREAM_CODEC = StreamCodec.composite(
                TommyLibExtraStreamCodecs.VEC_3, EntityTarget::position,
                UUIDUtil.STREAM_CODEC, EntityTarget::cause,
                ByteBufCodecs.FLOAT, EntityTarget::width,
                ByteBufCodecs.FLOAT, EntityTarget::height,
                EntityTarget::new);
    }
}
