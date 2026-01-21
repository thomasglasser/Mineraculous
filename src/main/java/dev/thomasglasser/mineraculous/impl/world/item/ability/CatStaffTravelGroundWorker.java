package dev.thomasglasser.mineraculous.impl.world.item.ability;

import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.impl.world.level.storage.newTravelingCatStaffData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class CatStaffTravelGroundWorker {
    // Method for calculating expected tip relative to the user
    // Method for extending
    // Method for ray tracing
    // Method for damage calculating
    protected static void activateMode(Level level, LivingEntity user) {
        if (!level.isClientSide()) {
            Vec3 staffTip = CatStaffItem.staffTipStartup(user, false);
            Vec3 staffOrigin = CatStaffItem.staffOriginStartup(user, staffTip);
            createData(user, staffOrigin, staffTip).save(user);
        }
    }

    protected static void makeStaffRetract(Level level, LivingEntity user, newTravelingCatStaffData data) {
        if (!level.isClientSide()) {
            data.withAnchored(false).withRetracting(true).save(user);
        }
    }

    protected static void stopStaffRetraction(Level level, LivingEntity user, newTravelingCatStaffData data) {
        if (!level.isClientSide()) {
            data.withRetracting(false).save(user);
        }
    }

    private static newTravelingCatStaffData createData(LivingEntity user, Vec3 staffOrigin, Vec3 staffTip) {
        Vec2 horizontalFacing = MineraculousMathUtils.getHorizontalFacingVector(user.getYRot());
        Vec3 horizontalDirection = new Vec3(horizontalFacing.x, 0, horizontalFacing.y);
        return new newTravelingCatStaffData(
                true,
                user.getLookAngle().normalize(),
                staffTip,
                staffOrigin,
                horizontalDirection,
                false,
                false);
    }
}
