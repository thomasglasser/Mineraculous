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
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

/**
 * This class makes decisions regarding what should happen based on the
 * game context (level, user) when using the perch mode for cat staff tool.
 *
 * The perch mode allows the user to extend its staff when it anchors on
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
            CatStaffPerchGroundWorker.activateModeAndLaunch(level, user);
        }
    }

    public static void onLeftClick(Level level, LivingEntity user) {
        if (!level.isClientSide()) {
            newPerchingCatStaffData data = user.getData(MineraculousAttachmentTypes.newPERCHING_CAT_STAFF);
            newPerchingCatStaffData.PerchingState state = data.state();
            boolean canTransitionToLean = state == newPerchingCatStaffData.PerchingState.STAND || state == newPerchingCatStaffData.PerchingState.LAUNCH;
            if (data.isModeActive()) {
                if (data.onGround() && canTransitionToLean) {
                    CatStaffPerchGroundWorker.startLeaning(user, data);
                }
            }
        }
    }

    public static void entityFall(LivingFallEvent event) {
        Entity entity = event.getEntity();
        newPerchingCatStaffData data = entity.getData(MineraculousAttachmentTypes.newPERCHING_CAT_STAFF);
        newPerchingCatStaffData.PerchingState state = data.state();
        if (state == newPerchingCatStaffData.PerchingState.LEAN || state == newPerchingCatStaffData.PerchingState.RELEASE) {
            event.setDamageMultiplier(0);
            newPerchingCatStaffData.remove(entity);
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
            CatStaffPerchGroundWorker.cancelUserFallDamage(user, data);
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
