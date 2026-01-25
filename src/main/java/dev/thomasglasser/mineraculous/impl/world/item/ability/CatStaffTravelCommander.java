package dev.thomasglasser.mineraculous.impl.world.item.ability;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemUtils;
import dev.thomasglasser.mineraculous.impl.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.impl.world.level.storage.TravelingCatStaffData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CatStaffTravelCommander {
    public static void itemUsed(Level level, LivingEntity user, ItemStack stack) {
        TravelingCatStaffData data = user.getData(MineraculousAttachmentTypes.TRAVELING_CAT_STAFF);
        if (data.isModeActive() && !data.retracting()) {
            CatStaffTravelGroundWorker.makeStaffRetract(level, user, data);
        } else {
            if (!data.isModeActive()) {
                CatStaffTravelGroundWorker.activateModeOrHelicopter(level, user, stack);
            } else { // If retracting
                CatStaffTravelGroundWorker.stopStaffRetraction(level, user, data);
            }
        }
    }

    public static void tick(Level level, Entity user, CatStaffItem.Mode mode) {
        TravelingCatStaffData originalData = user.getData(MineraculousAttachmentTypes.TRAVELING_CAT_STAFF);
        if (originalData.helicopter()) {
            user.resetFallDistance();
            MineraculousItemUtils.applyHelicopterSlowFall(user);
        }
        if (!level.isClientSide()) {
            if (mode != CatStaffItem.Mode.TRAVEL && user.hasData(MineraculousAttachmentTypes.TRAVELING_CAT_STAFF)) {
                TravelingCatStaffData.remove(user);
            }

            TravelingCatStaffData data = originalData;

            data = CatStaffTravelGroundWorker.updateSafeFallTicks(user, data);
            if (data.isModeActive()) {
                data = data.withHelicopter(false);
                data = CatStaffTravelGroundWorker.updateStaffExtremities(user, data);
                if (!data.anchored() && !data.retracting()) {
                    data = CatStaffTravelGroundWorker.increaseStaffLength(level, data);
                }
                boolean justAnchored = data.anchored() && !originalData.anchored();
                if (justAnchored) {
                    data = CatStaffTravelGroundWorker.launchUser(user, data);
                }
                if (data.safeFallTick() == 90) {
                    data = data.withRetracting(true);
                }
                if (data.retracting()) {
                    data = CatStaffTravelGroundWorker.decreaseStaffLength(user, data);
                }
                if (data.safeFallTick() == 20) {
                    data = data.withEnabled(false);
                }
            }
            originalData.update(user, data);
        }
    }
}
