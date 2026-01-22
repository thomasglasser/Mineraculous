package dev.thomasglasser.mineraculous.impl.world.item.ability;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.impl.world.level.storage.newTravelingCatStaffData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class CatStaffTravelCommander {
    public static void itemUsed(Level level, LivingEntity user) {
        newTravelingCatStaffData data = user.getData(MineraculousAttachmentTypes.newTRAVELING_CAT_STAFF);
        if (data.isModeActive() && !data.retracting()) {
            CatStaffTravelGroundWorker.makeStaffRetract(level, user, data);
        } else {
            if (!data.isModeActive()) {
                CatStaffTravelGroundWorker.activateMode(level, user);
            } else { // If retracting
                CatStaffTravelGroundWorker.stopStaffRetraction(level, user, data);
            }
        }
    }

    public static void tick(Level level, Entity user, CatStaffItem.Mode mode) {
        if (mode != CatStaffItem.Mode.TRAVEL && user.hasData(MineraculousAttachmentTypes.newTRAVELING_CAT_STAFF)) {
            newTravelingCatStaffData.remove(user);
        }

        newTravelingCatStaffData originalData = user.getData(MineraculousAttachmentTypes.newTRAVELING_CAT_STAFF);
        newTravelingCatStaffData data = originalData;

        data = CatStaffTravelGroundWorker.updateSafeFallTicks(user, data);
        if (data.isModeActive()) {
            System.out.println(data);
            data = CatStaffTravelGroundWorker.updateStaffExtremities(user, data);
            if (!data.anchored() && !data.retracting()) {
                data = CatStaffTravelGroundWorker.increaseStaffLength(level, data);
            }
            if (data.safeFallTick() <= 5 && data.retracting()) {
                data = CatStaffTravelGroundWorker.decreaseStaffLength(user, data);
            }
            if (data.retracting() && data.staffLength() <= CatStaffItem.getMinStaffLength(user) + 1) {// || collision
                data = data.withEnabled(false);
                // if collision apply damage to user
            }
            boolean justAnchored = data.anchored() && !originalData.anchored();
            if (justAnchored) {
                data = CatStaffTravelGroundWorker.launchUser(user, data);
            }
        }
        // IF NOT ANCHORED && NOT RETRACTING -> EXTEND AT THE ORIGIN UNTIL IT ANCHORS
        // WHEN ANCHORING IT LAUNCH THE PLAYER AND START RETRACTION
        // WHEN USER COLLIDES enable = false AND APPLY DAMAGE
        // WHEN RETRACTING AND LENGTH IS SMALL -> ENABLE = FALSE
        originalData.update(user, data);
    }
}
