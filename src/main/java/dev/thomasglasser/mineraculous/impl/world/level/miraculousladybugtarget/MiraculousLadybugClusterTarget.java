package dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public record MiraculousLadybugClusterTarget(List<List<MiraculousLadybugTarget<?>>> layers, Vec3 center, double width, double height, int tickCount) implements MiraculousLadybugTarget<MiraculousLadybugClusterTarget> {

    public static final MapCodec<MiraculousLadybugClusterTarget> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            MiraculousLadybugTarget.CODEC.listOf().listOf().fieldOf("layers").forGetter(MiraculousLadybugClusterTarget::layers),
            Vec3.CODEC.fieldOf("center").forGetter(MiraculousLadybugClusterTarget::center),
            Codec.DOUBLE.fieldOf("width").forGetter(MiraculousLadybugClusterTarget::width),
            Codec.DOUBLE.fieldOf("height").forGetter(MiraculousLadybugClusterTarget::height),
            Codec.INT.fieldOf("tick_count").forGetter(MiraculousLadybugClusterTarget::tickCount)).apply(instance, MiraculousLadybugClusterTarget::new));
    public static final Codec<MiraculousLadybugClusterTarget> CODEC = MAP_CODEC.codec();
    public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousLadybugClusterTarget> STREAM_CODEC = StreamCodec.composite(
            MiraculousLadybugTarget.STREAM_CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs.list()), MiraculousLadybugClusterTarget::layers,
            TommyLibExtraStreamCodecs.VEC_3, MiraculousLadybugClusterTarget::center,
            ByteBufCodecs.DOUBLE, MiraculousLadybugClusterTarget::width,
            ByteBufCodecs.DOUBLE, MiraculousLadybugClusterTarget::height,
            ByteBufCodecs.INT, MiraculousLadybugClusterTarget::tickCount,
            MiraculousLadybugClusterTarget::new);
    public MiraculousLadybugClusterTarget withTicks(int t) {
        return new MiraculousLadybugClusterTarget(layers, center, width, height, t);
    }

    public MiraculousLadybugClusterTarget withLayers(List<List<MiraculousLadybugTarget<?>>> layers) {
        return new MiraculousLadybugClusterTarget(layers, center, width, height, tickCount);
    }

    @Override
    public MiraculousLadybugTargetType<MiraculousLadybugClusterTarget> type() {
        return MiraculousLadybugTargetTypes.CLUSTER.get();
    }

    @Override
    public MiraculousLadybugTarget<MiraculousLadybugClusterTarget> revert(ServerLevel level, boolean instant) {
        if (instant) {
            for (List<MiraculousLadybugTarget<?>> layer : layers) {
                for (MiraculousLadybugTarget<?> target : layer) {
                    target.revert(level, true);
                }
            }
            return withLayers(List.of());
        }
        if (tickCount == -1 && !layers.isEmpty())
            return withTicks(0);
        return this;
    }

    @Override
    public MiraculousLadybugTarget<MiraculousLadybugClusterTarget> tick(ServerLevel level) {
        if (tickCount >= 0) {
            if (layers.isEmpty())
                return withTicks(-1);
            if (tickCount % SharedConstants.TICKS_PER_SECOND == 0)
                return revertLayer(level);
            return withTicks(tickCount + 1);
        }
        return this;
    }

    private MiraculousLadybugClusterTarget revertLayer(ServerLevel level) {
        if (!layers.isEmpty()) {
            List<MiraculousLadybugTarget<?>> layer = layers.getFirst();
            for (MiraculousLadybugTarget<?> target : layer)
                target.revert(level, false);
            return withLayers(new ArrayList<>(layers.subList(1, layers.size())));
        }
        return this;
    }

    @Override
    public Vec3 position() {
        return center;
    }

    @Override
    public List<Vec3> getControlPoints() {
        Vec3 pos = center.add(0, -height / 2, 0);
        return MineraculousMathUtils.spinAround(
                pos,
                width,
                width,
                height,
                2 * Math.PI / width,
                height / 16);
    }

    @Override
    public boolean isReverting() {
        return tickCount != -1;
    }

    @Override
    public boolean shouldExpandMiraculousLadybug() {
        return width > 5;
    }

    //BFS FLOOD FILL
    private static final int[][] NEIGHBOR_OFFSETS = createNeighborOffsets();

    private static int[][] createNeighborOffsets() {
        int[][] off = new int[26][3];
        int i = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    off[i++] = new int[] { dx, dy, dz };
                }
            }
        }
        return off;
    }

    public static Collection<MiraculousLadybugTarget<?>> reduceNearbyTargets(Collection<MiraculousLadybugTarget<?>> input) {
        Map<Vec3, MiraculousLadybugTarget<?>> targets = new Object2ObjectOpenHashMap<>(input.size());
        for (MiraculousLadybugTarget<?> target : input) {
            targets.put(target.position(), target);
        }

        Set<Vec3> unvisited = new ObjectOpenHashSet<>(targets.keySet());
        List<MiraculousLadybugTarget<?>> clusters = new ReferenceArrayList<>();

        while (!unvisited.isEmpty()) {
            Vec3 start = unvisited.iterator().next();
            unvisited.remove(start);

            List<Vec3> cluster = new ReferenceArrayList<>();
            cluster.add(start);

            double minX = start.x();
            double maxX = start.x();
            double minY = start.y();
            double maxY = start.y();
            double minZ = start.z();
            double maxZ = start.z();

            ArrayDeque<Vec3> queue = new ArrayDeque<>();
            queue.add(start);

            while (!queue.isEmpty()) {
                Vec3 pos = queue.poll();

                minX = Math.min(minX, pos.x());
                maxX = Math.max(maxX, pos.x());
                minY = Math.min(minY, pos.y());
                maxY = Math.max(maxY, pos.y());
                minZ = Math.min(minZ, pos.z());
                maxZ = Math.max(maxZ, pos.z());

                double px = pos.x();
                double py = pos.y();
                double pz = pos.z();

                if (px < minX) minX = px;
                else if (px > maxX) maxX = px;
                if (py < minY) minY = py;
                else if (py > maxY) maxY = py;
                if (pz < minZ) minZ = pz;
                else if (pz > maxZ) maxZ = pz;

                for (int[] o : NEIGHBOR_OFFSETS) {
                    Vec3 current = new Vec3(px + o[0], py + o[1], pz + o[2]);

                    if (unvisited.remove(current)) {
                        queue.add(current);
                        cluster.add(current);
                    }
                }
            }
            if (cluster.size() > 1) {
                double centerX = (minX + maxX) / 2;
                double centerY = (minY + maxY) / 2;
                double centerZ = (minZ + maxZ) / 2;
                Vec3 center = new Vec3(centerX + 0.5, centerY + 0.5, centerZ + 0.5);

                double width = ((maxX - minX + maxZ - minZ + 2) / 2d);
                double height = (maxY - minY + 1);

                List<MiraculousLadybugTarget<?>> clusterTargets = new ReferenceArrayList<>(cluster.size());
                for (Vec3 pos : cluster) {
                    clusterTargets.add(targets.get(pos));
                }
                MiraculousLadybugClusterTarget target = create(clusterTargets, center, width, height);
                if (target != null)
                    clusters.add(target);
            } else if (cluster.size() == 1) {
                MiraculousLadybugTarget<?> single = targets.get(cluster.getFirst());
                if (single != null) {
                    clusters.add(single);
                } else {
                    throw new IllegalStateException("Inside MiraculousLadybugClusterTarget#reduceNearbyTargets a list clump contained a Vec3 that wasn't in targets: " + cluster.getFirst());
                }
            }
        }
        return clusters;
    }

    public static MiraculousLadybugClusterTarget create(List<MiraculousLadybugTarget<?>> targets, Vec3 center, double width, double height) {
        if (targets == null || targets.isEmpty()) return null;

        MiraculousLadybugTarget<?> start = null;
        double bestDist = Double.MAX_VALUE;
        for (MiraculousLadybugTarget<?> target : targets) {
            double dist = target.position().distanceToSqr(center);
            if (dist < bestDist) {
                bestDist = dist;
                start = target;
            }
        }

        if (start == null) {
            throw new IllegalStateException("Passed a list of MiraculousLadybugTargets with null contents when calling MiraculousLadybugClusterTarget#create!");
        }

        Map<Vec3, MiraculousLadybugTarget<?>> targetMap = new HashMap<>();
        for (MiraculousLadybugTarget<?> target : targets) targetMap.put(target.position(), target);

        ArrayDeque<MiraculousLadybugTarget<?>> queue = new ArrayDeque<>();
        Set<Vec3> visited = new HashSet<>();

        List<List<MiraculousLadybugTarget<?>>> layers = new ArrayList<>();
        queue.add(start);
        visited.add(start.position());

        while (!queue.isEmpty()) {
            int qSize = queue.size();
            List<MiraculousLadybugTarget<?>> layer = new ReferenceArrayList<>(qSize);

            for (int i = 0; i < qSize; i++) {
                MiraculousLadybugTarget<?> curr = queue.poll();
                layer.add(curr);

                Vec3 pos = curr.position();

                for (int[] off : NEIGHBOR_OFFSETS) {
                    Vec3 neighbor = pos.add(off[0], off[1], off[2]);
                    if (visited.contains(neighbor)) continue;

                    MiraculousLadybugTarget<?> nb = targetMap.get(neighbor);
                    if (nb == null) continue;

                    visited.add(neighbor);
                    queue.add(nb);
                }
            }

            layers.add(layer);
        }

        return new MiraculousLadybugClusterTarget(layers, center, width, height, -1);
    }
}
