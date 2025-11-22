package dev.thomasglasser.mineraculous.impl.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import io.netty.buffer.ByteBuf;
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

public record MiraculousLadybugBlockClusterTarget(List<List<MiraculousLadybugBlockTarget>> blockLayers, Vec3 center, double width, double height, int tick) implements MiraculousLadybugTarget {

    public static final Codec<MiraculousLadybugBlockClusterTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MiraculousLadybugBlockTarget.CODEC.listOf().listOf().fieldOf("block_layers").forGetter(MiraculousLadybugBlockClusterTarget::blockLayers),
            Vec3.CODEC.fieldOf("center_position").forGetter(MiraculousLadybugBlockClusterTarget::center),
            Codec.DOUBLE.fieldOf("width").forGetter(MiraculousLadybugBlockClusterTarget::width),
            Codec.DOUBLE.fieldOf("height").forGetter(MiraculousLadybugBlockClusterTarget::height),
            Codec.INT.fieldOf("tick").forGetter(MiraculousLadybugBlockClusterTarget::tick)).apply(instance, MiraculousLadybugBlockClusterTarget::new));
    public static final StreamCodec<ByteBuf, MiraculousLadybugBlockClusterTarget> STREAM_CODEC = StreamCodec.composite(
            MiraculousLadybugBlockTarget.STREAM_CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs.list()), MiraculousLadybugBlockClusterTarget::blockLayers,
            TommyLibExtraStreamCodecs.VEC_3, MiraculousLadybugBlockClusterTarget::center,
            ByteBufCodecs.DOUBLE, MiraculousLadybugBlockClusterTarget::width,
            ByteBufCodecs.DOUBLE, MiraculousLadybugBlockClusterTarget::height,
            ByteBufCodecs.INT, MiraculousLadybugBlockClusterTarget::tick,
            MiraculousLadybugBlockClusterTarget::new);
    public MiraculousLadybugBlockClusterTarget withTicks(int t) {
        return new MiraculousLadybugBlockClusterTarget(blockLayers, center, width, height, t);
    }

    public MiraculousLadybugBlockClusterTarget withBlockLayers(List<List<MiraculousLadybugBlockTarget>> bl) {
        return new MiraculousLadybugBlockClusterTarget(bl, center, width, height, tick);
    }

    @Override
    public Vec3 getPosition() {
        return this.center;
    }

    @Override
    public MiraculousLadybugTargetType type() {
        return MiraculousLadybugTargetType.BLOCK_CLUSTER;
    }

    @Override
    public boolean isReverting() {
        return tick != -1;
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
    public MiraculousLadybugTarget startReversion(ServerLevel level) {
        if (tick == -1 && !blockLayers.isEmpty())
            return withTicks(0);
        return this;
    }

    @Override
    public MiraculousLadybugTarget instantRevert(ServerLevel level) {
        for (List<MiraculousLadybugBlockTarget> layer : blockLayers) {
            for (MiraculousLadybugBlockTarget blockTarget : layer) {
                blockTarget.instantRevert(level);
            }
        }
        return withBlockLayers(List.of());
    }

    @Override
    public MiraculousLadybugTarget tick(ServerLevel level) {
        if (tick >= 0) {
            if (blockLayers.isEmpty())
                return withTicks(-1);
            if (tick % 20 == 0 || tick == 0)
                return revertLayer(level);
            int newTicks = tick + 1;
            return withTicks(newTicks);
        }
        return this;
    }

    @Override
    public void spawnParticles(ServerLevel level) { // No actual usage ever.
        for (List<MiraculousLadybugBlockTarget> layer : blockLayers)
            for (MiraculousLadybugBlockTarget blockTarget : layer)
                blockTarget.spawnParticles(level);
    }

    private MiraculousLadybugBlockClusterTarget revertLayer(ServerLevel level) {
        if (!blockLayers.isEmpty()) {
            List<MiraculousLadybugBlockTarget> layer = blockLayers.getFirst();
            for (MiraculousLadybugBlockTarget blockTarget : layer)
                blockTarget.instantRevert(level);
            return withBlockLayers(new ArrayList<>(blockLayers.subList(1, blockLayers.size())));
        }
        return this;
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

    public static Collection<MiraculousLadybugTarget> reduceNearbyBlocks(Collection<MiraculousLadybugTarget> input) {
        List<MiraculousLadybugBlockTarget> list = new ArrayList<>(input.size());
        for (MiraculousLadybugTarget target : input) {
            if (target instanceof MiraculousLadybugBlockTarget blockTarget)
                list.add(blockTarget);
        }
        return reduceClumps(list);
    }

    public static Collection<MiraculousLadybugTarget> reduceClumps(Collection<MiraculousLadybugBlockTarget> input) {
        Map<BlockPos, MiraculousLadybugBlockTarget> allBlocks = new HashMap<>(input.size());
        for (MiraculousLadybugBlockTarget bt : input) {
            allBlocks.put(bt.blockPos(), bt);
        }

        Set<BlockPos> unvisited = new HashSet<>(allBlocks.keySet());
        List<MiraculousLadybugTarget> clumps = new ArrayList<>();

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        while (!unvisited.isEmpty()) {
            BlockPos start = unvisited.iterator().next();
            unvisited.remove(start);

            List<BlockPos> clump = new ArrayList<>();
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

                List<MiraculousLadybugBlockTarget> blockTargets = new ArrayList<>(clump.size());
                for (BlockPos pos : clump) {
                    blockTargets.add(allBlocks.get(pos));
                }
                MiraculousLadybugBlockClusterTarget cluster = create(blockTargets, center, width, height);
                if (cluster != null)
                    clumps.add(cluster);
            } else if (clump.size() == 1) {
                MiraculousLadybugBlockTarget single = allBlocks.get(clump.getFirst());
                if (single != null) {
                    clumps.add(single);
                } else {
                    throw new IllegalStateException("Inside MiraculousLadybugBlockClusterTarget::reduceClumps a list clump contained a BlockPos that wasn't in allBlocks: " + clump.getFirst());
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
            double dist = b.getPosition().distanceToSqr(center);
            if (dist < bestDist) {
                bestDist = dist;
                start = b;
            }
        }

        if (start == null) {
            throw new IllegalStateException("Passed a list of MiraculousLadybugBlockTarget with null contents when calling MiraculousLadybugBlockCluster::create!");
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
