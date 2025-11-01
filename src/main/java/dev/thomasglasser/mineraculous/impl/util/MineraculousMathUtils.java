package dev.thomasglasser.mineraculous.impl.util;

import com.google.common.collect.ImmutableList;
import dev.thomasglasser.mineraculous.impl.world.level.storage.MiraculousLadybugBlockTarget;
import dev.thomasglasser.mineraculous.impl.world.level.storage.MiraculousLadybugTarget;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;

public class MineraculousMathUtils {
    public static Vec3 projectOnCircle(Vec3 fromPointToCenter, Vec3 vec3) {
        Vec3 crossProd = fromPointToCenter.cross(vec3);
        Vec3 t = crossProd.cross(fromPointToCenter);

        double cosTheta = t.dot(vec3) / (t.length() * vec3.length());
        double tln = cosTheta * vec3.length();
        t = t.normalize().scale(tln);

        return t;
    }

    public static Vec3 getMovementVector(LivingEntity livingEntity, boolean... inputs) {
        boolean front = inputs[0];
        boolean back = inputs[1];
        boolean left = inputs[2];
        boolean right = inputs[3];
        Vec3 movement = new Vec3(0f, 0f, 0f);
        if (livingEntity != null) {
            double yawRad = Math.toRadians(livingEntity.getYRot());
            Vec3 frontMovement = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad)).normalize();
            Vec3 leftMovement = new Vec3(frontMovement.z, 0, -frontMovement.x).normalize();
            Vec3 backMovement = frontMovement.scale(-1).normalize();
            Vec3 rightMovement = leftMovement.scale(-1).normalize();

