package dev.thomasglasser.mineraculous.impl.world.level.storage;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record TravelingCatStaffData(
        boolean isModeActive,
        Vec3 launchingDirection,
        Vec3 staffOrigin,
        Vec3 staffTip,
        Vec3 initialUserHorizontalDirection,
        boolean anchored,
        boolean retracting,
        int safeFallTick) {

    public static final StreamCodec<ByteBuf, TravelingCatStaffData> STREAM_CODEC = TommyLibExtraStreamCodecs.composite(
            ByteBufCodecs.BOOL, TravelingCatStaffData::isModeActive,
            TommyLibExtraStreamCodecs.VEC_3, TravelingCatStaffData::launchingDirection,
            TommyLibExtraStreamCodecs.VEC_3, TravelingCatStaffData::staffOrigin,
            TommyLibExtraStreamCodecs.VEC_3, TravelingCatStaffData::staffTip,
            TommyLibExtraStreamCodecs.VEC_3, TravelingCatStaffData::initialUserHorizontalDirection,
            ByteBufCodecs.BOOL, TravelingCatStaffData::anchored,
            ByteBufCodecs.BOOL, TravelingCatStaffData::retracting,
            ByteBufCodecs.INT, TravelingCatStaffData::safeFallTick,
            TravelingCatStaffData::new);
    public TravelingCatStaffData() {
        this(false, Vec3.ZERO, Vec3.ZERO, Vec3.ZERO, Vec3.ZERO, false, false, 0);
    }

    public double staffLength() {
        return staffTip.subtract(staffOrigin).length();
    }

    public TravelingCatStaffData withEnabled(boolean enabled) {
        return new TravelingCatStaffData(enabled, launchingDirection, staffOrigin, staffTip, initialUserHorizontalDirection, anchored, retracting, safeFallTick);
    }

    public TravelingCatStaffData withLaunchingDirection(Vec3 direction) {
        return new TravelingCatStaffData(isModeActive, direction, staffOrigin, staffTip, initialUserHorizontalDirection, anchored, retracting, safeFallTick);
    }

    public TravelingCatStaffData withStaffOrigin(Vec3 origin) {
        return new TravelingCatStaffData(isModeActive, launchingDirection, origin, staffTip, initialUserHorizontalDirection, anchored, retracting, safeFallTick);
    }

    public TravelingCatStaffData withStaffTip(Vec3 tip) {
        return new TravelingCatStaffData(isModeActive, launchingDirection, staffTip, tip, initialUserHorizontalDirection, anchored, retracting, safeFallTick);
    }

    public TravelingCatStaffData withHorizontalDirection(Vec3 direction) {
        return new TravelingCatStaffData(isModeActive, launchingDirection, staffTip, staffTip, direction, anchored, retracting, safeFallTick);
    }

    public TravelingCatStaffData withAnchored(boolean anchored) {
        return new TravelingCatStaffData(isModeActive, launchingDirection, staffOrigin, staffTip, initialUserHorizontalDirection, anchored, retracting, safeFallTick);
    }

    public TravelingCatStaffData withRetracting(boolean retracting) {
        return new TravelingCatStaffData(isModeActive, launchingDirection, staffOrigin, staffTip, initialUserHorizontalDirection, anchored, retracting, safeFallTick);
    }

    public TravelingCatStaffData withSafeFallTick(int safeFallTick) {
        return new TravelingCatStaffData(isModeActive, launchingDirection, staffOrigin, staffTip, initialUserHorizontalDirection, anchored, retracting, safeFallTick);
    }

    public void save(Entity entity) {
        entity.setData(MineraculousAttachmentTypes.TRAVELING_CAT_STAFF, this);
    }

    public static void remove(Entity entity) {
        entity.removeData(MineraculousAttachmentTypes.TRAVELING_CAT_STAFF);
    }

    public void update(Entity entity, TravelingCatStaffData newData) {
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
                  initialHoriz = %s
                  anchored     = %s
                  retracting   = %s
                  tick         = %s
                }
                """.formatted(
                isModeActive,
                staffLength(),
                launchingDirection,
                initialUserHorizontalDirection,
                anchored,
                retracting,
                safeFallTick);
    }
}
