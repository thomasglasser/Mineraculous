package dev.thomasglasser.mineraculous.impl.world.entity;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.world.level.storage.MiraculousLadybugTargetData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class MiraculousLadybug extends PathfinderMob {
    public CatmullRom path;
    boolean shouldUpdatePath;
    public double t;

    public MiraculousLadybug(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
        this.shouldUpdatePath = true;
        this.t = 0;
    }

    @Override
    public void tick() {
        super.tick();
        MiraculousLadybugTargetData targetData = this.getData(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TARGET);
        //Level level = this.level();
        //if (level.isClientSide) return;
        if ((targetData.blockTargets() == null || targetData.blockTargets().isEmpty())) {
            this.discard();
            return;
        }
        List<BlockPos> blockTargets = sortBlockTargets(targetData.blockTargets(), this.blockPosition());
        if (this.shouldUpdatePath) { // this part should run only once in the entity's lifetime
            ArrayList<Vec3> targets = new ArrayList<>();
            for (BlockPos blockPos : blockTargets) {
                targets.add(blockPos.getCenter());
            }
            this.path = new CatmullRom(targets);
            this.t = path.getFirstParameter();
            this.shouldUpdatePath = false;
        } else {
            double speedPerTick = 0.8; // blocks per tick
            double arcSoFar = path.arcLength(t);
            double targetArc = arcSoFar + speedPerTick;

            t = path.findTForArcLength(targetArc, t);
            this.setPos(path.getPoint(t));

            Vec3 tangent = path.getTangent(t).normalize();
            double yaw = Math.toDegrees(Math.atan2(tangent.z, tangent.x)) - 90.0;
            double pitch = Math.toDegrees(-Math.atan2(tangent.y, Math.sqrt(tangent.x * tangent.x + tangent.z * tangent.z)));
            this.setYRot((float) yaw);
            this.setXRot((float) pitch);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1024)
                .add(Attributes.FLYING_SPEED, 0.1)
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(Attributes.FOLLOW_RANGE, 1024);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {}

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {}

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

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

    private static List<BlockPos> sortBlockTargets(List<BlockPos> blockTargets, BlockPos position) {
        List<BlockPos> toVisit = new ArrayList<>(blockTargets);
        List<BlockPos> ordered = new ArrayList<>();

        BlockPos current = new BlockPos(position);

        while (!toVisit.isEmpty()) {
            BlockPos nearest = null;
            double nearestDist = Double.MAX_VALUE;

            for (BlockPos target : toVisit) {
                double dist = current.distSqr(target);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = target;
                }
            }

            ordered.add(nearest);
            toVisit.remove(nearest);
            current = nearest;
        }
        ordered.add(0, new BlockPos(position));
        return ordered;
    }

    //TODO move to math utils
    public static class CatmullRom {
        private final List<Vec3> points;
        private final List<Double> T;
        private final List<Vec3> tangents;

        public CatmullRom(ArrayList<Vec3> targets) {
            // Add the ghost points
            ArrayList<Vec3> pts = new ArrayList<>(targets);
            pts.add(0, pts.get(0).subtract(pts.get(1)).add(pts.get(0)));
            int maxIndex = pts.size() - 1;
            pts.add(pts.get(maxIndex).subtract(pts.get(maxIndex - 1)).add(pts.get(maxIndex)));
            this.points = Collections.unmodifiableList(pts);

            // Uniform parameterization
            ArrayList<Double> tValues = new ArrayList<>(points.size());
            tValues.add(0d);
            for (int i = 1; i < points.size(); i++) {
                tValues.add(tValues.get(i - 1) + 1.0);
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
        public Vec3 getTangent(double t) {
            int index = findSegment(t);
            Vec3 P0 = points.get(index - 1);
            Vec3 v0 = tangents.get(index - 2);
            Vec3 P1 = points.get(index);
            Vec3 v1 = tangents.get(index - 1);
            double u = (t - T.get(index - 1)) / (T.get(index) - T.get(index - 1));
            return hermiteDerivative(u, P0, v0, P1, v1);
        }

        // Arc length [first, t] using Simpson’s rule
        public double arcLength(double t) {
            double a = getFirstParameter();
            double b = Math.max(a, Math.min(t, getLastParameter()));
            int steps = 32; // tune for accuracy vs cost
            double h = (b - a) / steps;
            double sum = 0.0;

            for (int i = 0; i <= steps; i++) {
                double x = a + i * h;
                double weight = (i == 0 || i == steps) ? 1 : (i % 2 == 0 ? 2 : 4);
                sum += weight * getTangent(x).length();
            }
            return sum * h / 3.0;
        }

        // Newton-Raphson: solve L(t) = s
        public double findTForArcLength(double s, double initialGuess) {
            double t = initialGuess;
            for (int i = 0; i < 3; i++) {
                double f = arcLength(t) - s;
                double fPrime = getTangent(t).length();
                if (fPrime < 1e-6) break; // avoid division by zero
                t -= f / fPrime;
                // clamp safely
                t = Math.max(getFirstParameter(), Math.min(t, getLastParameter()));
            }
            return t;
        }
    }
}
