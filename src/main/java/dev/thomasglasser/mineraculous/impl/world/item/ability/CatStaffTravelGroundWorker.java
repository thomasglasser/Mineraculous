package dev.thomasglasser.mineraculous.impl.world.item.ability;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.impl.world.level.storage.TravelingCatStaffData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class CatStaffTravelGroundWorker {
    // Method for damage calculating
    private static final double LAUNCHING_STRENGTH = 5;

    public static Vec3 expectedStaffTip(Entity user, TravelingCatStaffData data) {
        return expectedStaffTip(user, data.initialUserHorizontalDirection());
    }

    public static Vec3 expectedStaffTip(Entity user, Vec3 horizontalDirection) {
        return user.position().add(0, user.getBbHeight() / 2d, 0).add(horizontalDirection);
    }

    public static Vec3 expectedStaffTip(Entity user, TravelingCatStaffData data, float partialTick) {
        Vec3 horizontalDirection = data.initialUserHorizontalDirection();
        Vec3 userPos = user.getPosition(partialTick);
        return userPos.add(0, user.getBbHeight() / 2d, 0).add(horizontalDirection.normalize());
    }

    protected static void activateModeOrHelicopter(Level level, LivingEntity user, ItemStack stack) {
        if (!level.isClientSide()) {
            Vec2 horizontalFacing = MineraculousMathUtils.getHorizontalFacingVector(user.getYRot());
            Vec3 horizontalDirection = new Vec3(horizontalFacing.x, 0, horizontalFacing.y);
            Vec3 lookAngle = user.getLookAngle().normalize();
            Vec3 staffTip = expectedStaffTip(user, horizontalDirection);
            Vec3 staffOrigin = staffTip.add(lookAngle.scale(CatStaffItem.getMinStaffLength(user)));

            HitResult raycast = raycast(level, staffOrigin, staffTip.subtract(staffOrigin).normalize(), MineraculousServerConfig.get().maxToolLength.get());
            if (raycast.getType() == HitResult.Type.BLOCK) {
                activateMode(user, stack, lookAngle, staffTip, staffOrigin, horizontalDirection);
            } else {
                startHelicopter(user);
            }
        }
    }

    protected static void makeStaffRetract(Level level, LivingEntity user, TravelingCatStaffData data) {
        if (!level.isClientSide()) {
            data.withAnchored(false).withRetracting(true).save(user);
        }
    }

    protected static void stopStaffRetraction(Level level, LivingEntity user, TravelingCatStaffData data) {
        if (!level.isClientSide()) {
            data.withRetracting(false).withAnchored(false).save(user);
        }
    }

    protected static TravelingCatStaffData launchUser(Entity user, TravelingCatStaffData data) {
        Vec3 direction = data.launchingDirection();
        user.hurtMarked = true;
        user.setDeltaMovement(direction.scale(LAUNCHING_STRENGTH));
        return data.withSafeFallTick(100);
    }

    protected static TravelingCatStaffData updateSafeFallTicks(Entity user, TravelingCatStaffData data) {
        int tick = data.safeFallTick();
        if (tick > 0) {
            user.resetFallDistance();
            return data.withSafeFallTick(tick - 1);
        }
        return data;
    }

    protected static TravelingCatStaffData decreaseStaffLength(Entity user, TravelingCatStaffData data) {
        Vec3 origin = data.staffOrigin();
        Vec3 tip = data.staffTip();
        Vec3 originToTip = tip.subtract(origin);
        double minLength = CatStaffItem.getMinStaffLength(user);
        Vec3 newOrigin = origin.add(originToTip.normalize().scale(CatStaffItem.STAFF_GROWTH_SPEED / 4d));
        if (tip.subtract(newOrigin).length() < minLength) {
            newOrigin = origin.add(originToTip.normalize().scale(minLength));
            data = data.withEnabled(false);
        }
        return data.withStaffOrigin(newOrigin);
    }

    protected static TravelingCatStaffData increaseStaffLength(Level level, TravelingCatStaffData data) {
        Vec3 origin = data.staffOrigin();
        Vec3 tip = data.staffTip();
        Vec3 tipToOrigin = origin.subtract(tip).normalize();
        HitResult raycast = raycast(level, origin, tipToOrigin, CatStaffItem.STAFF_GROWTH_SPEED);
        Vec3 newOrigin = raycast.getLocation();
        double maxLength = MineraculousServerConfig.get().maxToolLength.get();
        double newLength = newOrigin.subtract(tip).length();
        if (newLength > maxLength) {
            newOrigin = tip.add(tipToOrigin.scale(maxLength));
        } else {
            if (raycast.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = BlockPos.containing(newOrigin);
                BlockState state = level.getBlockState(pos);
                if (!state.getCollisionShape(level, pos).isEmpty()) {
                    data = data.withAnchored(true);
                }
            }
        }
        return data.withStaffOrigin(newOrigin);
    }

    protected static TravelingCatStaffData updateStaffExtremities(Entity user, TravelingCatStaffData data) {
        Vec3 oldTip = data.staffTip();
        Vec3 oldOrigin = data.staffOrigin();
        Vec3 tipToOrigin = oldOrigin.subtract(oldTip);
        Vec3 updatedTip = expectedStaffTip(user, data);
        Vec3 updatedOrigin = updatedTip.add(tipToOrigin);
        return data.withStaffTip(updatedTip).withStaffOrigin(updatedOrigin);
    }

    protected static HitResult raycast(Level level, Vec3 origin, Vec3 direction, double length) {
        return level.clip(
                new ClipContext(
                        origin,
                        origin.add(direction.scale(length)),
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        CollisionContext.empty()));
    }

    private static void activateMode(Entity user, ItemStack stack, Vec3 lookAngle, Vec3 staffTip, Vec3 staffOrigin, Vec3 horizontalDirection) {
        new TravelingCatStaffData(
                stack,
                true,
                lookAngle,
                staffTip,
                staffOrigin,
                horizontalDirection,
                false,
                false,
                false,
                0)
                        .save(user);
    }

    private static void startHelicopter(Entity user) {
        TravelingCatStaffData data = user.getData(MineraculousAttachmentTypes.TRAVELING_CAT_STAFF);
        data.withEnabled(false).withHelicopter(true).save(user);
    }
}
