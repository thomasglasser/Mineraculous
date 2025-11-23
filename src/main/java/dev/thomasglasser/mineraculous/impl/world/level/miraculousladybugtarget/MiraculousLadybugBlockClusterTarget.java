package dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import io.netty.buffer.ByteBuf;
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
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public record MiraculousLadybugBlockClusterTarget(List<List<MiraculousLadybugBlockTarget>> blockLayers, Vec3 center, double width, double height, int tickCount) implements MiraculousLadybugTarget<MiraculousLadybugBlockClusterTarget> {

    public static final MapCodec<MiraculousLadybugBlockClusterTarget> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            MiraculousLadybugBlockTarget.CODEC.listOf().listOf().fieldOf("block_layers").forGetter(MiraculousLadybugBlockClusterTarget::blockLayers),
            Vec3.CODEC.fieldOf("center_position").forGetter(MiraculousLadybugBlockClusterTarget::center),
            Codec.DOUBLE.fieldOf("width").forGetter(MiraculousLadybugBlockClusterTarget::width),
            Codec.DOUBLE.fieldOf("height").forGetter(MiraculousLadybugBlockClusterTarget::height),
            Codec.INT.fieldOf("tick_count").forGetter(MiraculousLadybugBlockClusterTarget::tickCount)).apply(instance, MiraculousLadybugBlockClusterTarget::new));
    public static final Codec<MiraculousLadybugBlockClusterTarget> CODEC = MAP_CODEC.codec();
    public static final StreamCodec<ByteBuf, MiraculousLadybugBlockClusterTarget> STREAM_CODEC = StreamCodec.composite(
            MiraculousLadybugBlockTarget.STREAM_CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs.list()), MiraculousLadybugBlockClusterTarget::blockLayers,
            TommyLibExtraStreamCodecs.VEC_3, MiraculousLadybugBlockClusterTarget::center,
            ByteBufCodecs.DOUBLE, MiraculousLadybugBlockClusterTarget::width,
            ByteBufCodecs.DOUBLE, MiraculousLadybugBlockClusterTarget::height,
            ByteBufCodecs.INT, MiraculousLadybugBlockClusterTarget::tickCount,
            MiraculousLadybugBlockClusterTarget::new);
    public MiraculousLadybugBlockClusterTarget withTicks(int t) {
        return new MiraculousLadybugBlockClusterTarget(blockLayers, center, width, height, t);
    }

    public MiraculousLadybugBlockClusterTarget withBlockLayers(List<List<MiraculousLadybugBlockTarget>> bl) {
        return new MiraculousLadybugBlockClusterTarget(bl, center, width, height, tickCount);
    }

    @Override
    public MiraculousLadybugTargetType<MiraculousLadybugBlockClusterTarget> type() {
        return MiraculousLadybugTargetTypes.BLOCK_CLUSTER.get();
    }

    @Override
    public MiraculousLadybugTarget<MiraculousLadybugBlockClusterTarget> revert(ServerLevel level, boolean instant) {
        if (instant) {
            for (List<MiraculousLadybugBlockTarget> layer : blockLayers) {
                for (MiraculousLadybugBlockTarget blockTarget : layer) {
                    blockTarget.revert(level, true);
                }
            }
            return withBlockLayers(List.of());
        }
        if (tickCount == -1 && !blockLayers.isEmpty())
            return withTicks(0);
        return this;
    }

    @Override
    public MiraculousLadybugTarget<MiraculousLadybugBlockClusterTarget> tick(ServerLevel level) {
        if (tickCount >= 0) {
            if (blockLayers.isEmpty())
                return withTicks(-1);
            if (tickCount % 20 == 0)
                return revertLayer(level);
            return withTicks(tickCount + 1);
        }
        return this;
    }

    private MiraculousLadybugBlockClusterTarget revertLayer(ServerLevel level) {
        if (!blockLayers.isEmpty()) {
            List<MiraculousLadybugBlockTarget> layer = blockLayers.getFirst();
            for (MiraculousLadybugBlockTarget blockTarget : layer)
                blockTarget.revert(level, false);
            return withBlockLayers(new ArrayList<>(blockLayers.subList(1, blockLayers.size())));
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

    public static Collection<MiraculousLadybugTarget<?>> reduceNearbyBlocks(Collection<MiraculousLadybugBlockTarget> input) {
        Map<BlockPos, MiraculousLadybugBlockTarget> targets = new Object2ObjectOpenHashMap<>(input.size());
        for (MiraculousLadybugBlockTarget target : input) {
            targets.put(target.blockPos(), target);
        }

        Set<BlockPos> unvisited = new ObjectOpenHashSet<>(targets.keySet());
        List<MiraculousLadybugTarget<?>> clumps = new ReferenceArrayList<>();

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        while (!unvisited.isEmpty()) {
            BlockPos start = unvisited.iterator().next();
            unvisited.remove(start);

            List<BlockPos> clump = new ReferenceArrayList<>();
            clump.add(start);

            int minX = start.getX();
            int maxX = start.getX();
            int minY = start.getY();
            int maxY = start.getY();
            int minZ = start.getZ();
            int maxZ = start.getZ();

            ArrayDeque<BlockPos> queue = new ArrayDeque<>();
            queue.add(start);

            while (!queue.isEmpty()) {
                BlockPos pos = queue.poll();

                minX = Math.min(minX, pos.getX());
                maxX = Math.max(maxX, pos.getX());
                minY = Math.min(minY, pos.getY());
                maxY = Math.max(maxY, pos.getY());
                minZ = Math.min(minZ, pos.getZ());
                maxZ = Math.max(maxZ, pos.getZ());

                int px = pos.getX();
                int py = pos.getY();
                int pz = pos.getZ();

                if (px < minX) minX = px;
                else if (px > maxX) maxX = px;
                if (py < minY) minY = py;
                else if (py > maxY) maxY = py;
                if (pz < minZ) minZ = pz;
                else if (pz > maxZ) maxZ = pz;

                for (int[] o : NEIGHBOR_OFFSETS) {
                    mutable.set(px + o[0], py + o[1], pz + o[2]);

                    if (unvisited.remove(mutable)) {
                        BlockPos fixed = mutable.immutable();

                        queue.add(fixed);
                        clump.add(fixed);
                    }
                }
            }
            if (clump.size() > 1) {
                int centerX = (minX + maxX) / 2;
                int centerY = (minY + maxY) / 2;
                int centerZ = (minZ + maxZ) / 2;
                Vec3 center = new Vec3(centerX + 0.5, centerY + 0.5, centerZ + 0.5);

                double width = ((maxX - minX + maxZ - minZ + 2) / 2d);
                double height = (maxY - minY + 1);

                List<MiraculousLadybugBlockTarget> blockTargets = new ReferenceArrayList<>(clump.size());
                for (BlockPos pos : clump) {
                    blockTargets.add(targets.get(pos));
                }
                MiraculousLadybugBlockClusterTarget cluster = create(blockTargets, center, width, height);
                if (cluster != null)
                    clumps.add(cluster);
            } else if (clump.size() == 1) {
                MiraculousLadybugBlockTarget single = targets.get(clump.getFirst());
                if (single != null) {
                    clumps.add(single);
                } else {
                    throw new IllegalStateException("Inside MiraculousLadybugBlockClusterTarget#reduceClumps a list clump contained a BlockPos that wasn't in allBlocks: " + clump.getFirst());
                }
            }
        }
        return clumps;
    }

    public static MiraculousLadybugBlockClusterTarget create(List<MiraculousLadybugBlockTarget> blockTargets, Vec3 center, double width, double height) {
        if (blockTargets == null || blockTargets.isEmpty()) return null;

        MiraculousLadybugBlockTarget start = null;
        double bestDist = Double.MAX_VALUE;
        for (MiraculousLadybugBlockTarget b : blockTargets) {
            double dist = b.position().distanceToSqr(center);
            if (dist < bestDist) {
                bestDist = dist;
                start = b;
            }
        }

        if (start == null) {
            throw new IllegalStateException("Passed a list of MiraculousLadybugBlockTarget with null contents when calling MiraculousLadybugBlockCluster#create!");
        }

        Map<BlockPos, MiraculousLadybugBlockTarget> map = new HashMap<>();
        for (MiraculousLadybugBlockTarget b : blockTargets) map.put(b.blockPos(), b);

        ArrayDeque<MiraculousLadybugBlockTarget> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();

        List<List<MiraculousLadybugBlockTarget>> layers = new ArrayList<>();
        queue.add(start);
        visited.add(start.blockPos());

        while (!queue.isEmpty()) {
            int qsize = queue.size();
            List<MiraculousLadybugBlockTarget> layer = new ArrayList<>(qsize);

            for (int i = 0; i < qsize; i++) {
                MiraculousLadybugBlockTarget curr = queue.poll();
                layer.add(curr);

                BlockPos pos = curr.blockPos();

                for (int[] off : NEIGHBOR_OFFSETS) {
                    BlockPos neighbor = pos.offset(off[0], off[1], off[2]);
                    if (visited.contains(neighbor)) continue;

                    MiraculousLadybugBlockTarget nb = map.get(neighbor);
                    if (nb == null) continue;

                    visited.add(neighbor);
                    queue.add(nb);
                }
            }

            layers.add(layer);
        }

        return new MiraculousLadybugBlockClusterTarget(layers, center, width, height, -1);
    }
}
