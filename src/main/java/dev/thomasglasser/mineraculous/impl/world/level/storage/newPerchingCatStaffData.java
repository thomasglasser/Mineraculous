package dev.thomasglasser.mineraculous.impl.world.level.storage;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.world.item.CatStaffItem;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public record newPerchingCatStaffData(
        PerchingState perchState,
        VerticalMovement verticalMovement,
        Direction pawDirection,
        Vec3 userPositionBeforeFall,
        Vec3 staffOrigin,
        Vec3 staffHead, // changed only to a bigger value when in launch state
        boolean enabled,
        boolean onGround,
        boolean userGravity) {

    public static final StreamCodec<ByteBuf, newPerchingCatStaffData> STREAM_CODEC = TommyLibExtraStreamCodecs.composite(
            PerchingState.STREAM_CODEC, newPerchingCatStaffData::perchState,
            VerticalMovement.STREAM_CODEC, newPerchingCatStaffData::verticalMovement,
            Direction.STREAM_CODEC, newPerchingCatStaffData::pawDirection,
            TommyLibExtraStreamCodecs.VEC_3, newPerchingCatStaffData::userPositionBeforeFall,
            TommyLibExtraStreamCodecs.VEC_3, newPerchingCatStaffData::staffOrigin,
            TommyLibExtraStreamCodecs.VEC_3, newPerchingCatStaffData::staffHead,
            ByteBufCodecs.BOOL, newPerchingCatStaffData::enabled,
            ByteBufCodecs.BOOL, newPerchingCatStaffData::onGround,
            ByteBufCodecs.BOOL, newPerchingCatStaffData::userGravity,
            newPerchingCatStaffData::new);

    public newPerchingCatStaffData() {
        this(PerchingState.LAUNCH, VerticalMovement.NETURAL, Direction.NORTH, Vec3.ZERO, Vec3.ZERO, Vec3.ZERO, false, false, true);
        this.validate();
    }

    public double getStaffLength() {
        return staffHead.subtract(staffOrigin).length();
    }

    private void validate() {
        if (staffHead.y < staffOrigin.y) {
            throw new IllegalStateException(
                    "Cat Staff has impossible geometry:\n" + this);
        }
        if (getStaffLength() < 0) {
            throw new IllegalStateException(
                    "Cat Staff has negative length:\n" + this);
        }
    }

    public newPerchingCatStaffData withStaffLength(double newLength, boolean head) {
        Vec3 newHead;
        if (head) {
            Vec3 staffUnit = staffHead.subtract(staffOrigin).normalize();
            newHead = staffUnit.scale(newLength).add(staffOrigin);
        } else {
            Vec3 staffUnit = staffOrigin.subtract(staffHead).normalize();
            newHead = staffUnit.scale(newLength).add(staffHead);
        }
        newPerchingCatStaffData toReturn = withStaffHead(newHead);
        toReturn.validate();
        return toReturn;
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
                onGround,
                userGravity);
    }

    public newPerchingCatStaffData withStaffHeadY(double y) {
        return withStaffHead(new Vec3(staffHead.x, y, staffHead.z));
    }

    public newPerchingCatStaffData withStaffHead(Vec3 newStaffHead) {
        newPerchingCatStaffData toReturn = new newPerchingCatStaffData(
                perchState,
                verticalMovement,
                pawDirection,
                userPositionBeforeFall,
                staffOrigin,
                newStaffHead,
                enabled,
                onGround,
                userGravity);
        toReturn.validate();
        return toReturn;
    }

    public newPerchingCatStaffData withStaffOrigin(Vec3 newStaffOrigin) {
        newPerchingCatStaffData toReturn = new newPerchingCatStaffData(
                perchState,
                verticalMovement,
                pawDirection,
                userPositionBeforeFall,
                newStaffOrigin,
                staffHead,
                enabled,
                onGround,
                userGravity);
        toReturn.validate();
        return toReturn;
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
                onGround,
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
                onGround,
                userGravity);
    }

    public newPerchingCatStaffData withGround(boolean hitGround) {
        return new newPerchingCatStaffData(
                perchState,
                verticalMovement,
                pawDirection,
                userPositionBeforeFall,
                staffOrigin,
                staffHead,
                enabled,
                hitGround,
                userGravity);
    }

    public newPerchingCatStaffData withEnabled(boolean enable) {
        return new newPerchingCatStaffData(
                perchState,
                verticalMovement,
                pawDirection,
                userPositionBeforeFall,
                staffOrigin,
                staffHead,
                enable,
                onGround,
                userGravity);
    }

    public newPerchingCatStaffData release() {
        if (perchState == PerchingState.STAND && onGround) {
            return withState(PerchingState.RELEASE).withGravity(true);
        }
        return this;
    }

    public newPerchingCatStaffData launch(Vec3 userPosition, double userHeight, float yHeadRot, Vec2 horizontalFacing) {
        Vec3 staffHead = getInitializedStaffHead(userPosition, userHeight, horizontalFacing);
        Vec3 staffOrigin = getStaffOrigin(userPosition.y, staffHead);
        newPerchingCatStaffData toReturn = new newPerchingCatStaffData(
                PerchingState.LAUNCH,
                VerticalMovement.NETURAL,
                Direction.fromYRot(yHeadRot),
                userPosition,
                staffOrigin,
                staffHead,
                true,
                false,
                true);
        return toReturn;
    }

    public newPerchingCatStaffData applyGravity() {
        return withGravity(hasGravity());
    }

    public newPerchingCatStaffData updateLength(boolean airBelow, double apexStaffHeadY) {
        return switch (perchState) {
            case STAND -> updateLengthStanding();
            case LAUNCH -> updateLengthLaunching(airBelow, apexStaffHeadY);
            default -> this;
        };
    }

    private newPerchingCatStaffData updateLengthStanding() {
        double yMovement = switch (verticalMovement()) {
            case NETURAL -> 0.0;
            case ASCENDING -> CatStaffItem.USER_VERTICAL_MOVEMENT_SPEED;
            case DESCENDING -> -CatStaffItem.USER_VERTICAL_MOVEMENT_SPEED;
        };

        if (yMovement == 0.0) {
            return this;
        }

        double length = getStaffLength() + yMovement;
        double minLength = 2.0;
        double maxLength = MineraculousServerConfig.get().maxToolLength.get();
        if (length < minLength || length > maxLength) {
            return this;
        }

        return withStaffLength(length, true);
    }

    private newPerchingCatStaffData updateLengthLaunching(boolean airBelow, double apexStaffHeadY) {
        newPerchingCatStaffData result = this;

        result = result.withStaffHeadY(apexStaffHeadY);
        if (airBelow) {
            if (result.onGround()) {
                result = result.withGround(false);
            }
            double maxLength = MineraculousServerConfig.get().maxToolLength.get();
            if (result.getStaffLength() < maxLength) {
                result = result.withStaffOrigin(new Vec3(staffOrigin.x, staffOrigin.y - CatStaffItem.STAFF_GROWTH_SPEED, staffOrigin.z));
            }
        } else if (!result.onGround()) {
            result = result.withGround(true);
        }
        return result;
    }

    public newPerchingCatStaffData updateState(double userY, double staffHeadYExpectation) {
        switch (perchState) {
            case LAUNCH -> {
                boolean userFalling = staffHeadYExpectation < staffHead.y;
                if (userFalling && onGround) {
                    return withStaffHeadY(staffHeadYExpectation)
                            .withState(PerchingState.STAND)
                            .withGravity(false);
                }
            }
            case RELEASE -> {
                if (userY - staffOrigin.y < 0.5) {
                    return withEnabled(false);
                }
            }
        }
        return this;
    }

    public boolean hasGravity() {
        return perchState != PerchingState.STAND;
    }

    public boolean shouldCancelFallDamage() {
        return perchState == PerchingState.STAND ||
                perchState == PerchingState.LAUNCH ||
                perchState == PerchingState.RELEASE;
    }

    public boolean hasTetheringState() {
        return perchState == PerchingState.STAND ||
                perchState == PerchingState.RELEASE;
    }

    public static VerticalMovement getVerticalMovement(boolean ascend, boolean descend) {
        return ascend && !descend ? VerticalMovement.ASCENDING : descend && !ascend ? VerticalMovement.DESCENDING : VerticalMovement.NETURAL;
    }

    public void update(Entity entity, newPerchingCatStaffData newData) {
        if (this != newData) {
            newData.save(entity);
        }
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

    private static Vec3 getInitializedStaffHead(Vec3 userPosition, double userHeight, Vec2 horizontalFacing) {
        //Vec2 horizontalFacingVector = MineraculousMathUtils.getHorizontalFacingVector(user).scale(newCatStaffPerchHandler.DISTANCE_BETWEEN_STAFF_AND_USER);
        Vec3 staffHead = new Vec3(
                userPosition.x + horizontalFacing.x,
                userPosition.y + userHeight + CatStaffItem.STAFF_HEAD_ABOVE_USER_HEAD_OFFSET,
                userPosition.z + horizontalFacing.y);
        return staffHead;
    }

    private static Vec3 getStaffOrigin(double userY, Vec3 staffHead) {
        Vec3 staffOrigin = new Vec3(staffHead.x, userY, staffHead.z);
        return staffOrigin;
    }

    @Override
    public String toString() {
        return """
                CatStaffData {
                  state        = %s
                  verticalMove = %s
                  enabled      = %s
                  onGround     = %s
                  gravity      = %s
                  origin       = %s
                  head         = %s
                  length       = %.3f
                }
                """.formatted(
                perchState,
                verticalMovement,
                enabled,
                onGround,
                userGravity,
                staffOrigin,
                staffHead,
                getStaffLength());
    }
}
