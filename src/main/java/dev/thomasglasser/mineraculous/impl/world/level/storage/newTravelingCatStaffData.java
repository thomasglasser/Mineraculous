package dev.thomasglasser.mineraculous.impl.world.level.storage;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record newTravelingCatStaffData(
        boolean isModeActive,
        Vec3 launchingDirection,
        Vec3 staffOrigin,
        Vec3 staffTip,
        Vec3 initialUserHorizontalDirection,
        boolean anchored,
        boolean retracting,
        int safeFallTick) {

    public static final StreamCodec<ByteBuf, newTravelingCatStaffData> STREAM_CODEC = TommyLibExtraStreamCodecs.composite(
            ByteBufCodecs.BOOL, newTravelingCatStaffData::isModeActive,
            TommyLibExtraStreamCodecs.VEC_3, newTravelingCatStaffData::launchingDirection,
            TommyLibExtraStreamCodecs.VEC_3, newTravelingCatStaffData::staffOrigin,
            TommyLibExtraStreamCodecs.VEC_3, newTravelingCatStaffData::staffTip,
            TommyLibExtraStreamCodecs.VEC_3, newTravelingCatStaffData::initialUserHorizontalDirection,
            ByteBufCodecs.BOOL, newTravelingCatStaffData::anchored,
            ByteBufCodecs.BOOL, newTravelingCatStaffData::retracting,
            ByteBufCodecs.INT, newTravelingCatStaffData::safeFallTick,
            newTravelingCatStaffData::new);
    public newTravelingCatStaffData() {
        this(false, Vec3.ZERO, Vec3.ZERO, Vec3.ZERO, Vec3.ZERO, false, false, 0);
    }

    public double staffLength() {
        return staffTip.subtract(staffOrigin).length();
    }

    public newTravelingCatStaffData withEnabled(boolean enabled) {
        return new newTravelingCatStaffData(enabled, launchingDirection, staffOrigin, staffTip, initialUserHorizontalDirection, anchored, retracting, safeFallTick);
    }

    public newTravelingCatStaffData withLaunchingDirection(Vec3 direction) {
        return new newTravelingCatStaffData(isModeActive, direction, staffOrigin, staffTip, initialUserHorizontalDirection, anchored, retracting, safeFallTick);
    }

    public newTravelingCatStaffData withStaffOrigin(Vec3 origin) {
        return new newTravelingCatStaffData(isModeActive, launchingDirection, origin, staffTip, initialUserHorizontalDirection, anchored, retracting, safeFallTick);
    }

    public newTravelingCatStaffData withStaffTip(Vec3 tip) {
        return new newTravelingCatStaffData(isModeActive, launchingDirection, staffTip, tip, initialUserHorizontalDirection, anchored, retracting, safeFallTick);
    }

    public newTravelingCatStaffData withHorizontalDirection(Vec3 direction) {
        return new newTravelingCatStaffData(isModeActive, launchingDirection, staffTip, staffTip, direction, anchored, retracting, safeFallTick);
    }

    public newTravelingCatStaffData withAnchored(boolean anchored) {
        return new newTravelingCatStaffData(isModeActive, launchingDirection, staffOrigin, staffTip, initialUserHorizontalDirection, anchored, retracting, safeFallTick);
    }

    public newTravelingCatStaffData withRetracting(boolean retracting) {
        return new newTravelingCatStaffData(isModeActive, launchingDirection, staffOrigin, staffTip, initialUserHorizontalDirection, anchored, retracting, safeFallTick);
    }

    public newTravelingCatStaffData withSafeFallTick(int safeFallTick) {
        return new newTravelingCatStaffData(isModeActive, launchingDirection, staffOrigin, staffTip, initialUserHorizontalDirection, anchored, retracting, safeFallTick);
    }

    public void save(Entity entity) {
        entity.setData(MineraculousAttachmentTypes.newTRAVELING_CAT_STAFF, this);
    }

    public static void remove(Entity entity) {
        entity.removeData(MineraculousAttachmentTypes.newTRAVELING_CAT_STAFF);
    }

    public void update(Entity entity, newTravelingCatStaffData newData) {
        if (this != newData) {
            newData.save(entity);
        }
    }

    @Override
    public String toString() {
        return """
                CatStaffData {
                  enabled      = %s
                  length       = %s
                  launchingDir = %s
                  origin       = %s
                  tip          = %s
                  initialHoriz = %s
                  anchored     = %s
                  tick         = %s
                }
                """.formatted(
                isModeActive,
                staffLength(),
                launchingDirection,
                staffOrigin,
                staffTip,
                initialUserHorizontalDirection,
                anchored,
                retracting,
                safeFallTick);
    }
}
