package dev.thomasglasser.mineraculous.impl.world.entity;

import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.world.level.storage.MiraculousLadybugTargetData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import net.minecraft.commands.arguments.EntityAnchorArgument;
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
    public double length = 1;

    public MiraculousLadybug(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    @Override
    public void tick() {
        super.tick();
        MiraculousLadybugTargetData targetData = this.getData(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TARGET);
        Level level = this.level();
        if ((targetData.blockTargets() == null || targetData.blockTargets().isEmpty())
                && targetData.currentTarget().isEmpty()) {
            this.discard();
            return;
        }
        List<BlockPos> blockTargets = new ArrayList<>(targetData.blockTargets());
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
            if (target != null) {
                double distance = target.distanceTo(this.position());
                this.length = distance < 20 ? 2 / distance : 1;
            }
        } else {
            if (target == null && !blockTargets.isEmpty()) {
                Vec3 nextTarget = blockTargets.remove(0).getCenter();
                targetData = new MiraculousLadybugTargetData(Optional.of(nextTarget), blockTargets);
                targetData.save(this, true);
            } else if (target != null) {
                Vec3 diff = target.subtract(this.position());
                double distance = diff.length();
                if (distance > 3) {
                    this.setDeltaMovement(diff.normalize());
                    this.hurtMarked = true;
                }
                if (distance < 4) {
                    targetData = new MiraculousLadybugTargetData(Optional.empty(), blockTargets);
                    targetData.save(this, true);
                }
            }
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
}
