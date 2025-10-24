package dev.thomasglasser.mineraculous.impl.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

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

    public static ArrayList<Vec3> getCenter(List<BlockPos> blockPos) {
        ArrayList<Vec3> toReturn = new ArrayList<>();
        for (BlockPos pos : blockPos)
            toReturn.add(pos.getCenter());
        return toReturn;
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

    //ALGORITHMS
    //ITERATIVE FILL
    public static List<BlockPos> reduceNearbyBlocks(List<BlockPos> positions) {
        List<BlockPos> result = new ArrayList<>();
        Set<BlockPos> unvisited = new HashSet<>(positions);

        while (!unvisited.isEmpty()) {
            BlockPos start = unvisited.iterator().next();
            result.add(start);

            Queue<BlockPos> queue = new LinkedList<>();
            queue.add(start);
            unvisited.remove(start);

            while (!queue.isEmpty()) {
                BlockPos current = queue.poll();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx != 0 || dy != 0 || dz != 0) {
                                BlockPos neighbor = new BlockPos(
                                        current.getX() + dx,
                                        current.getY() + dy,
                                        current.getZ() + dz);

                                if (unvisited.contains(neighbor)) {
                                    queue.add(neighbor);
                                    unvisited.remove(neighbor);
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    public static Multimap<ResourceKey<Level>, BlockPos> reduceNearbyBlocks(Multimap<ResourceKey<Level>, BlockPos> blockPositions) {
        Multimap<ResourceKey<Level>, BlockPos> reducedBlockPositions = ArrayListMultimap.create();
        for (ResourceKey<Level> levelKey : blockPositions.keySet()) {
            Collection<BlockPos> positions = blockPositions.get(levelKey);
            List<BlockPos> reduced = MineraculousMathUtils.reduceNearbyBlocks(new ArrayList<>(positions));
            reducedBlockPositions.putAll(levelKey, reduced);
        }
        return reducedBlockPositions;
    }

    //GREEDY TSP
    public static List<Vec3> sortTargets(List<Vec3> targets) {
        if (targets.isEmpty()) return List.of();
        return sortTargets(targets, targets.getFirst());
    }

    public static List<Vec3> sortTargets(List<Vec3> targets, Vec3 position) {
        List<Vec3> toVisit = new ArrayList<>(targets);
        List<Vec3> ordered = new ArrayList<>();

        Vec3 current = new Vec3(position.toVector3f());

        while (!toVisit.isEmpty()) {
            Vec3 nearest = null;
            double nearestDist = Double.MAX_VALUE;

            for (Vec3 target : toVisit) {
                double dist = current.distanceTo(target);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = target;
                }
            }

            ordered.add(nearest);
            toVisit.remove(nearest);
            current = nearest;
        }
        return ordered;
    }

    /**
     * works only if A, B, C are collinear (on the same straight line)
     * returns true if A is between C and B
     * C ---- A ---- B or B ---- A ---- C
     */
    public static boolean isBetween(Vec3 A, Vec3 B, Vec3 C) {
        Vec3 ab = A.subtract(B);
        Vec3 cb = C.subtract(B);

        double dot = ab.dot(cb);
        return dot > 0 && dot < cb.lengthSqr();
    }

    public static class CatmullRom {
        private final List<Vec3> points;
        private final List<Double> T;
        private final List<Vec3> tangents;

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
                // centripetal spacing — sqrt of distance
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
        private int findSegment(double t) {
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
