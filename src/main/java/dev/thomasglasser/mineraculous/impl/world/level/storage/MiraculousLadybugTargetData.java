package dev.thomasglasser.mineraculous.impl.world.level.storage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.tommylib.api.network.ClientboundSyncDataAttachmentPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// note: the keys of the targets map are indexes for pathControlPoints
// TODO need an util for Multimap codec/streamcodec (for safety please also add linked multi map so the keys remain in the order i add them).
public record MiraculousLadybugTargetData(List<Vec3> pathControlPoints, Map<Integer, List<Target>> targets, double splinePosition) {

    public static final Codec<MiraculousLadybugTargetData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Vec3.CODEC.listOf().fieldOf("path_control_points").forGetter(MiraculousLadybugTargetData::pathControlPoints),
            Codec.unboundedMap(Codec.INT, TargetType.TARGET_CODEC.listOf()).fieldOf("targets").forGetter(MiraculousLadybugTargetData::targets),
            Codec.DOUBLE.fieldOf("spline_position").forGetter(MiraculousLadybugTargetData::splinePosition)).apply(instance, MiraculousLadybugTargetData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousLadybugTargetData> STREAM_CODEC = StreamCodec.composite(
            TommyLibExtraStreamCodecs.VEC_3.apply(ByteBufCodecs.list()), MiraculousLadybugTargetData::pathControlPoints,
            ByteBufCodecs.map(
                    Maps::newHashMapWithExpectedSize,
                    ByteBufCodecs.INT,
                    TargetType.TARGET_STREAM_CODEC.apply(ByteBufCodecs.list())),
            MiraculousLadybugTargetData::targets,
            ByteBufCodecs.DOUBLE, MiraculousLadybugTargetData::splinePosition,
            MiraculousLadybugTargetData::new);

    public MiraculousLadybugTargetData() {
        this(ImmutableList.of(), ImmutableMap.of(), 0);
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
        return new MiraculousLadybugTargetData(pathControlPoints, targets, splinePosition);
    }

    private static final int PREPEND_POINTS = 25;

    public static MiraculousLadybugTargetData create(List<BlockTarget> blockTargets, List<EntityTarget> entityTargets) {
        ArrayList<Vec3> controlPoints = new ArrayList<>();
        ArrayList<Target> targets = new ArrayList<>(sortedTargets(blockTargets, entityTargets)); // includes the spawn point and the fake circle target
        Multimap<Integer, Target> targetMap = LinkedHashMultimap.create(); // maps control points to targets
        int targetsCount = targets.size();
        if (targetsCount >= 3) {
            for (int i = 2; i < targetsCount; i++) {
                Target target = targets.get(i);
                if (target instanceof EntityTarget entityTarget) { // Spin Target
                    ArrayList<Vec3> spiralPoints = new ArrayList<>(MineraculousMathUtils.spinAround(target.position(), entityTarget.width, entityTarget.height, Math.PI / 2d, entityTarget.height / 16d));
                    int middleControlPointIndex = spiralPoints.size() / 2 + controlPoints.size() + PREPEND_POINTS;
                    controlPoints.addAll(spiralPoints);
                    targetMap.put(middleControlPointIndex, target);
                } else { // Normal Target
                    controlPoints.add(target.position());
                    targetMap.put(controlPoints.size() - 1 + PREPEND_POINTS, target);
                }
            }
        }
        Target spawnPos = targets.getFirst();
        Target circlePos = targets.get(1);
        for (int i = 0; i <= PREPEND_POINTS; i++) {
            controlPoints.add(i, spawnPos.position().lerp(circlePos.position(), i / (double) PREPEND_POINTS));
        }
        //adds an extra point so it does not die suddenly at the last target
        int controlPointsCount = controlPoints.size();
        Vec3 last = controlPoints.get(controlPointsCount - 1);
        Vec3 secondLast = controlPoints.get(controlPointsCount - 2);
        Vec3 newPoint = last.subtract(secondLast).normalize().scale(20).add(last);
        controlPoints.add(newPoint);
        Map<Integer, List<Target>> targetsMap = targetMap.asMap().entrySet().stream()
                .collect(Maps::newHashMap,
                        (m, e) -> m.put(e.getKey(), List.copyOf(e.getValue())),
                        Map::putAll);
        MineraculousMathUtils.CatmullRom path = new MineraculousMathUtils.CatmullRom(controlPoints);
        return new MiraculousLadybugTargetData(controlPoints, targetsMap, path.getFirstParameter());
    }

    private static List<Target> sortedTargets(List<BlockTarget> blockTargets, List<EntityTarget> entityTargets) {
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
        return targets;
    }
    public enum TargetType {
        BLOCK(BlockTarget.CODEC.fieldOf("block_target"), BlockTarget.STREAM_CODEC),
        ENTITY(EntityTarget.CODEC.fieldOf("entity_target"), EntityTarget.STREAM_CODEC);

        public static final Codec<TargetType> CODEC = Codec.STRING.xmap(TargetType::valueOf, TargetType::name);
        public static final Codec<Target> TARGET_CODEC = CODEC.dispatch(Target::type, TargetType::codec);

        public static final StreamCodec<ByteBuf, TargetType> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(TargetType::of, TargetType::getSerializedName);
        public static final StreamCodec<ByteBuf, Target> TARGET_STREAM_CODEC = STREAM_CODEC.dispatch(Target::type, TargetType::streamCodec);

        private final MapCodec<? extends Target> codec;
        private final StreamCodec<ByteBuf, ? extends Target> streamCodec;

        TargetType(MapCodec<? extends Target> codec, StreamCodec<ByteBuf, ? extends Target> streamCodec) {
            this.codec = codec;
            this.streamCodec = streamCodec;
        }

        MapCodec<? extends Target> codec() {
            return codec;
        }

        public StreamCodec<ByteBuf, ? extends Target> streamCodec() {
            return this.streamCodec;
        }

        public String getSerializedName() {
            return name().toLowerCase();
        }

        public static TargetType of(String name) {
            return valueOf(name.toUpperCase());
        }
    }

    public interface Target {
        Vec3 position();

        TargetType type();
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

        @Override
        public TargetType type() {
            return TargetType.BLOCK;
        }
    }

    public record EntityTarget(Vec3 position, UUID cause, float width, float height) implements Target {

        public static final Codec<EntityTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Vec3.CODEC.fieldOf("position").forGetter(EntityTarget::position),
                UUIDUtil.CODEC.fieldOf("cause").forGetter(EntityTarget::cause),
                Codec.FLOAT.fieldOf("width").forGetter(EntityTarget::width),
                Codec.FLOAT.fieldOf("height").forGetter(EntityTarget::height)).apply(instance, EntityTarget::new));
        public static final StreamCodec<ByteBuf, EntityTarget> STREAM_CODEC = StreamCodec.composite(
                TommyLibExtraStreamCodecs.VEC_3, EntityTarget::position,
                UUIDUtil.STREAM_CODEC, EntityTarget::cause,
                ByteBufCodecs.FLOAT, EntityTarget::width,
                ByteBufCodecs.FLOAT, EntityTarget::height,
                EntityTarget::new);
        @Override
        public TargetType type() {
            return TargetType.ENTITY;
        }
    }
}
