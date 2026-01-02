package dev.thomasglasser.mineraculous.impl.world.item.ability;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.impl.network.ServerboundPerchVerticalInputPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundUpdateStaffInputPayload;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.level.storage.newPerchingCatStaffData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class newCatStaffPerchHandler {
    private static final float DISTANCE_BETWEEN_STAFF_AND_USER = 0.5f;
    private static final float STAFF_HEAD_ABOVE_USER_HEAD_OFFSET = 0.2f;
    private static final float USER_VERTICAL_MOVEMENT_SPEED = 0.5f;
    public static final double HORIZONTAL_MOVEMENT_THRESHOLD = 0.15d;
    public static final double HORIZONTAL_MOVEMENT_SCALE = 0.1d;

    public static void itemUsed(Level level, LivingEntity user) {
        if (!level.isClientSide()) {
            if (user.hasData(MineraculousAttachmentTypes.newPERCHING_CAT_STAFF)) {
                newPerchingCatStaffData perchingData = user.getData(MineraculousAttachmentTypes.newPERCHING_CAT_STAFF);
                if (perchingData.enabled()) {
                    startReleasing(user, perchingData);
                } else {
                    startLaunching(level, user);
                }
            } else {
                startLaunching(level, user);
            }
        }
    }

    public static void tick(Level level, Entity user) {
        newPerchingCatStaffData perchingData = user.getData(MineraculousAttachmentTypes.newPERCHING_CAT_STAFF);
        user.setNoGravity(!perchingData.userGravity());
        if (perchingData.enabled()) {
            cancelFallDamage(user, perchingData);
            setVerticalMovement(user, perchingData);
            constrainUserPosition(user, perchingData);
            if (level.isClientSide()) {
                signalMovementInputToServer(perchingData);
                handleHorizontalMovementInput(perchingData);
            } else {
                updatePerchingState(user, perchingData);
                updateStaffLength(user, perchingData);
            }
        }
    }

    private static void cancelFallDamage(Entity user, newPerchingCatStaffData perchingData) {
        if (perchingData.perchState() == newPerchingCatStaffData.PerchingState.LAUNCH ||
                perchingData.perchState() == newPerchingCatStaffData.PerchingState.RELEASE ||
                perchingData.perchState() == newPerchingCatStaffData.PerchingState.STAND) {

            user.resetFallDistance();
        }
    }

    private static void handleHorizontalMovementInput(newPerchingCatStaffData perchData) {
        MineraculousClientUtils.InputState input = MineraculousClientUtils.captureInput();
        int packedInput = input.packInputs();
        TommyLibServices.NETWORK.sendToServer(new ServerboundUpdateStaffInputPayload(packedInput, perchData));
    }

    private static void constrainUserPosition(Entity user, newPerchingCatStaffData perchingData) {
        if (perchingData.perchState() == newPerchingCatStaffData.PerchingState.STAND) {
            Vec3 staffHorizontalPosition = new Vec3(perchingData.staffOrigin().x, 0, perchingData.staffOrigin().z);
            Vec3 userHorizontalPosition = new Vec3(user.getX(), 0, user.getZ());
            Vec3 fromPlayerToStaff = staffHorizontalPosition.subtract(userHorizontalPosition);
            double distance = fromPlayerToStaff.length();
            if (distance != DISTANCE_BETWEEN_STAFF_AND_USER) {
                Vec3 constrain = fromPlayerToStaff
                        .normalize()
                        .scale(fromPlayerToStaff.length() - DISTANCE_BETWEEN_STAFF_AND_USER);
                user.move(MoverType.SELF, constrain);

            }
        }
    }

    private static void updatePerchingState(Entity user, newPerchingCatStaffData perchingData) {
        if (perchingData.perchState() == newPerchingCatStaffData.PerchingState.LAUNCH) {
            double headYRelative = user.getY() + user.getBbHeight() + STAFF_HEAD_ABOVE_USER_HEAD_OFFSET;
            double deltaY = headYRelative - perchingData.staffOrigin().y;
            if (deltaY < perchingData.getStaffLength()) {
                perchingData
                        .withStaffLength(deltaY)
                        .withState(newPerchingCatStaffData.PerchingState.STAND)
                        .withGravity(false)
                        .save(user);
            }
        }
        if (perchingData.perchState() == newPerchingCatStaffData.PerchingState.RELEASE) {
            if (user.getY() - perchingData.staffOrigin().y < 0.5) {
                newPerchingCatStaffData.remove(user);
            }
        }
    }

    private static void signalMovementInputToServer(newPerchingCatStaffData perchingData) {
        if (perchingData.perchState() == newPerchingCatStaffData.PerchingState.STAND) {
            boolean ascend = MineraculousKeyMappings.ASCEND_TOOL.isDown();
            boolean descend = MineraculousKeyMappings.DESCEND_TOOL.isDown();
            newPerchingCatStaffData.VerticalMovement currentVerticalMovement = newPerchingCatStaffData.getVerticalMovement(ascend, descend);
            if (currentVerticalMovement != perchingData.verticalMovement()) {
                TommyLibServices.NETWORK.sendToServer(new ServerboundPerchVerticalInputPayload(perchingData, ascend, descend));
            }
        }
    }

    private static void setVerticalMovement(Entity user, newPerchingCatStaffData perchingData) {
        if (perchingData.perchState() == newPerchingCatStaffData.PerchingState.STAND) {
            double targetY = perchingData.staffHead().y - (user.getBbHeight() + STAFF_HEAD_ABOVE_USER_HEAD_OFFSET);
            double deltaY = targetY - user.getY();
            Vec3 movement = new Vec3(0, deltaY, 0);
            user.move(MoverType.SELF, movement);
            user.hurtMarked = true;
            perchingData.withGravity(false).save(user);
        } else {
            perchingData.withGravity(true).save(user);
        }
    }

    private static void updateStaffLength(Entity user, newPerchingCatStaffData perchingData) {
        if (perchingData.perchState() == newPerchingCatStaffData.PerchingState.STAND) {
            double yMovement = switch (perchingData.verticalMovement()) {
                case NETURAL -> 0.0;
                case ASCENDING -> USER_VERTICAL_MOVEMENT_SPEED;
                case DESCENDING -> -USER_VERTICAL_MOVEMENT_SPEED;
            };

            double newLength = perchingData.getStaffLength() + yMovement;
            perchingData.withStaffLength(newLength).save(user);
        } else if (perchingData.perchState() == newPerchingCatStaffData.PerchingState.LAUNCH) {
            double newStaffHeadY = user.getY() + user.getBbHeight() + STAFF_HEAD_ABOVE_USER_HEAD_OFFSET;
            double newLength = newStaffHeadY - perchingData.staffOrigin().y;
            if (newLength > perchingData.getStaffLength()) {
                perchingData.withStaffLength(newLength).save(user);
            }
        }
    }

    private static void startReleasing(LivingEntity user, newPerchingCatStaffData perchingData) {
        if (perchingData.perchState() == newPerchingCatStaffData.PerchingState.STAND) {
            perchingData
                    .withState(newPerchingCatStaffData.PerchingState.RELEASE)
                    .withGravity(true)
                    .save(user);
        }
    }

    private static void startLaunching(Level level, LivingEntity user) {
        Vec3 staffHead = getInitializedStaffHead(user);
        Vec3 staffOrigin = getStaffOrigin(level, staffHead);
        final boolean enable = true;
        final boolean gravity = false;
        new newPerchingCatStaffData(
                newPerchingCatStaffData.PerchingState.LAUNCH,
                newPerchingCatStaffData.VerticalMovement.NETURAL,
                Direction.fromYRot(user.yHeadRot),
                user.position(),
                staffOrigin,
                staffHead,
                enable,
                gravity).save(user);
        user.hurtMarked = true;
        user.addDeltaMovement(new Vec3(0, 1, 0));
    }

    private static Vec3 getInitializedStaffHead(Entity user) {
        Vec2 horizontalFacingVector = MineraculousMathUtils.getHorizontalFacingVector(user).scale(DISTANCE_BETWEEN_STAFF_AND_USER);
        Vec3 staffHead = new Vec3(
                user.getX() + horizontalFacingVector.x,
                user.getY() + user.getBbHeight() + STAFF_HEAD_ABOVE_USER_HEAD_OFFSET,
                user.getZ() + horizontalFacingVector.y);
        return staffHead;
    }

    private static Vec3 getStaffOrigin(Level level, Vec3 staffHead) {
        Vec3 staffOrigin = getGroundPosition(level, staffHead);
        return staffOrigin;
    }

    // TODO rename this and make the staff origin graddualy decrease its Y
    private static Vec3 getGroundPosition(Level level, Vec3 startPosition) {
        BlockPos position = BlockPos.containing(startPosition);
        int i = 0;
        while (level.getBlockState(position).isAir() && i < MineraculousServerConfig.get().maxToolLength.get()) {
            position = position.below();
        }
        position = position.above();
        return new Vec3(startPosition.x, position.getY(), startPosition.z);
    }
}
