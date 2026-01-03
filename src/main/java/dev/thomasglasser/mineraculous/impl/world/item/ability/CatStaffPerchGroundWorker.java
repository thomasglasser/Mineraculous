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
        BlockPos targetPosition = BlockPos.containing(data.staffOrigin().subtract(0, CatStaffItem.STAFF_GROWTH_SPEED, 0)).below();
        boolean airBelowTarget = level.getBlockState(targetPosition).isAir();
        double expectedHeadY = expectedStaffTipY(user);

        data = updateState(data, user.getY(), expectedHeadY);
        data = CatStaffPerchGroundWorker.updateLength(airBelowTarget, user.getBbHeight(), expectedHeadY, data);
        return data;
    }

    protected static void setUserVerticalPosition(Entity user, newPerchingCatStaffData data) {
        if (/*!perchingStateHasGravity(user, data)*/ user.isNoGravity()) { //TODO account only the server result but execute on both
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

    private static newPerchingCatStaffData updateLength(boolean airBelow, double userHeight, double expectedStaffTipY, newPerchingCatStaffData data) {
        return switch (data.perchState()) {
            case STAND -> updateLengthStanding(userHeight, data);
            case LAUNCH -> updateLengthLaunching(data, airBelow, expectedStaffTipY);
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
        double minLength = data.staffOrigin().y + userHeight + CatStaffItem.STAFF_HEAD_ABOVE_USER_HEAD_OFFSET;
        double maxLength = MineraculousServerConfig.get().maxToolLength.get();
        /*if (length < minLength) {
            return data.withStaffLength(minLength, true);
        }
        if (length > maxLength) {
            return data.withStaffLength(maxLength, true);
        }*/

        return data.withStaffLength(length, true);
    }

    private static newPerchingCatStaffData updateLengthLaunching(newPerchingCatStaffData data, boolean airBelow, double apexStaffTipY) {
        newPerchingCatStaffData result = data;

        result = result.withStaffTipY(apexStaffTipY);
        if (airBelow) {
            if (result.onGround()) {
                result = result.withGround(false);
            }
            double maxLength = MineraculousServerConfig.get().maxToolLength.get();
            if (result.staffLength() < maxLength) {
                result = result.withStaffOriginY(data.staffOrigin().y - CatStaffItem.STAFF_GROWTH_SPEED);
            }
        } else if (!result.onGround()) {
            result = result.withGround(true);
        }
        return result;
    }

    private static newPerchingCatStaffData updateState(newPerchingCatStaffData data, double userY, double staffTipYExpectation) {
        switch (data.perchState()) {
            case LAUNCH -> {
                boolean userFalling = staffTipYExpectation < data.staffTip().y;
                if (userFalling && data.onGround()) {
                    return data.withStaffTipY(staffTipYExpectation)
                            .withState(newPerchingCatStaffData.PerchingState.STAND)
                            .withGravity(false);
                }
            }
            case RELEASE -> {
                if (userY - data.staffOrigin().y < 0.5) {
                    return data.withEnabled(false);
                }
            }
        }
        return data;
    }
}
