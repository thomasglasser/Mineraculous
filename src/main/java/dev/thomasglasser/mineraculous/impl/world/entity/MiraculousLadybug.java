package dev.thomasglasser.mineraculous.impl.world.entity;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.world.level.storage.MiraculousLadybugTargetData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
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

public class MiraculousLadybug extends PathfinderMob {
    CatmullRom path;
    boolean shouldUpdatePath;
    double t;

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
        Level level = this.level();
        if (level.isClientSide) return;
        if ((targetData.blockTargets() == null || targetData.blockTargets().isEmpty())) {
            this.discard();
            return;
        }
        List<BlockPos> blockTargets = sortBlockTargets(targetData.blockTargets(), this.blockPosition());
        if (this.shouldUpdatePath) { //this part should run only once in the entity's lifetime
            ArrayList<Vec3> targets = new ArrayList<>();
            for (BlockPos blockPos : blockTargets) {
                targets.add(blockPos.getCenter());
            }
            this.path = new CatmullRom(targets);
            this.t = path.T.get(1);
            this.shouldUpdatePath = false;
        } else if (t >= path.T.get(1)) { //0.01
            this.setPos(path.getPoint(t));
            t = Math.min(t + 0.1, path.getLastParameter());
        }

        /*
        Vec3 target = targetData.currentTarget().orElse(null);
        
        Vec3 lookAngle = this.getLookAngle().normalize();
        if (target != null) {
            this.lookAt(EntityAnchorArgument.Anchor.EYES, target);
        }
        if (level().isClientSide) {
            for (int i = 1; i <= 3; i++)
                level.addParticle(
                        MineraculousParticleTypes.STARLIGHT.get(),
                        this.getX() + lookAngle.x + Math.random() * 5 - 2.5,
                        this.getY() + lookAngle.y + Math.random() * 5 - 2.5,
                        this.getZ() + lookAngle.z + Math.random() * 5 - 2.5,
                        0, 0, 0);
        } else {
            if (target == null && !blockTargets.isEmpty()) {
                Vec3 nextTarget = blockTargets.remove(0).getCenter();
                targetData = new MiraculousLadybugTargetData(Optional.of(nextTarget), blockTargets, targetData.sphereTicks());
                targetData.save(this, true);
            } else if (target != null) {
                Vec3 diff = target.subtract(this.position());
                double distance = diff.length();
                if (distance > 1) {
                    this.setDeltaMovement(diff.normalize());
                    this.hurtMarked = true;
                }
                if (distance <= 4) {
                    targetData = new MiraculousLadybugTargetData(targetData.currentTarget(), blockTargets, 30);
                    targetData.save(this, true);
                }
                if (distance <= 1) {
                    targetData = new MiraculousLadybugTargetData(Optional.empty(), blockTargets, targetData.sphereTicks());
                    targetData.save(this, true);
                }
            }
        
            this.getData(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TARGET).tick(this, true);
        }*/
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

    private static class CatmullRom {
        private final List<Vec3> points;
        private final List<Double> T;
        private final List<Vec3> tangents;

        public CatmullRom(ArrayList<Vec3> targets) {
            // Add the ghost points
            ArrayList<Vec3> points = new ArrayList<>(targets);
            points.add(0, points.get(0).subtract(points.get(1)).add(points.get(0)));
            int maxIndex = points.size() - 1;
            points.add(points.get(maxIndex).subtract(points.get(maxIndex - 1)).add(points.get(maxIndex)));
            this.points = Collections.unmodifiableList(points);

            // Get t args ready
            ArrayList<Double> T = new ArrayList<>(points.size()); // values will be positive and can be bigger than 1
            List<Vec3> P = this.points;
            T.add(0d); //T0 = 0
            for (int i = 1; i < points.size(); i++) {
                T.add(T.get(i - 1) + Math.pow(P.get(i).subtract(P.get(i - 1)).length(), 0));
            }
            this.T = Collections.unmodifiableList(T);

            ArrayList<Vec3> tangents = new ArrayList<>(P.size() - 2);
            for (int i = 1; i < P.size() - 1; i++) {
                Vec3 mi = P.get(i + 1).subtract(P.get(i - 1)).scale(1 / (T.get(i + 1) - T.get(i - 1)));
                tangents.add(mi);
            }
            this.tangents = Collections.unmodifiableList(tangents);
        }

        public double getLastParameter() {
            return T.get(T.size() - 2);
        }

        public Vec3 getPointWithProgress(double progress) { //progress must be inside [0, 1]
            double t = T.get(1) + (T.get(T.size() - 2) - T.get(1)) * progress;
            return getPoint(t);
        }

        public Vec3 getPoint(double t) {
            t = t == T.get(T.size() - 2) ? T.get(T.size() - 2) - 0.00001d : t;
            int index = 2;
            for (int i = 1; i < T.size(); i++) {
                if (T.get(i) > t) {
                    index = i;
                    break;
                }
            }
            Vec3 P0 = points.get(index - 1);
            Vec3 v0 = tangents.get(index - 2);
            Vec3 P1 = points.get(index);
            Vec3 v1 = tangents.get(index - 1);
            double u = (t - T.get(index - 1)) / (T.get(index) - T.get(index - 1));
            double h00 = 1 - 3 * u * u + 2 * u * u * u;
            double h01 = u - 2 * u * u + u * u * u;
            double h10 = 3 * u * u - 2 * u * u * u;
            double h11 = -u * u + u * u * u;
            Vec3 P = P0.scale(h00).add(
                    v0.scale(h01).add(
                            P1.scale(h10).add(
                                    v1.scale(h11))));
            return P;
        }
    }
}
