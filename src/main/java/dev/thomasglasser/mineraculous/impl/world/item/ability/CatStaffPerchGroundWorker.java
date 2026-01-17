package dev.thomasglasser.mineraculous.impl.world.item.ability;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.impl.world.level.storage.PerchingCatStaffData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class CatStaffPerchGroundWorker {
    private static final double LAUNCHING_USER_STRENGTH = 2.0;
    private static final double POSITION_EPSILON = 1e-5;
    private static final int LEANING_PUSH_SCALE_DIVISOR = 10;
    private static final double USER_FELL_TOO_MUCH_THRESHOLD = 0.5;
    private static final double USER_TOO_FAR_THRESHOLD = 1;
    private static final double USER_HEAD_CLEARANCE_BLOCKS = 1.0;

    /**
     * Used for rendering purposes only, already lerped.
     * 
     * @param user
     * @return
     */
    public static Vec3 expectedStaffTip(Entity user, float partialTick) {
        Vec3 delta = new Vec3(0, user.getEyeHeight(Pose.STANDING) + CatStaffItem.STAFF_HEAD_ABOVE_USER_HEAD_OFFSET, 0);
        PerchingCatStaffData perchingData = user.getData(MineraculousAttachmentTypes.PERCHING_CAT_STAFF);
        PerchingCatStaffData.PerchingState state = perchingData.state();
        boolean movingTip = state == PerchingCatStaffData.PerchingState.LAUNCH ||
                state == PerchingCatStaffData.PerchingState.STAND ||
                state == PerchingCatStaffData.PerchingState.RELEASE;
        if (movingTip) {
            Vec3 oldPos = new Vec3(user.xOld, user.yOld, user.zOld);
            Vec3 from = oldPos.add(delta);
            Vec3 to = user.position().add(delta);
            return from.lerp(to, partialTick);
        } else {
            return perchingData.userPositionBeforeLeanOrRelease().add(delta);
        }
    }

    /**
     * Calculates at what Y coordinate the staff's tip (upward extremity) should be.
     *
     * @param entity The entity using perch mode.
     * @return Returns the expected staff's tip Y coordinate.
     */
    public static double expectedStaffTipY(Entity entity) {
        return entity.getY() + entity.getEyeHeight(Pose.STANDING) + CatStaffItem.STAFF_HEAD_ABOVE_USER_HEAD_OFFSET;
    }

    protected static void activateModeAndLaunch(Level level, LivingEntity user) {
        if (!level.isClientSide()) {
            setUserLaunchingData(user);
        }
        launchUser(user);
    }

    protected static void makeUserReleaseStaff(Entity user, PerchingCatStaffData data) {
        data
                .withState(PerchingCatStaffData.PerchingState.RELEASE)
                .withGravity(true)
                .withUserPositionBeforeLeanOrRelease(user.position())
                .save(user);
    }

    protected static PerchingCatStaffData applyGravity(Entity user, PerchingCatStaffData data) {
        boolean stateHasGravity = data.state() != PerchingCatStaffData.PerchingState.STAND;
        user.setNoGravity(!data.userGravity());
        return data.withGravity(stateHasGravity);
    }

    protected static void cancelUserFallDamage(Entity user, PerchingCatStaffData data) {
        boolean userAboveOrigin = user.getY() + USER_HEAD_CLEARANCE_BLOCKS > data.staffOrigin().y;
        boolean leaning = data.state() == PerchingCatStaffData.PerchingState.LEAN;
        if (data.shouldCancelFallDamage() || (leaning && userAboveOrigin)) {
            user.resetFallDistance();
        }
    }

    protected static void alignUserVerticalPosition(Entity user, PerchingCatStaffData data) {
        if (user.isNoGravity()) {
            double verticalPosition = expectedUserY(user, data);
            double positionCorrection = verticalPosition - user.getY();
            Vec3 movement = new Vec3(0, positionCorrection, 0);
            user.move(MoverType.SELF, movement);
            user.hurtMarked = true;
        }
    }

    protected static PerchingCatStaffData adjustLength(Level level, Entity user, PerchingCatStaffData data) {
        BlockPos belowStaff = BlockPos.containing(data.staffOrigin()).below();
        boolean onGround = level.getBlockState(belowStaff).isSolid();
        double expectedStaffTipY = expectedStaffTipY(user);

        data = data.withGround(onGround);
        if (onGround && data.state() == PerchingCatStaffData.PerchingState.STAND) {
            data = applyVerticalInput(user, data);
            data = fixLengthOverMaxLimit(data);
        } else if (data.state() != PerchingCatStaffData.PerchingState.LEAN) {
            data = data.withStaffTipY(expectedStaffTipY);
        }

        if (!onGround) {
            data = extendDownward(level, expectedStaffTipY, data);
        }

        return data;
    }

    protected static PerchingCatStaffData transitionState(Entity user, PerchingCatStaffData data) {
        if (data.state() == PerchingCatStaffData.PerchingState.LAUNCH) {
            data = transitionFromLaunch(user, data);
        } else if (data.state() == PerchingCatStaffData.PerchingState.RELEASE) {
            data = transitionFromRelease(user, data);
        } else if (data.state() == PerchingCatStaffData.PerchingState.LEAN) {
            data = transitionFromLean(user, data);
        }
        return data;
    }

    protected static void startLeaning(Entity user, PerchingCatStaffData data) {
        Vec2 horizontalFacing = MineraculousMathUtils.getHorizontalFacingVector(user.getYRot());
        Vec3 push = new Vec3(horizontalFacing.x, 0, horizontalFacing.y);
        user.setDeltaMovement(push.scale(data.staffLength() / LEANING_PUSH_SCALE_DIVISOR));
        user.hurtMarked = true;
        getUserLeaningData(user, data).save(user);
    }

    protected static void constrainUserPosition(Entity user, PerchingCatStaffData data) {
        if (data.state() == PerchingCatStaffData.PerchingState.STAND) {
            constrainUserAroundStaff(user, data);
        } else if (data.state() == PerchingCatStaffData.PerchingState.LEAN) {
            anchorUserToStaff(user, data);
        }
    }

    private static PerchingCatStaffData disablePerch(PerchingCatStaffData data) {
        return data.withEnabled(false);
    }

    private static double expectedUserY(Entity user, PerchingCatStaffData data) {
        double userHeight = user.getEyeHeight(Pose.STANDING);
        return data.staffTip().y - (userHeight + CatStaffItem.STAFF_HEAD_ABOVE_USER_HEAD_OFFSET);
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

    private static PerchingCatStaffData createLaunchingData(LivingEntity user, Vec3 staffOrigin, Vec3 staffTip) {
        return new PerchingCatStaffData(
                PerchingCatStaffData.PerchingState.LAUNCH,
                PerchingCatStaffData.VerticalMovement.NEUTRAL,
                Direction.fromYRot(user.yHeadRot),
                user.position(),
                staffOrigin,
                staffTip,
                true,
                false,
                true);
    }

    private static PerchingCatStaffData getUserLeaningData(Entity user, PerchingCatStaffData data) {
        return data
                .withState(PerchingCatStaffData.PerchingState.LEAN)
                .withUserPositionBeforeLeanOrRelease(user.position());
    }

    private static PerchingCatStaffData getUserStandingData(Entity user, PerchingCatStaffData data) {
        return data.withStaffTipY(expectedStaffTipY(user))
                .withState(PerchingCatStaffData.PerchingState.STAND)
                .withGravity(false);
    }

    private static PerchingCatStaffData transitionFromLaunch(Entity user, PerchingCatStaffData data) {
        boolean userFalling = user.getDeltaMovement().y < 0;
        if (userFalling && data.onGround()) {
            return getUserStandingData(user, data);
        }
        boolean userGotTooFar = data.horizontalPosition().subtract(user.getX(), 0, user.getZ()).length() > USER_TOO_FAR_THRESHOLD;
        if (userGotTooFar) {
            return disablePerch(data);
        }
        return data;
    }

    private static PerchingCatStaffData transitionFromRelease(Entity user, PerchingCatStaffData data) {
        boolean userFellTooMuch = user.getY() - data.staffOrigin().y < USER_FELL_TOO_MUCH_THRESHOLD;
        boolean userGotTooFar = data.horizontalPosition().subtract(user.getX(), 0, user.getZ()).length() > USER_TOO_FAR_THRESHOLD;
        if (userFellTooMuch || userGotTooFar) {
            return disablePerch(data);
        }
        return data;
    }

    private static PerchingCatStaffData transitionFromLean(Entity user, PerchingCatStaffData data) {
        boolean userFellTooMuch = user.getY() + USER_FELL_TOO_MUCH_THRESHOLD < data.staffOrigin().y;
        boolean userJumped = user.getDeltaMovement().y > 0;
        boolean userGotTooFar = user.position().subtract(data.staffOrigin()).length() - data.staffLength() > USER_TOO_FAR_THRESHOLD;
        if (userJumped || userFellTooMuch || user.onGround() || userGotTooFar) {
            return disablePerch(data);
        }
        return data;
    }

    private static PerchingCatStaffData applyVerticalInput(Entity user, PerchingCatStaffData data) {
        double yMovement = switch (data.verticalMovement()) {
            case NEUTRAL -> 0.0;
            case ASCENDING -> CatStaffItem.USER_VERTICAL_MOVEMENT_SPEED;
            case DESCENDING -> -CatStaffItem.USER_VERTICAL_MOVEMENT_SPEED;
        };
        if (yMovement == 0.0) {
            return data;
        }

        double maxLength = MineraculousServerConfig.get().maxToolLength.get();
        double minLength = user.getEyeHeight(Pose.STANDING) + CatStaffItem.STAFF_HEAD_ABOVE_USER_HEAD_OFFSET;
        double length = data.staffLength();
        length += (length + yMovement < maxLength) ? yMovement : (maxLength - length);
        length = Math.max(minLength, length);
        return data.withStaffLength(length, true);
    }

    private static PerchingCatStaffData fixLengthOverMaxLimit(PerchingCatStaffData data) {
        double length = data.staffLength();
        double maxLength = MineraculousServerConfig.get().maxToolLength.get();
        if (length > maxLength) {
            double delta = Math.min(1, length - maxLength);
            length -= delta;
            data = data.withStaffLength(length, true);
        }
        return data;
    }

    private static PerchingCatStaffData extendDownward(Level level, double expectedStaffTipY, PerchingCatStaffData data) {
        double maxLength = MineraculousServerConfig.get().maxToolLength.get();
        BlockPos targetPosition = BlockPos.containing(data.staffOrigin());
        int analyzedBlocks = 0;
        while (!level.getBlockState(targetPosition.below()).isSolid() && analyzedBlocks <= CatStaffItem.STAFF_GROWTH_SPEED) {
            analyzedBlocks++;
            targetPosition = targetPosition.below();
        }
        Vec3 newStaffOrigin = data.withStaffOriginY(targetPosition.getY()).staffOrigin();
        double newLength = expectedStaffTipY - newStaffOrigin.y;
        if (newLength < maxLength) {
            data = data.withStaffLength(newLength, false);
        }
        return data;
    }

    private static void constrainUserAroundStaff(Entity user, PerchingCatStaffData data) {
        Vec3 userHorizontalPosition = new Vec3(user.getX(), 0, user.getZ());
        Vec3 fromPlayerToStaff = data.horizontalPosition().subtract(userHorizontalPosition);
        double distance = fromPlayerToStaff.length();
        boolean shouldConstrain = Math.abs(distance - CatStaffItem.DISTANCE_BETWEEN_STAFF_AND_USER_IN_BLOCKS) > POSITION_EPSILON;
        if (shouldConstrain) {
            Vec3 constrain = fromPlayerToStaff
                    .normalize()
                    .scale(distance - CatStaffItem.DISTANCE_BETWEEN_STAFF_AND_USER_IN_BLOCKS);
            user.move(MoverType.SELF, constrain);
            user.hurtMarked = true;
        }
    }

    private static void anchorUserToStaff(Entity user, PerchingCatStaffData data) {
        Vec3 fromPlayerToOrigin = data.staffOrigin().subtract(user.position());
        double distance = fromPlayerToOrigin.length();
        double length = data.staffLength() - user.getEyeHeight(Pose.STANDING) - CatStaffItem.STAFF_HEAD_ABOVE_USER_HEAD_OFFSET;
        boolean shouldConstrain = Math.abs(distance - length) > POSITION_EPSILON && user.getY() > data.staffOrigin().y;
        boolean inAir = user.level().getBlockState(BlockPos.containing(user.position())).isAir();
        if (shouldConstrain && inAir) {
            Vec3 constrain = fromPlayerToOrigin
                    .normalize()
                    .scale(distance - length);
            user.move(MoverType.SELF, constrain);
            user.hurtMarked = true;
        }
    }

    private static Vec3 staffTipStartup(Entity user) {
        Vec3 userPosition = user.position();
        Vec2 horizontalFacing = MineraculousMathUtils.getHorizontalFacingVector(user.getYRot());
        Vec3 front = new Vec3(horizontalFacing.x, 0, horizontalFacing.y);
        Vec3 placement = user.onGround()
                ? front
                : MineraculousMathUtils.UP.cross(front)
                        .scale((user instanceof Player player && player.getMainArm() == HumanoidArm.RIGHT) ? -1 : 1)
                        .add(front.scale(CatStaffItem.DISTANCE_BETWEEN_STAFF_AND_USER_IN_BLOCKS));
        placement = placement.scale(CatStaffItem.DISTANCE_BETWEEN_STAFF_AND_USER_IN_BLOCKS);
        double userHeight = user.getEyeHeight(Pose.STANDING);
        return new Vec3(
                userPosition.x + placement.x,
                userPosition.y + userHeight + CatStaffItem.STAFF_HEAD_ABOVE_USER_HEAD_OFFSET,
                userPosition.z + placement.z);
    }

    private static Vec3 staffOriginStartup(Entity user, Vec3 staffTip) {
        double userY = user.getY();
        return new Vec3(staffTip.x, userY, staffTip.z);
    }
}
