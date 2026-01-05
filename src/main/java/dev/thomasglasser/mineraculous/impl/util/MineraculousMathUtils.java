package dev.thomasglasser.mineraculous.impl.util;

import com.google.common.collect.ImmutableList;
import dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget.MiraculousLadybugTarget;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;

public class MineraculousMathUtils {
    public static Vec3 rotateYaw(Vec3 vec3, double yaw) {
        return new Vec3(vec3.x * Math.cos(yaw) - vec3.z * Math.sin(yaw), vec3.y, vec3.x * Math.sin(yaw) + vec3.z * Math.cos(yaw));
    }

    public static Vec3 rotatePitch(Vec3 vec3, double pitch) {
        return new Vec3(vec3.x, vec3.y * Math.cos(pitch) + vec3.z * Math.sin(pitch), vec3.z * Math.cos(pitch) - vec3.y * Math.sin(pitch));
    }

    public static Vec3 projectOnCircle(Vec3 fromPointToCenter, Vec3 vec3) {
        Vec3 crossProd = fromPointToCenter.cross(vec3);
        Vec3 t = crossProd.cross(fromPointToCenter);

        double cosTheta = t.dot(vec3) / (t.length() * vec3.length());
        double tln = cosTheta * vec3.length();
        t = t.normalize().scale(tln);

        return t;
    }

    public static Vec2 getHorizontalFacingVector(float yaw) {
        while (yaw < 0.0f) {
            yaw += 360.0f;
        }
        while (yaw >= 360.0f) {
            yaw -= 360.0f;
        }
        float cos = (float) Math.cos(Math.toRadians(yaw));
        float sin = (float) -Math.sin(Math.toRadians(yaw));
        return new Vec2(sin, cos).normalized();
    }

    public static Vec3 getMovementVector(double yaw, boolean... inputs) {
        boolean front = inputs[0];
        boolean back = inputs[1];
        boolean left = inputs[2];
        boolean right = inputs[3];
        Vec3 movement = new Vec3(0f, 0f, 0f);
        double yawRad = Math.toRadians(yaw);
        Vec3 frontMovement = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad)).normalize();
        Vec3 leftMovement = new Vec3(frontMovement.z, 0, -frontMovement.x).normalize();
        Vec3 backMovement = frontMovement.scale(-1).normalize();
        Vec3 rightMovement = leftMovement.scale(-1).normalize();

        if (front) movement = movement.add(frontMovement);
        if (back) movement = movement.add(backMovement);
        if (left) movement = movement.add(leftMovement);
        if (right) movement = movement.add(rightMovement);
        movement = movement.normalize();
        return movement;
    }

    // generates the positions of n points on a circle, equally spaced between each other.
    public static List<Vector2d> generateCirclePoints(double rad, int n) {
        ImmutableList.Builder<Vector2d> points = new ImmutableList.Builder<>();
        double angleStep = 2 * Math.PI / n;

        for (int i = 0; i < n; i++) {
            double angle = i * angleStep;
            double x = rad * Math.cos(angle);
            double y = rad * Math.sin(angle);
            points.add(new Vector2d(x, y));
        }

        return points.build();
    }

    /**
     * Generates a spiral (cylindrical or conical) path around a center point.
     *
     * @param center     the center position
     * @param baseRadius radius at the bottom
     * @param topRadius  radius at the top (same as baseRadius for a cylinder)
     * @param height     total height of the spiral
     * @param angleStep  rotation step in radians
     * @param yStep      vertical step per point
     * @return list of Vec3 points along the spiral
     */
    public static List<Vec3> spinAround(
            Vec3 center,
            double baseRadius,
            double topRadius,
            double height,
            double angleStep,
            double yStep) {
        List<Vec3> points = new ArrayList<>();
        double alpha = 0;
        double y = 0;

        while (y <= height) {
            double t = y / height;
            double radius = baseRadius + (topRadius - baseRadius) * t;

            double x = Math.cos(alpha) * radius;
            double z = Math.sin(alpha) * radius;

            points.add(center.add(x, y, z));
            alpha += angleStep;
            y += yStep;
        }

        return points;
    }

    //ALGORITHMS
    //GREEDY TSP
    public static List<MiraculousLadybugTarget<?>> sortTargets(Collection<MiraculousLadybugTarget<?>> targets, Vec3 startTarget) {
        List<MiraculousLadybugTarget<?>> toVisit = new ArrayList<>(targets);
        List<MiraculousLadybugTarget<?>> ordered = new ArrayList<>();

        Vec3 current = startTarget;

        while (!toVisit.isEmpty()) {
            MiraculousLadybugTarget<?> nearest = null;
            double nearestDist = Double.MAX_VALUE;

            for (MiraculousLadybugTarget<?> target : toVisit) {
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
        private final List<Double> tParameters;
        private final List<Vec3> tangents;

        public CatmullRom(List<Vec3> targets) {
            this(new ArrayList<>(targets));
        }

        public CatmullRom(ArrayList<Vec3> targets) {
            // Add the ghost points
            ArrayList<Vec3> pts = new ArrayList<>(targets);
            pts.addFirst(pts.get(0).subtract(pts.get(1)).add(pts.get(0))); //adds the ghost point
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
            this.tParameters = Collections.unmodifiableList(tValues);

            // Tangents
            ArrayList<Vec3> tans = new ArrayList<>(points.size() - 2);
            for (int i = 1; i < points.size() - 1; i++) {
                Vec3 mi = points.get(i + 1).subtract(points.get(i - 1))
                        .scale(1.0 / (tParameters.get(i + 1) - tParameters.get(i - 1)));
                tans.add(mi);
            }
            this.tangents = Collections.unmodifiableList(tans);
        }

        public double getFirstParameter() {
            return tParameters.get(1); // skip ghost point
        }

        public double getLastParameter() {
            return tParameters.get(tParameters.size() - 2); // skip ghost point
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
            for (int i = 1; i < tParameters.size(); i++) {
                if (tParameters.get(i) > t) {
                    return i;
                }
            }
            return tParameters.size() - 2; // fallback to last segment
        }

        // Evaluate point
        public Vec3 getPoint(double t) {
            int index = findSegment(t);
            Vec3 P0 = points.get(index - 1);
            Vec3 v0 = tangents.get(index - 2);
            Vec3 P1 = points.get(index);
            Vec3 v1 = tangents.get(index - 1);
            double u = (t - tParameters.get(index - 1)) / (tParameters.get(index) - tParameters.get(index - 1));
            return hermite(u, P0, v0, P1, v1);
        }

        // Evaluate derivative
        public Vec3 getDerivative(double t) {
            int index = findSegment(t);
            Vec3 P0 = points.get(index - 1);
            Vec3 v0 = tangents.get(index - 2);
            Vec3 P1 = points.get(index);
            Vec3 v1 = tangents.get(index - 1);
            double u = (t - tParameters.get(index - 1)) / (tParameters.get(index) - tParameters.get(index - 1));

            // derivative returned by hermiteDerivative is dP/du
            Vec3 dPdu = hermiteDerivative(u, P0, v0, P1, v1);

            // convert to dP/dt using chain rule: dP/dt = dP/du * du/dt
            double du_dt = 1.0 / (tParameters.get(index) - tParameters.get(index - 1));
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
