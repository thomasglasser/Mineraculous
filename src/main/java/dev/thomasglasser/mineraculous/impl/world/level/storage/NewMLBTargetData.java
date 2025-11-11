package dev.thomasglasser.mineraculous.impl.world.level.storage;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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

public record NewMLBTargetData(List<Vec3> controlPoints, Multimap<Integer, NewMLBTarget> targets) {
    private static final int PREPEND_POINTS = 25;

    public static final Codec<NewMLBTargetData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Vec3.CODEC.listOf().fieldOf("control_points").forGetter(NewMLBTargetData::controlPoints),
            Codec.unboundedMap(Codec.STRING, NewMLBTargetType.TARGET_CODEC.listOf()).fieldOf("targets").forGetter(NewMLBTargetData::targetsMap))
            .apply(instance, NewMLBTargetData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, NewMLBTargetData> STREAM_CODEC = StreamCodec.composite(
            TommyLibExtraStreamCodecs.VEC_3.apply(ByteBufCodecs.list()), NewMLBTargetData::controlPoints,
            ByteBufCodecs.map(
                    Maps::newHashMapWithExpectedSize,
                    ByteBufCodecs.STRING_UTF8,
                    NewMLBTargetType.TARGET_STREAM_CODEC.apply(ByteBufCodecs.list())),
            NewMLBTargetData::targetsMap,
            NewMLBTargetData::new);

    public NewMLBTargetData() {
        this(ImmutableList.of(), ImmutableMap.of());
    }

    public NewMLBTargetData withTargets(Multimap<Integer, NewMLBTarget> targets) {
        return new NewMLBTargetData(controlPoints, targets);
    }

    public NewMLBTargetData(List<Vec3> vec3s, Map<String, List<NewMLBTarget>> targets) {
        this(vec3s, convertTargets(targets));
    }

    public static NewMLBTargetData create(List<NewMLBTarget> targets, Vec3 spawnPosition, Vec3 circlePosition) {
        Multimap<Integer, NewMLBTarget> targetMap = LinkedHashMultimap.create(); // maps control points to targets
        ArrayList<Vec3> controlPoints = new ArrayList<>();
        mapTargets(targets, targetMap, controlPoints);
        prependPoints(spawnPosition, circlePosition, controlPoints);
        extendLastControlPoint(controlPoints);
        return new NewMLBTargetData(controlPoints, targetMap);
    }

    public NewMLBTargetData tick(ServerLevel level) {
        Multimap<Integer, NewMLBTarget> newTargets = LinkedHashMultimap.create();
        for (Map.Entry<Integer, NewMLBTarget> entry : targets.entries()) {
            NewMLBTarget target = entry.getValue().tick(level);
            newTargets.put(entry.getKey(), target);
        }
        return this.withTargets(newTargets);
    }

    private static Multimap<Integer, NewMLBTarget> convertTargets(Map<String, List<NewMLBTarget>> map) {
        Multimap<Integer, NewMLBTarget> multimap = HashMultimap.create();
        map.forEach((index, targets) -> multimap.putAll(Integer.parseInt(index), targets));
        return multimap;
    }

    private Map<String, List<NewMLBTarget>> targetsMap() {
        Map<String, List<NewMLBTarget>> targetsMap = new HashMap<>();
        targets.asMap().forEach((index, targets) -> targetsMap.put(String.valueOf(index), ImmutableList.copyOf(targets)));
        return targetsMap;
    }

    private static void mapTargets(List<NewMLBTarget> targets, Multimap<Integer, NewMLBTarget> targetMap, ArrayList<Vec3> controlPoints) {
        for (NewMLBTarget target : targets) {
            List<Vec3> targetControlPoints = target.getControlPoints();
            int middleIndex = targetControlPoints.size() / 2 + targetControlPoints.size() + PREPEND_POINTS - 1;
            targetMap.put(middleIndex, target);
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
