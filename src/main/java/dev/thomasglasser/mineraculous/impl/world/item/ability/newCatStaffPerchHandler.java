package dev.thomasglasser.mineraculous.impl.world.item.ability;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.impl.network.ServerboundPerchVerticalInputPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundUpdateStaffInputPayload;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.impl.world.level.storage.newPerchingCatStaffData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class newCatStaffPerchHandler {
    public static double getExpectedStaffHeadY(Entity entity) {
        return entity.getY() + entity.getBbHeight() + CatStaffItem.STAFF_HEAD_ABOVE_USER_HEAD_OFFSET;
    }

    public static void itemUsed(Level level, LivingEntity user) {
        newPerchingCatStaffData perchingData = user.getData(MineraculousAttachmentTypes.newPERCHING_CAT_STAFF);
        if (perchingData.enabled()) {
            if (!level.isClientSide()) {
                perchingData.release().save(user);
            }
        } else {
            if (!level.isClientSide()) {
                Vec3 position = user.position();
                float height = user.getBbHeight();
                float headRotation = user.yHeadRot;
                Vec2 horizontalFacing = MineraculousMathUtils.getHorizontalFacingVector(user);

                perchingData.launch(position, height, headRotation, horizontalFacing).save(user);
            }
            user.hurtMarked = true;
            user.addDeltaMovement(new Vec3(0, 2, 0));
        }
    }

    // TODO add a method which changes the staff length inside STAND state if it is bigger than the max

    public static void tick(Level level, Entity user) {
        newPerchingCatStaffData originalPerchingData = user.getData(MineraculousAttachmentTypes.newPERCHING_CAT_STAFF);
        newPerchingCatStaffData updatedPerchingData = originalPerchingData;
        user.setNoGravity(!updatedPerchingData.userGravity());
        if (updatedPerchingData.enabled()) {
            cancelFallDamage(user, updatedPerchingData);
            updatedPerchingData = updatedPerchingData.applyGravity();
            setUserVerticalPosition(user, updatedPerchingData);
            constrainUserPosition(user, updatedPerchingData);
            if (level.isClientSide()) {
                signalMovementInputToServer(updatedPerchingData);
            } else {
                BlockPos targetPosition = BlockPos.containing(updatedPerchingData.staffOrigin().subtract(0, CatStaffItem.STAFF_GROWTH_SPEED, 0)).below();
                boolean airBelowTarget = level.getBlockState(targetPosition).isAir();
                double expectedHeadY = getExpectedStaffHeadY(user);

                updatedPerchingData = updatedPerchingData
                        .updateState(user.getY(), expectedHeadY)
                        .updateLength(airBelowTarget, expectedHeadY);
            }
        }
        originalPerchingData.update(user, updatedPerchingData);
    }

    private static void cancelFallDamage(Entity user, newPerchingCatStaffData perchingData) {
        if (perchingData.shouldCancelFallDamage()) {
            user.resetFallDistance();
        }
    }

    private static void signalMovementInputToServer(newPerchingCatStaffData perchingData) {
        sendVerticalInput(perchingData);
        sendHorizontalInput();
    }

    private static void sendHorizontalInput() {
        MineraculousClientUtils.InputState input = MineraculousClientUtils.captureInput();
        int packedInput = input.packInputs();
        TommyLibServices.NETWORK.sendToServer(new ServerboundUpdateStaffInputPayload(packedInput));
    }

    private static void constrainUserPosition(Entity user, newPerchingCatStaffData perchingData) {
        if (perchingData.hasTetheringState()) {
            Vec3 staffHorizontalPosition = new Vec3(perchingData.staffOrigin().x, 0, perchingData.staffOrigin().z);
            Vec3 userHorizontalPosition = new Vec3(user.getX(), 0, user.getZ());
            Vec3 fromPlayerToStaff = staffHorizontalPosition.subtract(userHorizontalPosition);
            double distance = fromPlayerToStaff.length();
            if (Math.abs(distance - CatStaffItem.DISTANCE_BETWEEN_STAFF_AND_USER) > 1e-5) {
                Vec3 constrain = fromPlayerToStaff
                        .normalize()
                        .scale(fromPlayerToStaff.length() - CatStaffItem.DISTANCE_BETWEEN_STAFF_AND_USER);
                user.move(MoverType.SELF, constrain);
            }
        }
    }

    private static void sendVerticalInput(newPerchingCatStaffData perchingData) {
        if (!perchingData.hasGravity()) {
            boolean ascend = MineraculousKeyMappings.ASCEND_TOOL.isDown();
            boolean descend = MineraculousKeyMappings.DESCEND_TOOL.isDown();
            newPerchingCatStaffData.VerticalMovement currentVerticalMovement = newPerchingCatStaffData.getVerticalMovement(ascend, descend);
            if (currentVerticalMovement != perchingData.verticalMovement()) {
                TommyLibServices.NETWORK.sendToServer(new ServerboundPerchVerticalInputPayload(ascend, descend));
            }
        }
    }

    private static void setUserVerticalPosition(Entity user, newPerchingCatStaffData perchingData) {
        if (!perchingData.hasGravity()) {
            double targetY = perchingData.staffHead().y - (user.getBbHeight() + CatStaffItem.STAFF_HEAD_ABOVE_USER_HEAD_OFFSET);
            double deltaY = targetY - user.getY();
            Vec3 movement = new Vec3(0, deltaY, 0);
            user.move(MoverType.SELF, movement);
            user.hurtMarked = true;
        }
    }
}
