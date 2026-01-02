package dev.thomasglasser.mineraculous.impl.world.level.storage;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record newPerchingCatStaffData(
        PerchingState perchState,
        VerticalMovement verticalMovement,
        Direction pawDirection,
        Vec3 userPositionBeforeFall,
        Vec3 staffOrigin,
        Vec3 staffHead,
        boolean enabled,
        boolean userGravity) {

    public static final StreamCodec<ByteBuf, newPerchingCatStaffData> STREAM_CODEC = TommyLibExtraStreamCodecs.composite(
            PerchingState.STREAM_CODEC, newPerchingCatStaffData::perchState,
            VerticalMovement.STREAM_CODEC, newPerchingCatStaffData::verticalMovement,
            Direction.STREAM_CODEC, newPerchingCatStaffData::pawDirection,
            TommyLibExtraStreamCodecs.VEC_3, newPerchingCatStaffData::userPositionBeforeFall,
            TommyLibExtraStreamCodecs.VEC_3, newPerchingCatStaffData::staffOrigin,
            TommyLibExtraStreamCodecs.VEC_3, newPerchingCatStaffData::staffHead,
            ByteBufCodecs.BOOL, newPerchingCatStaffData::enabled,
            ByteBufCodecs.BOOL, newPerchingCatStaffData::userGravity,
            newPerchingCatStaffData::new);

    public newPerchingCatStaffData() {
        this(PerchingState.LAUNCH, VerticalMovement.NETURAL, Direction.NORTH, Vec3.ZERO, Vec3.ZERO, Vec3.ZERO, false, true);
    }

    public double getStaffLength() {
        return staffHead.subtract(staffOrigin).length();
    }

    public newPerchingCatStaffData withStaffLength(double newLength) {
        Vec3 staffUnit = staffHead.subtract(staffOrigin).normalize();
        Vec3 newHead = staffUnit.scale(newLength).add(staffOrigin);
        return withStaffHead(newHead);
    }

    public newPerchingCatStaffData withVerticalMovement(VerticalMovement newVerticalMovement) {
        return new newPerchingCatStaffData(
                perchState,
                newVerticalMovement,
                pawDirection,
                userPositionBeforeFall,
                staffOrigin,
                staffHead,
                enabled,
                userGravity);
    }

    public newPerchingCatStaffData withStaffHead(Vec3 newStaffHead) {
        return new newPerchingCatStaffData(
                perchState,
                verticalMovement,
                pawDirection,
                userPositionBeforeFall,
                staffOrigin,
                newStaffHead,
                enabled,
                userGravity);
    }

    public newPerchingCatStaffData withGravity(boolean newGravity) {
        return new newPerchingCatStaffData(
                perchState,
                verticalMovement,
                pawDirection,
                userPositionBeforeFall,
                staffOrigin,
                staffHead,
                enabled,
                newGravity);
    }

    public newPerchingCatStaffData withState(PerchingState newState) {
        return new newPerchingCatStaffData(
                newState,
                verticalMovement,
                pawDirection,
                userPositionBeforeFall,
                staffOrigin,
                staffHead,
                enabled,
                userGravity);
    }

    public static VerticalMovement getVerticalMovement(boolean ascend, boolean descend) {
        return ascend && !descend ? newPerchingCatStaffData.VerticalMovement.ASCENDING : descend && !ascend ? newPerchingCatStaffData.VerticalMovement.DESCENDING : newPerchingCatStaffData.VerticalMovement.NETURAL;
    }

    public void save(Entity entity) {
        entity.setData(MineraculousAttachmentTypes.newPERCHING_CAT_STAFF, this);
    }

    public static void remove(Entity entity) {
        entity.removeData(MineraculousAttachmentTypes.newPERCHING_CAT_STAFF);
    }
    public enum PerchingState implements StringRepresentable {
        STAND,
        LEAN,
        RELEASE,
        LAUNCH;

        public static final StreamCodec<ByteBuf, PerchingState> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(PerchingState::of, PerchingState::getSerializedName);
        public static final Codec<PerchingState> CODEC = StringRepresentable.fromEnum(PerchingState::values);

        public static PerchingState of(String name) {
            return valueOf(name.toUpperCase());
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }

    public enum VerticalMovement implements StringRepresentable {
        ASCENDING,
        DESCENDING,
        NETURAL;

        public static final StreamCodec<ByteBuf, VerticalMovement> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(VerticalMovement::of, VerticalMovement::getSerializedName);
        public static final Codec<VerticalMovement> CODEC = StringRepresentable.fromEnum(VerticalMovement::values);

        public static VerticalMovement of(String name) {
            return valueOf(name.toUpperCase());
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }
}
