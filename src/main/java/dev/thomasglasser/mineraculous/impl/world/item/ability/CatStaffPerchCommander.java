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

/**
 * This class makes decisions regarding what should happen based on the
 * game context (level, user) when using the perch mode for cat staff tool.
 *
 * The perch mode allows the user to extend its staff until it anchors on
 * the ground. This mode has 3 behavior states : LAUNCH, STAND, RELEASE, LEAN.
 */
public class CatStaffPerchCommander {
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

    public static void onLeftClick(Level level, LivingEntity user) {
        if (!level.isClientSide()) {
            newPerchingCatStaffData data = user.getData(MineraculousAttachmentTypes.newPERCHING_CAT_STAFF);
            if (data.isModeActive() && data.onGround() && data.state() == newPerchingCatStaffData.PerchingState.STAND) {
                CatStaffPerchGroundWorker.startLeaning(user, data);
            }
        }
    }

    /**
     * This method gets triggered every tick while the player has the
     * cat staff in their inventory, regardless of the selected mode.
     * 
     * @param level The level where the tool is.
     * @param user  The entity using the tool.
     */
    public static void tick(Level level, Entity user, CatStaffItem.Mode mode) {
        if (mode != CatStaffItem.Mode.PERCH && user.hasData(MineraculousAttachmentTypes.newPERCHING_CAT_STAFF)) {
            newPerchingCatStaffData.remove(user);
        }

        newPerchingCatStaffData originalData = user.getData(MineraculousAttachmentTypes.newPERCHING_CAT_STAFF);
        newPerchingCatStaffData data = originalData;

        data = CatStaffPerchGroundWorker.applyGravity(user, data);
        if (data.isModeActive()) {
            cancelFallDamage(user, data);
            CatStaffPerchGroundWorker.alignUserVerticalPosition(user, data);
            CatStaffPerchGroundWorker.constrainUserPosition(user, data);
            if (level.isClientSide()) {
                signalMovementInputToServer(data);
            } else {
                data = CatStaffPerchGroundWorker.adjustLength(level, user, data);
                data = CatStaffPerchGroundWorker.transitionState(user, data);
            }
        }
        originalData.update(user, data);
    }

    private static final double USER_HEAD_CLEARANCE_BLOCKS = 1.0;

    private static void cancelFallDamage(Entity user, newPerchingCatStaffData data) {
        boolean shouldCancelFallDamageWhileLeaning = data.state() == newPerchingCatStaffData.PerchingState.LEAN &&
                user.getY() + USER_HEAD_CLEARANCE_BLOCKS > data.staffOrigin().y;;
        if (data.shouldCancelFallDamage() || shouldCancelFallDamageWhileLeaning) {
            user.resetFallDistance();
        }
    }

    private static void signalMovementInputToServer(newPerchingCatStaffData data) {
        sendVerticalInput(data);
        sendWASDJumpInput();
    }

    private static void sendWASDJumpInput() {
        MineraculousClientUtils.InputState input = MineraculousClientUtils.captureInput();
        if (input.hasInput()) {
            int packedInput = input.packInputs();
            TommyLibServices.NETWORK.sendToServer(new ServerboundUpdateStaffInputPayload(packedInput));
        }
    }

    private static void sendVerticalInput(newPerchingCatStaffData data) {
        if (data.state() == newPerchingCatStaffData.PerchingState.STAND) {
            boolean ascend = MineraculousKeyMappings.ASCEND_TOOL.isDown();
            boolean descend = MineraculousKeyMappings.DESCEND_TOOL.isDown();
            newPerchingCatStaffData.VerticalMovement suggestedMovement = newPerchingCatStaffData.getVerticalMovement(ascend, descend);
            if (suggestedMovement != data.verticalMovement()) {
                TommyLibServices.NETWORK.sendToServer(new ServerboundPerchVerticalInputPayload(suggestedMovement));
            }
        }
    }
}
