package dev.thomasglasser.mineraculous.impl.world.level.storage;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget.MiraculousLadybugTarget;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record MiraculousLadybugTargetData(List<Vec3> controlPoints, Multimap<Integer, MiraculousLadybugTarget<?>> targets) {
    private static final int PREPEND_POINTS = 25;

    public static final Codec<MiraculousLadybugTargetData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Vec3.CODEC.listOf().fieldOf("control_points").forGetter(MiraculousLadybugTargetData::controlPoints),
            Codec.unboundedMap(Codec.STRING, MiraculousLadybugTarget.CODEC.listOf()).fieldOf("targets").forGetter(MiraculousLadybugTargetData::targetsMap))
            .apply(instance, MiraculousLadybugTargetData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousLadybugTargetData> STREAM_CODEC = StreamCodec.composite(
            TommyLibExtraStreamCodecs.VEC_3.apply(ByteBufCodecs.list()), MiraculousLadybugTargetData::controlPoints,
            ByteBufCodecs.map(
                    Maps::newHashMapWithExpectedSize,
                    ByteBufCodecs.STRING_UTF8,
                    MiraculousLadybugTarget.STREAM_CODEC.apply(ByteBufCodecs.list())),
            MiraculousLadybugTargetData::targetsMap,
            MiraculousLadybugTargetData::new);

    public MiraculousLadybugTargetData() {
        this(ImmutableList.of(), ImmutableMap.of());
    }

    public MiraculousLadybugTargetData withTargets(Multimap<Integer, MiraculousLadybugTarget<?>> targets) {
        return new MiraculousLadybugTargetData(controlPoints, targets);
    }

    public MiraculousLadybugTargetData(List<Vec3> vec3s, Map<String, List<MiraculousLadybugTarget<?>>> targets) {
        this(vec3s, convertTargets(targets));
    }

    public static MiraculousLadybugTargetData create(List<MiraculousLadybugTarget<?>> targets, Vec3 spawnPosition, Vec3 circlePosition) {
        Multimap<Integer, MiraculousLadybugTarget<?>> targetMap = LinkedHashMultimap.create(); // maps control points to targets
        ArrayList<Vec3> controlPoints = new ArrayList<>();
        mapTargets(targets, targetMap, controlPoints);
        prependPoints(spawnPosition, circlePosition, controlPoints);
        extendLastControlPoint(controlPoints);
        return new MiraculousLadybugTargetData(controlPoints, targetMap);
    }

    public MiraculousLadybugTargetData tick(ServerLevel level) {
        Multimap<Integer, MiraculousLadybugTarget<?>> newTargets = LinkedHashMultimap.create();
        for (Map.Entry<Integer, MiraculousLadybugTarget<?>> entry : targets.entries()) {
            MiraculousLadybugTarget<?> target = entry.getValue().tick(level);
            newTargets.put(entry.getKey(), target);
        }
        return this.withTargets(newTargets);
    }

    private static Multimap<Integer, MiraculousLadybugTarget<?>> convertTargets(Map<String, List<MiraculousLadybugTarget<?>>> map) {
        Multimap<Integer, MiraculousLadybugTarget<?>> multimap = HashMultimap.create();
        map.forEach((index, targets) -> multimap.putAll(Integer.parseInt(index), targets));
        return multimap;
    }

    private Map<String, List<MiraculousLadybugTarget<?>>> targetsMap() {
        Map<String, List<MiraculousLadybugTarget<?>>> targetsMap = new HashMap<>();
        targets.asMap().forEach((index, targets) -> targetsMap.put(String.valueOf(index), ImmutableList.copyOf(targets)));
        return targetsMap;
    }

    private static void mapTargets(List<MiraculousLadybugTarget<?>> targets, Multimap<Integer, MiraculousLadybugTarget<?>> targetMap, ArrayList<Vec3> controlPoints) {
        for (MiraculousLadybugTarget<?> target : targets) {
            List<Vec3> targetControlPoints = target.getControlPoints();
            int middleIndex = targetControlPoints.size() / 2;
            Vec3 representativePoint = targetControlPoints.get(middleIndex);
            int globalIndex = controlPoints.indexOf(representativePoint);
            if (globalIndex == -1) {
                int mappedIndex = controlPoints.size() + middleIndex + PREPEND_POINTS;
                targetMap.put(mappedIndex, target);
            } else {
                targetMap.put(globalIndex, target);
            }
            controlPoints.addAll(targetControlPoints);
        }
    }

    private static void prependPoints(Vec3 spawnPosition, Vec3 circlePosition, ArrayList<Vec3> controlPoints) {
        for (int i = 0; i < PREPEND_POINTS; i++) {
            controlPoints.add(i, spawnPosition.lerp(circlePosition, i / (double) PREPEND_POINTS));
        }
    }

    private static void extendLastControlPoint(List<Vec3> controlPoints) {
        int size = controlPoints.size();
        if (size > 2) {
            Vec3 last = controlPoints.get(size - 1);
            Vec3 secondLast = controlPoints.get(size - 2);
            Vec3 newPoint = last.subtract(secondLast).normalize().scale(50).add(last);
            controlPoints.add(newPoint);
        }
    }
}
