package dev.thomasglasser.mineraculous.impl.world.level.storage;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public record TravelingCatStaffData(
        ItemStack stack,
        boolean isModeActive,
        Vec3 launchingDirection,
        Vec3 staffOrigin,
        Vec3 staffTip,
        Vec3 initialUserHorizontalDirection,
        boolean anchored,
        boolean retracting,
        boolean helicopter,
        int safeFallTick) {

    public static final StreamCodec<RegistryFriendlyByteBuf, TravelingCatStaffData> STREAM_CODEC = TommyLibExtraStreamCodecs.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, TravelingCatStaffData::stack,
            ByteBufCodecs.BOOL, TravelingCatStaffData::isModeActive,
            TommyLibExtraStreamCodecs.VEC_3, TravelingCatStaffData::launchingDirection,
            TommyLibExtraStreamCodecs.VEC_3, TravelingCatStaffData::staffOrigin,
            TommyLibExtraStreamCodecs.VEC_3, TravelingCatStaffData::staffTip,
            TommyLibExtraStreamCodecs.VEC_3, TravelingCatStaffData::initialUserHorizontalDirection,
            ByteBufCodecs.BOOL, TravelingCatStaffData::anchored,
            ByteBufCodecs.BOOL, TravelingCatStaffData::retracting,
            ByteBufCodecs.BOOL, TravelingCatStaffData::helicopter,
            ByteBufCodecs.INT, TravelingCatStaffData::safeFallTick,
            TravelingCatStaffData::new);
    public TravelingCatStaffData() {
        this(ItemStack.EMPTY, false, Vec3.ZERO, Vec3.ZERO, Vec3.ZERO, Vec3.ZERO, false, false, false, 0);
    }

    public double staffLength() {
        return staffTip.subtract(staffOrigin).length();
    }

    public TravelingCatStaffData withEnabled(boolean enabled) {
        return new TravelingCatStaffData(stack, enabled, launchingDirection, staffOrigin, staffTip, initialUserHorizontalDirection, anchored, retracting, helicopter, safeFallTick);
    }

    public TravelingCatStaffData withStaffOrigin(Vec3 origin) {
        return new TravelingCatStaffData(stack, isModeActive, launchingDirection, origin, staffTip, initialUserHorizontalDirection, anchored, retracting, helicopter, safeFallTick);
    }

    public TravelingCatStaffData withStaffTip(Vec3 tip) {
        return new TravelingCatStaffData(stack, isModeActive, launchingDirection, staffTip, tip, initialUserHorizontalDirection, anchored, retracting, helicopter, safeFallTick);
    }

    public TravelingCatStaffData withAnchored(boolean anchored) {
        return new TravelingCatStaffData(stack, isModeActive, launchingDirection, staffOrigin, staffTip, initialUserHorizontalDirection, anchored, retracting, helicopter, safeFallTick);
    }

    public TravelingCatStaffData withRetracting(boolean retracting) {
        return new TravelingCatStaffData(stack, isModeActive, launchingDirection, staffOrigin, staffTip, initialUserHorizontalDirection, anchored, retracting, helicopter, safeFallTick);
    }

    public TravelingCatStaffData withSafeFallTick(int safeFallTick) {
        return new TravelingCatStaffData(stack, isModeActive, launchingDirection, staffOrigin, staffTip, initialUserHorizontalDirection, anchored, retracting, helicopter, safeFallTick);
    }

    public TravelingCatStaffData withHelicopter(boolean helicopter) {
        return new TravelingCatStaffData(stack, isModeActive, launchingDirection, staffOrigin, staffTip, initialUserHorizontalDirection, anchored, retracting, helicopter, safeFallTick);
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
