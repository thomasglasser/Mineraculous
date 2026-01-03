package dev.thomasglasser.mineraculous.impl.world.item.ability;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.impl.network.ServerboundPerchVerticalInputPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundUpdateStaffInputPayload;
import dev.thomasglasser.mineraculous.impl.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.impl.world.level.storage.newPerchingCatStaffData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * This class makes decisions regarding what should happen based on the
 * game context (level, user) when using the perch mode for cat staff tool.
 *
 * The perch mode allows the user to extend its staff until it anchors on
 * the ground. This mode has 3 behavior states : LAUNCH, STAND, RELEASE, LEAN.
 *
 * LAUNCH state makes the staff extend itself downwards and rocket the user
 * into the air until it reaches the ground. The staff will never extend past
 * the limit set in server configuration for max tool length.
 *
 * STAND state describes the moment when the staff is completely still and will
 * change its length only when the user uses the ascend and descend tool keybinds
 * which lately changes the VerticalMovement of the Perching State.
 * The staff length can never be negative and will always be bigger than the user's
 * height and smaller than the max tool length value set in configuration.
 *
 * RELEASE state means the user slowly lets go of the staff, therefore slips and
 * descends quickly without fall damage. If they move horizontally too much, the
 * tool will retract completely, and they will get fall damage. The staff
 * can transition to RELEASE state only from STAND state and when the user
 * right-clicks the tool.
 *
 * LEAN state will incline the staff causing a circular-motion fall. The user
 * won't experience damage as long as the staff has not retracted during the fall.
 * The staff can transition to LEAN state only from STAND state and when the user
 * left-clicks the tool. Jumping while falling or falling below the Y coordinate of
 * the staff's ground position will make the tool retract completely.
 */
public class CatStaffPerchCommander {
    private static final double POSITION_EPSILON = 1e-5;

    /**
     * This method gets triggered when the user right clicks with the cat staff.
     * 
     * @param level The level where the tool is.
     * @param user  The entity using the cat staff while in perch mode.
     */
    public static void itemUsed(Level level, LivingEntity user) {
        newPerchingCatStaffData data = user.getData(MineraculousAttachmentTypes.newPERCHING_CAT_STAFF);
        if (data.isModeActive()) {
            if (!level.isClientSide() && data.isStaffReleaseable()) {
                CatStaffPerchGroundWorker.makeUserReleaseStaff(user, data);
            }
        } else {
            CatStaffPerchGroundWorker.activateMode(level, user);
        }
    }

    /**
     * This method gets triggered every tick while the player has the
     * cat staff in their inventory, regardless of the selected mode.
     * 
     * @param level The level where the tool is.
     * @param user  The entity using the tool.
     */
    public static void tick(Level level, Entity user) {
        newPerchingCatStaffData originalData = user.getData(MineraculousAttachmentTypes.newPERCHING_CAT_STAFF);
        newPerchingCatStaffData data = originalData;

        data = CatStaffPerchGroundWorker.updateGravity(user, data);
        if (data.isModeActive()) {
            cancelFallDamage(user, data);
            CatStaffPerchGroundWorker.setUserVerticalPosition(user, data);
            decideToConstrainUserPosition(user, data);
            if (level.isClientSide()) {
                signalMovementInputToServer(data);
            } else {
                data = CatStaffPerchGroundWorker.updateStateAndLength(level, user, data);
            }
        }
        originalData.update(user, data);
    }

    private static void cancelFallDamage(Entity user, newPerchingCatStaffData data) {
        if (data.shouldCancelFallDamage()) {
            user.resetFallDistance();
        }
    }

    /**
     * Tethering states are states only used to determine if the user's position should be
     * constrained.
     * See PerchingCatStaffData to see which states are considered tethering.
     * 
     * @param user The entity whose position is questioned whether to be constrained.
     * @param data The user's perching data.
     */
    private static void decideToConstrainUserPosition(Entity user, newPerchingCatStaffData data) {
        if (data.hasTetheringState()) {
            Vec3 userToStaff = CatStaffPerchGroundWorker.userToStaff(user, data);
            if (shouldConstrainPositon(userToStaff)) {
                CatStaffPerchGroundWorker.constrainUserPosition(user, data);
            }
        }
    }

    private static boolean shouldConstrainPositon(Vec3 userToStaff) {
        return Math.abs(userToStaff.length() - CatStaffItem.DISTANCE_BETWEEN_STAFF_AND_USER_IN_BLOCKS) > POSITION_EPSILON;
    }

    private static void signalMovementInputToServer(newPerchingCatStaffData data) {
        sendVerticalInput(data);
        sendHorizontalInput();
    }

    private static void sendHorizontalInput() {
        MineraculousClientUtils.InputState input = MineraculousClientUtils.captureInput();
        int packedInput = input.packInputs();
        TommyLibServices.NETWORK.sendToServer(new ServerboundUpdateStaffInputPayload(packedInput));
    }

    /**
     * This method is called on the client, and it signals the user's input to the server.
     * 
     * @param data The user's perching data, used only to check if the client can signal the server.
     */
    private static void sendVerticalInput(newPerchingCatStaffData data) {
        if (!data.perchingStateHasGravity()) {
            boolean ascend = MineraculousKeyMappings.ASCEND_TOOL.isDown();
            boolean descend = MineraculousKeyMappings.DESCEND_TOOL.isDown();
            newPerchingCatStaffData.VerticalMovement suggestedMovement = newPerchingCatStaffData.getVerticalMovement(ascend, descend);
            if (suggestedMovement != data.verticalMovement()) {
                TommyLibServices.NETWORK.sendToServer(new ServerboundPerchVerticalInputPayload(ascend, descend));
            }
        }
    }
}