            if (front) movement = movement.add(frontMovement);
            if (back) movement = movement.add(backMovement);
            if (left) movement = movement.add(leftMovement);
            if (right) movement = movement.add(rightMovement);
            movement = movement.normalize();
        }
        return movement;
    }

    // generates the positions of n points on a circle, equally spaced between each other.
    public static ArrayList<Vector2d> generateCirclePoints(double rad, int n) {
        ArrayList<Vector2d> points = new ArrayList<>();
        double angleStep = 2 * Math.PI / n;

        for (int i = 0; i < n; i++) {
            double angle = i * angleStep;
            double x = rad * Math.cos(angle);
            double y = rad * Math.sin(angle);
            points.add(new Vector2d(x, y));
        }

        return points;
    }

    public static Vec3i getVec3i(Vec3 vec) {
        return new Vec3i((int) vec.x, (int) vec.y, (int) vec.z);
    }

    public static void spawnBlockParticles(ServerLevel level, BlockPos pos, SimpleParticleType type, int particleCount) {
        Vec3 center = pos.getCenter();
        double startX = center.x;
        double startY = center.y;
        double startZ = center.z;

        level.sendParticles(
                type,
                startX,
                startY,
                startZ,
                particleCount,
                0.2,
                0.2,
                0.2,
                0.2);
    }

    //TODO check kwami implementation and if u can replace their spin with this.

    /**
     * angleStep is represented in radians
     * yStep MUST be >= 0
     * height and width MUST be > 0
     **/
    public static List<Vec3> spinAround(Vec3 position, double width, double height, double angleStep, double yStep) {
        ArrayList<Vec3> toReturn = new ArrayList<>();
        double radius = width;
        double y = 0;
        double alpha = 0;
        while (y <= height) {
            double x = Math.sin(alpha) * radius;
            double z = Math.cos(alpha) * radius;
            Vec3 point = new Vec3(x, y, z);
            toReturn.add(position.add(point));

            y += yStep;
            alpha += angleStep;
        }
        return toReturn;
    }

    public static BlockPos findNearestBlockPos(Vec3 pos, Iterable<BlockPos> candidates) {
        BlockPos best = null;
        double bestDistSq = Double.POSITIVE_INFINITY;
        for (BlockPos c : candidates) {
            double dx = pos.x - (c.getX() + 0.5);
            double dy = pos.y - (c.getY() + 0.5);
            double dz = pos.z - (c.getZ() + 0.5);
            double d2 = dx * dx + dy * dy + dz * dz;
            if (d2 < bestDistSq) {
                bestDistSq = d2;
                best = c;
            }
        }
        return best;
    }

    public static List<List<BlockPos>> buildRevertLayers(BlockPos origin, java.util.Set<BlockPos> validBlocks) {
        List<List<BlockPos>> layers = new ArrayList<>();
        java.util.Set<BlockPos> remaining = new java.util.HashSet<>(validBlocks);
        if (!remaining.contains(origin)) {
            return layers;
        }

        List<BlockPos> current = new ArrayList<>();
        current.add(origin);
        remaining.remove(origin);
        layers.add(ImmutableList.copyOf(current));

        List<int[]> offsets = new ArrayList<>(26);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    offsets.add(new int[] { dx, dy, dz });
                }
            }
        }

        while (true) {
            List<BlockPos> nextLayer = new ArrayList<>();
            for (BlockPos bp : current) {
                for (int[] off : offsets) {
                    BlockPos n = bp.offset(off[0], off[1], off[2]);
                    if (remaining.contains(n)) {
                        nextLayer.add(n);
                        remaining.remove(n);
                    }
                }
            }
            if (nextLayer.isEmpty()) break;
            layers.add(ImmutableList.copyOf(nextLayer));
            current = nextLayer;
        }
        return layers;
    }

    //ALGORITHMS
    //ITERATIVE FILL
    public static Collection<MiraculousLadybugBlockTarget> reduceNearbyBlocks(Collection<MiraculousLadybugBlockTarget> input) {
        // Flatten input into one big map of BlockPos -> UUID
        Map<BlockPos, UUID> allBlocks = new HashMap<>();
        for (var bt : input) {
            for (var entry : bt.blocksToRevert().entrySet()) {
                allBlocks.put(entry.getKey(), entry.getValue());
            }
        }

        Set<BlockPos> unvisited = new HashSet<>(allBlocks.keySet());
        List<MiraculousLadybugBlockTarget> result = new ArrayList<>();

        while (!unvisited.isEmpty()) {
            BlockPos start = unvisited.iterator().next();
            Map<BlockPos, UUID> clump = new HashMap<>();

            Queue<BlockPos> queue = new LinkedList<>();
            queue.add(start);
            unvisited.remove(start);

            while (!queue.isEmpty()) {
                BlockPos current = queue.poll();
                clump.put(current, allBlocks.get(current));

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) continue;
                            BlockPos neighbor = current.offset(dx, dy, dz);
                            if (unvisited.remove(neighbor)) {
                                queue.add(neighbor);
                            }
                        }
                    }
                }
            }

            double avgX = 0, avgY = 0, avgZ = 0;
            for (BlockPos pos : clump.keySet()) {
                avgX += pos.getX() + 0.5;
                avgY += pos.getY() + 0.5;
                avgZ += pos.getZ() + 0.5;
            }
            int size = clump.size();
            Vec3 center = new Vec3(avgX / size, avgY / size, avgZ / size);

            result.add(new MiraculousLadybugBlockTarget(center, clump));
        }

        return result;
    }

    //GREEDY TSP
    public static List<MiraculousLadybugTarget> sortTargets(Collection<MiraculousLadybugTarget> targets, MiraculousLadybugTarget startTarget) {
        List<MiraculousLadybugTarget> toVisit = new ArrayList<>(targets);
        List<MiraculousLadybugTarget> ordered = new ArrayList<>();

        Vec3 current = startTarget.position();

        while (!toVisit.isEmpty()) {
            MiraculousLadybugTarget nearest = null;
            double nearestDist = Double.MAX_VALUE;

            for (MiraculousLadybugTarget target : toVisit) {
                double dist = current.distanceTo(target.position());
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = target;
                }
            }

            ordered.add(nearest);
            toVisit.remove(nearest);
            if (nearest != null)
                current = nearest.position();
        }
        return ordered;
    }

    public static class CatmullRom {
        private final List<Vec3> points;
        private final List<Double> T;
        private final List<Vec3> tangents;

        public CatmullRom(List<Vec3> targets) {
            this(new ArrayList<>(targets));
        }

        public CatmullRom(ArrayList<Vec3> targets) {
            // Add the ghost points
            ArrayList<Vec3> pts = new ArrayList<>(targets);
            pts.add(0, pts.get(0).subtract(pts.get(1)).add(pts.get(0))); //adds the ghost point
            int maxIndex = pts.size() - 1;
            pts.add(pts.get(maxIndex).subtract(pts.get(maxIndex - 1)).add(pts.get(maxIndex)));
            this.points = Collections.unmodifiableList(pts);

            // Centripetal parameterization
            ArrayList<Double> tValues = new ArrayList<>(points.size());
            tValues.add(0d);
            for (int i = 1; i < points.size(); i++) {
                double dist = points.get(i).distanceTo(points.get(i - 1));
                tValues.add(tValues.get(i - 1) + Math.sqrt(Math.max(dist, 1e-9)));
            }
            this.T = Collections.unmodifiableList(tValues);

            // Tangents
            ArrayList<Vec3> tans = new ArrayList<>(points.size() - 2);
            for (int i = 1; i < points.size() - 1; i++) {
                Vec3 mi = points.get(i + 1).subtract(points.get(i - 1))
                        .scale(1.0 / (T.get(i + 1) - T.get(i - 1)));
                tans.add(mi);
            }
            this.tangents = Collections.unmodifiableList(tans);
        }

        public double getFirstParameter() {
            return T.get(1); // skip ghost point
        }

        public double getLastParameter() {
            return T.get(T.size() - 2); // skip ghost point
        }

        // Hermite blend
        private Vec3 hermite(double u, Vec3 P0, Vec3 v0, Vec3 P1, Vec3 v1) {
            double h00 = 1 - 3 * u * u + 2 * u * u * u;
            double h01 = u - 2 * u * u + u * u * u;
            double h10 = 3 * u * u - 2 * u * u * u;
            double h11 = -u * u + u * u * u;
            return P0.scale(h00).add(v0.scale(h01)).add(P1.scale(h10)).add(v1.scale(h11));
        }

        // Hermite derivative
        private Vec3 hermiteDerivative(double u, Vec3 P0, Vec3 v0, Vec3 P1, Vec3 v1) {
            double dh00 = -6 * u + 6 * u * u;
            double dh01 = 1 - 4 * u + 3 * u * u;
            double dh10 = 6 * u - 6 * u * u;
            double dh11 = -2 * u + 3 * u * u;
            return P0.scale(dh00).add(v0.scale(dh01)).add(P1.scale(dh10)).add(v1.scale(dh11));
        }

        // Find which segment t belongs to
        public int findSegment(double t) {
            // clamp t to valid range
            t = Math.max(getFirstParameter(), Math.min(t, getLastParameter() - 1e-9));
            for (int i = 1; i < T.size(); i++) {
                if (T.get(i) > t) {
                    return i;
                }
            }
            return T.size() - 2; // fallback to last segment
        }

        // Evaluate point
        public Vec3 getPoint(double t) {
            int index = findSegment(t);
            Vec3 P0 = points.get(index - 1);
            Vec3 v0 = tangents.get(index - 2);
            Vec3 P1 = points.get(index);
            Vec3 v1 = tangents.get(index - 1);
            double u = (t - T.get(index - 1)) / (T.get(index) - T.get(index - 1));
            return hermite(u, P0, v0, P1, v1);
        }

        // Evaluate derivative
        public Vec3 getDerivative(double t) {
            int index = findSegment(t);
            Vec3 P0 = points.get(index - 1);
            Vec3 v0 = tangents.get(index - 2);
            Vec3 P1 = points.get(index);
            Vec3 v1 = tangents.get(index - 1);
            double u = (t - T.get(index - 1)) / (T.get(index) - T.get(index - 1));

            // derivative returned by hermiteDerivative is dP/du
            Vec3 dPdu = hermiteDerivative(u, P0, v0, P1, v1);

            // convert to dP/dt using chain rule: dP/dt = dP/du * du/dt
            double du_dt = 1.0 / (T.get(index) - T.get(index - 1));
            return dPdu.scale(du_dt);
        }

        public double advanceParameter(double t, double speed) {
            Vec3 deriv = getDerivative(t);
            double denom = deriv.length();
            if (denom < 1e-8) return t;
            double dsdt = speed / denom;
            return Math.min(getLastParameter(), t + dsdt);
        }

        public double getParameterBehind(double t, double distance) {
            double step = -0.01; // step backward
            double traveled = 0.0;
            Vec3 prev = getPoint(t);
            double curT = t;

            while (curT > getFirstParameter() && traveled < distance) {
                curT += step;
                Vec3 pos = getPoint(curT);
                traveled += pos.distanceTo(prev);
                prev = pos;
            }

            return Math.max(getFirstParameter(), curT);
        }
    }
}
