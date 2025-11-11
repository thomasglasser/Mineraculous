package dev.thomasglasser.mineraculous.impl.world.level.storage;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

// Note: the keys of the targets map are indexes for pathControlPoints.
public record MiraculousLadybugTargetData(List<Vec3> pathControlPoints, Multimap<Integer, MiraculousLadybugTarget> targets, double splinePosition) {

    // Number of control points to interpolate between spawnPos and circlePos.
    // Chosen to create a smooth curve without affecting performance.
    // Since circlePos is ~50 blocks away from spawnPos, 25 points gives roughly 2 blocks per point.
    // This could be lowered if performance becomes an issue.
    private static final int PREPEND_POINTS = 25;

    public static final Codec<MiraculousLadybugTargetData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Vec3.CODEC.listOf().fieldOf("path_control_points").forGetter(MiraculousLadybugTargetData::pathControlPoints),
            Codec.unboundedMap(Codec.STRING, MiraculousLadybugTargetType.TARGET_CODEC.listOf()).fieldOf("targets").forGetter(MiraculousLadybugTargetData::targetsMap),
            Codec.DOUBLE.fieldOf("spline_position").forGetter(MiraculousLadybugTargetData::splinePosition)).apply(instance, MiraculousLadybugTargetData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousLadybugTargetData> STREAM_CODEC = StreamCodec.composite(
            TommyLibExtraStreamCodecs.VEC_3.apply(ByteBufCodecs.list()), MiraculousLadybugTargetData::pathControlPoints,
            ByteBufCodecs.map(
                    Maps::newHashMapWithExpectedSize,
                    ByteBufCodecs.STRING_UTF8,
                    MiraculousLadybugTargetType.TARGET_STREAM_CODEC.apply(ByteBufCodecs.list())),
            MiraculousLadybugTargetData::targetsMap,
            ByteBufCodecs.DOUBLE, MiraculousLadybugTargetData::splinePosition,
            MiraculousLadybugTargetData::new);
    public MiraculousLadybugTargetData(List<Vec3> pathControlPoints, Map<String, List<MiraculousLadybugTarget>> targets, double splinePosition) {
        this(pathControlPoints, convertTargets(targets), splinePosition);
    }

    public MiraculousLadybugTargetData() {
        this(ImmutableList.of(), ImmutableMap.of(), 0);
    }

    public static MiraculousLadybugTargetData create(List<MiraculousLadybugBlockTarget> blockTargets, List<MiraculousLadybugEntityTarget> entityTargets) {
        ArrayList<Vec3> controlPoints = new ArrayList<>();
        ArrayList<MiraculousLadybugTarget> targets = new ArrayList<>(sortedTargets(blockTargets, entityTargets)); // includes the spawn point and the fake circle target
        Multimap<Integer, MiraculousLadybugTarget> targetMap = LinkedHashMultimap.create(); // maps control points to targets
        if (targets.size() >= 3) {
            for (int i = 2; i < targets.size(); i++) {
                mapTargetsToControlPoints(controlPoints, targetMap, targets.get(i), PREPEND_POINTS);
            }
        }
        MiraculousLadybugTarget spawnPos = targets.getFirst();
        MiraculousLadybugTarget circlePos = targets.get(1);
        prependSpawnPoints(controlPoints, spawnPos, circlePos, PREPEND_POINTS);
        extendLastControlPoint(controlPoints);
        MineraculousMathUtils.CatmullRom path = new MineraculousMathUtils.CatmullRom(controlPoints);
        return new MiraculousLadybugTargetData(controlPoints, targetMap, path.getFirstParameter());
    }

    public MiraculousLadybugTargetData tick(ServerLevel level) {
        Multimap<Integer, MiraculousLadybugTarget> newTargets = LinkedHashMultimap.create();

        for (Map.Entry<Integer, MiraculousLadybugTarget> entry : targets.entries()) {
            MiraculousLadybugTarget target = entry.getValue();
            if (target instanceof MiraculousLadybugBlockTarget blockTarget) {
                target = blockTarget.tick(level);
            }
            newTargets.put(entry.getKey(), target);
        }

        return new MiraculousLadybugTargetData(pathControlPoints, newTargets, splinePosition);
    }

    private static void mapTargetsToControlPoints(List<Vec3> controlPoints, Multimap<Integer, MiraculousLadybugTarget> targetMap, MiraculousLadybugTarget target, int prependPoints) {
        if (target instanceof MiraculousLadybugEntityTarget entityTarget) {
            List<Vec3> spiralPoints = entityTarget.getSpiralPoints();
            int middleIndex = spiralPoints.size() / 2 + controlPoints.size() + prependPoints;
            controlPoints.addAll(spiralPoints);
            targetMap.put(middleIndex, target);
        } else if (target instanceof MiraculousLadybugBlockTarget) {
            controlPoints.add(target.position());
            targetMap.put(controlPoints.size() - 1 + prependPoints, target);
        }
    }

    private static void prependSpawnPoints(List<Vec3> controlPoints, MiraculousLadybugTarget spawnPos, MiraculousLadybugTarget circlePos, int prependPoints) {
        for (int i = 0; i <= prependPoints; i++) {
            controlPoints.add(i, spawnPos.position().lerp(circlePos.position(), i / (double) prependPoints));
        }
    }

    private static void extendLastControlPoint(List<Vec3> controlPoints) {
        int size = controlPoints.size();
        if (size < 2) return;
        Vec3 last = controlPoints.get(size - 1);
        Vec3 secondLast = controlPoints.get(size - 2);
        Vec3 newPoint = last.subtract(secondLast).normalize().scale(50).add(last);
        controlPoints.add(newPoint);
    }

    private static Multimap<Integer, MiraculousLadybugTarget> convertTargets(Map<String, List<MiraculousLadybugTarget>> map) {
        Multimap<Integer, MiraculousLadybugTarget> multimap = HashMultimap.create();
        map.forEach((index, targets) -> multimap.putAll(Integer.parseInt(index), targets));
        return multimap;
    }

    public Map<String, List<MiraculousLadybugTarget>> targetsMap() {
        Map<String, List<MiraculousLadybugTarget>> targetsMap = new HashMap<>();
        targets.asMap().forEach((index, targets) -> targetsMap.put(String.valueOf(index), ImmutableList.copyOf(targets)));
        return targetsMap;
    }

    public MiraculousLadybugTargetData withSplinePosition(double splinePosition) {
        return new MiraculousLadybugTargetData(pathControlPoints, targets, splinePosition);
    }

    private static List<MiraculousLadybugTarget> sortedTargets(List<MiraculousLadybugBlockTarget> blockTargets, List<MiraculousLadybugEntityTarget> entityTargets) {
        ArrayList<MiraculousLadybugTarget> targets = new ArrayList<>();
        int blockCount = blockTargets.size();
        if (blockCount >= 3) targets.addAll(blockTargets.subList(2, blockCount));
        targets.addAll(entityTargets);
        MiraculousLadybugBlockTarget spawnPos = blockTargets.getFirst();
        MiraculousLadybugBlockTarget circlePos = blockTargets.get(1);
        List<MiraculousLadybugTarget> sorted = new ArrayList<>(/*MineraculousMathUtils.sortTargets(targets, circlePos)*/);
        sorted.addFirst(circlePos);
        sorted.addFirst(spawnPos);
        return sorted;
    }
}
