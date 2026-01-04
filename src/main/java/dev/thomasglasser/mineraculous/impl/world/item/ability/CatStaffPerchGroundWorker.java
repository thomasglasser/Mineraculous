package dev.thomasglasser.mineraculous.impl.world.item.ability;

import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.impl.world.level.storage.newPerchingCatStaffData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class CatStaffPerchGroundWorker {
    private static final double LAUNCHING_USER_STRENGTH = 2.0;

    protected static void activateMode(Level level, LivingEntity user) {
        if (!level.isClientSide()) {
            setUserLaunchingData(user);
        }
        launchUser(user);
    }

    protected static void makeUserReleaseStaff(Entity user, newPerchingCatStaffData data) {
        data
                .withState(newPerchingCatStaffData.PerchingState.RELEASE)
                .withGravity(true)
                .save(user);
    }

    protected static newPerchingCatStaffData updateGravity(Entity user, newPerchingCatStaffData data) {
        boolean stateHasGravity = data.perchingStateHasGravity();
        user.setNoGravity(!data.userGravity());
        return data.withGravity(stateHasGravity);
    }

    protected static newPerchingCatStaffData updateStateAndLength(Level level, Entity user, newPerchingCatStaffData data) {
        data = updateState(user, data);
        data = CatStaffPerchGroundWorker.updateLength(level, user, data);
        return data;
    }

    protected static void setUserVerticalPosition(Entity user, newPerchingCatStaffData data) {
        if (user.isNoGravity()) {
            double verticalPosition = expectedUserY(user, data);
            double positionCorrection = verticalPosition - user.getY();
            Vec3 movement = new Vec3(0, positionCorrection, 0);
            user.move(MoverType.SELF, movement);
            user.hurtMarked = true;
        }
    }

    /**
     * This method constrains the user movement to a circle.
     *
     * @param user The constrained movement entity.
     * @param data The user's perching data.
     */
    protected static void constrainUserPosition(Entity user, newPerchingCatStaffData data) {
        Vec3 userToStaff = userToStaff(user, data);
        double distance = userToStaff.length();
        Vec3 constrain = userToStaff
                .normalize()
                .scale(distance - CatStaffItem.DISTANCE_BETWEEN_STAFF_AND_USER_IN_BLOCKS);
        user.move(MoverType.SELF, constrain);
    }

    protected static Vec3 userToStaff(Entity user, newPerchingCatStaffData data) {
        Vec3 staffHorizontalPosition = data.horizontalPosition();
        Vec3 userHorizontalPosition = new Vec3(user.getX(), 0, user.getZ());
        return staffHorizontalPosition.subtract(userHorizontalPosition);
    }

    private static void launchUser(Entity user) {
        user.hurtMarked = true;
        user.setDeltaMovement(new Vec3(0, LAUNCHING_USER_STRENGTH, 0));
    }

    private static void setUserLaunchingData(LivingEntity user) {
        Vec3 staffTipStartup = staffTipStartup(user);
        Vec3 staffOriginStartup = staffOriginStartup(user, staffTipStartup);
        createLaunchingData(user, staffOriginStartup, staffTipStartup).save(user);
    }

    private static newPerchingCatStaffData createLaunchingData(LivingEntity user, Vec3 staffOrigin, Vec3 staffTip) {
        return new newPerchingCatStaffData(
                newPerchingCatStaffData.PerchingState.LAUNCH,
                newPerchingCatStaffData.VerticalMovement.NEUTRAL,
                Direction.fromYRot(user.yHeadRot),
                user.position(),
                staffOrigin,
                staffTip,
                true,
                false,
                true);
    }

    private static Vec3 staffTipStartup(Entity user) {
        Vec3 userPosition = user.position();
        Vec2 horizontalFacing = MineraculousMathUtils.getHorizontalFacingVector(user);
        double userHeight = user.getBbHeight();
        return new Vec3(
                userPosition.x + horizontalFacing.x,
                userPosition.y + userHeight + CatStaffItem.STAFF_HEAD_ABOVE_USER_HEAD_OFFSET,
                userPosition.z + horizontalFacing.y);
    }

    private static Vec3 staffOriginStartup(Entity user, Vec3 staffTip) {
        double userY = user.getY();
        return new Vec3(staffTip.x, userY, staffTip.z);
    }

    /**
     * Calculates at what Y coordinate the staff's tip (upward extremity) should be.
     *
     * @param entity The entity using perch mode.
     * @return Returns the expected staff's tip Y coordinate.
     */
    private static double expectedStaffTipY(Entity entity) {
        return entity.getY() + entity.getBbHeight() + CatStaffItem.STAFF_HEAD_ABOVE_USER_HEAD_OFFSET;
    }

    protected static double expectedUserY(Entity user, newPerchingCatStaffData data) {
        double userHeight = user.getBbHeight();
        return data.staffTip().y - (userHeight + CatStaffItem.STAFF_HEAD_ABOVE_USER_HEAD_OFFSET);
    }

    private static newPerchingCatStaffData updateLength(Level level, Entity user, newPerchingCatStaffData data) {
        return switch (data.perchState()) {
            case STAND -> updateLengthStanding(user.getBbHeight(), data);
            case LAUNCH -> updateLengthLaunching(level, expectedStaffTipY(user), data);
            default -> data;
        };
    }

    // TODO refactor the following 2 because the block on which the staff is anchored might get broken
    private static newPerchingCatStaffData updateLengthStanding(double userHeight, newPerchingCatStaffData data) {
        double yMovement = switch (data.verticalMovement()) {
            case NEUTRAL -> 0.0;
            case ASCENDING -> CatStaffItem.USER_VERTICAL_MOVEMENT_SPEED;
            case DESCENDING -> -CatStaffItem.USER_VERTICAL_MOVEMENT_SPEED;
        };

        if (yMovement == 0.0) {
            return data;
        }

        double length = data.staffLength() + yMovement;
        double minLength = userHeight + CatStaffItem.STAFF_HEAD_ABOVE_USER_HEAD_OFFSET;
        double maxLength = MineraculousServerConfig.get().maxToolLength.get();

        length = Math.max(minLength, length);
        length = Math.min(maxLength, length);

        return data.withStaffLength(length, true);
    }

    private static newPerchingCatStaffData updateLengthLaunching(Level level, double expectedStaffTipY, newPerchingCatStaffData data) {
        newPerchingCatStaffData result = data;

        result = result.withStaffTipY(expectedStaffTipY);
        BlockPos targetPosition = BlockPos.containing(data.staffOrigin().subtract(0, CatStaffItem.STAFF_GROWTH_SPEED, 0)).below();
        boolean airBelow = level.getBlockState(targetPosition).isAir();
        if (airBelow) {
            if (result.onGround()) {
                result = result.withGround(false);
            }
            Vec3 newStaffOrigin = result.withStaffOriginY(data.staffOrigin().y - CatStaffItem.STAFF_GROWTH_SPEED).staffOrigin();
            double newLength = expectedStaffTipY - newStaffOrigin.y;
            double maxLength = MineraculousServerConfig.get().maxToolLength.get();
            if (newLength < maxLength) {
                result = result.withStaffLength(newLength, false);
            }
        } else if (!result.onGround()) {
            result = result.withGround(true);
        }
        return result;
    }

    private static newPerchingCatStaffData updateState(Entity user, newPerchingCatStaffData data) {
        switch (data.perchState()) {
            case LAUNCH -> {
                boolean userFalling = expectedStaffTipY(user) < data.staffTip().y;
                if (userFalling && data.onGround()) {
                    return data.withStaffTipY(expectedStaffTipY(user))
                            .withState(newPerchingCatStaffData.PerchingState.STAND)
                            .withGravity(false);
                }
            }
            case RELEASE -> {
                if (user.getY() - data.staffOrigin().y < 0.5) {
                    return data.withEnabled(false);
                }
            }
        }
        return data;
    }
}
