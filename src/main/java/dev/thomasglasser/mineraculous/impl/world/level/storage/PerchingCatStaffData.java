package dev.thomasglasser.mineraculous.impl.world.level.storage;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * This record is meant to hold data related to cat staff perch mode.
 * See CatStaffPerchCommander to see how the perch mode works.
 * This record does not get any additional context related to the game.
 *
 * @param isModeActive                    Weather or not the perch mode is active.
 * @param state                           The current state of the mode, see CatStaffPerchCommander for more details
 * @param staffOrigin                     The lower extremity's coordinates of the staff.
 * @param staffTip                        The upper extremity's coordinates of the staff.
 * @param userPositionBeforeLeanOrRelease The coordinates of the player before transitioning to LEAN/RELEASE state
 * @param onGround                        Weather or not the staff is anchored.
 * @param userGravity                     Weather or not the user should be affected by gravity.
 * @param verticalMovement                The movement of the player during STAND state.
 * @param pawDirection                    The direction the user was facing when perch mode became active.
 */

public record PerchingCatStaffData(
        ItemStack stack,
        boolean isModeActive,
        PerchingState state,
        Vec3 staffOrigin,
        Vec3 staffTip,
        Vec3 userPositionBeforeLeanOrRelease,
        boolean onGround,
        boolean userGravity,
        VerticalMovement verticalMovement,
        Direction pawDirection) {

    public static final StreamCodec<RegistryFriendlyByteBuf, PerchingCatStaffData> STREAM_CODEC = TommyLibExtraStreamCodecs.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, PerchingCatStaffData::stack,
            ByteBufCodecs.BOOL, PerchingCatStaffData::isModeActive,
            PerchingState.STREAM_CODEC, PerchingCatStaffData::state,
            TommyLibExtraStreamCodecs.VEC_3, PerchingCatStaffData::staffOrigin,
            TommyLibExtraStreamCodecs.VEC_3, PerchingCatStaffData::staffTip,
            TommyLibExtraStreamCodecs.VEC_3, PerchingCatStaffData::userPositionBeforeLeanOrRelease,
            ByteBufCodecs.BOOL, PerchingCatStaffData::onGround,
            ByteBufCodecs.BOOL, PerchingCatStaffData::userGravity,
            VerticalMovement.STREAM_CODEC, PerchingCatStaffData::verticalMovement,
            Direction.STREAM_CODEC, PerchingCatStaffData::pawDirection,
            PerchingCatStaffData::new);

    public PerchingCatStaffData() {
        this(ItemStack.EMPTY, false, PerchingState.LAUNCH, Vec3.ZERO, Vec3.ZERO, Vec3.ZERO, false, true, VerticalMovement.NEUTRAL, Direction.NORTH);
    }

    public PerchingCatStaffData {
        if (staffTip.y < staffOrigin.y) {
            throw new IllegalStateException(
                    "Cat Staff has impossible geometry:\n" + this);
        }
    }

    public double staffLength() {
        return staffTip.subtract(staffOrigin).length();
    }

    public PerchingCatStaffData withStaffLength(double newLength, boolean tip) {
        Vec3 extendedExtremity;
        PerchingCatStaffData toReturn;
        if (tip) {
            Vec3 staffUnit = staffTip.subtract(staffOrigin).normalize();
            extendedExtremity = staffUnit.scale(newLength).add(staffOrigin);
            toReturn = withStaffTip(extendedExtremity);
        } else {
            Vec3 staffUnit = staffOrigin.subtract(staffTip).normalize();
            extendedExtremity = staffUnit.scale(newLength).add(staffTip);
            toReturn = withStaffOrigin(extendedExtremity);
        }
        return toReturn;
    }

    public PerchingCatStaffData withVerticalMovement(VerticalMovement newVerticalMovement) {
        return new PerchingCatStaffData(stack, isModeActive, state, staffOrigin, staffTip, userPositionBeforeLeanOrRelease, onGround, userGravity, newVerticalMovement, pawDirection);
    }

    public PerchingCatStaffData withStaffTipY(double y) {
        return withStaffTip(new Vec3(staffTip.x, y, staffTip.z));
    }

    public PerchingCatStaffData withStaffTip(Vec3 newStaffTip) {
        return new PerchingCatStaffData(stack, isModeActive, state, staffOrigin, newStaffTip, userPositionBeforeLeanOrRelease, onGround, userGravity, verticalMovement, pawDirection);
    }

    public PerchingCatStaffData withStaffOriginY(double y) {
        return withStaffOrigin(new Vec3(staffOrigin.x, y, staffOrigin.z));
    }

    public PerchingCatStaffData withStaffOrigin(Vec3 newStaffOrigin) {
        return new PerchingCatStaffData(stack, isModeActive, state, newStaffOrigin, staffTip, userPositionBeforeLeanOrRelease, onGround, userGravity, verticalMovement, pawDirection);
    }

    public PerchingCatStaffData withGravity(boolean newGravity) {
        return new PerchingCatStaffData(stack, isModeActive, state, staffOrigin, staffTip, userPositionBeforeLeanOrRelease, onGround, newGravity, verticalMovement, pawDirection);
    }

    public PerchingCatStaffData withState(PerchingState newState) {
        return new PerchingCatStaffData(stack, isModeActive, newState, staffOrigin, staffTip, userPositionBeforeLeanOrRelease, onGround, userGravity, verticalMovement, pawDirection);
    }

    public PerchingCatStaffData withGround(boolean hitGround) {
        return new PerchingCatStaffData(stack, isModeActive, state, staffOrigin, staffTip, userPositionBeforeLeanOrRelease, hitGround, userGravity, verticalMovement, pawDirection);
    }

    public PerchingCatStaffData withEnabled(boolean enable) {
        return new PerchingCatStaffData(stack, enable, state, staffOrigin, staffTip, userPositionBeforeLeanOrRelease, onGround, userGravity, verticalMovement, pawDirection);
    }

    public PerchingCatStaffData withUserPositionBeforeLeanOrRelease(Vec3 position) {
        return new PerchingCatStaffData(stack, isModeActive, state, staffOrigin, staffTip, position, onGround, userGravity, verticalMovement, pawDirection);
    }

    public Vec3 horizontalPosition() {
        return new Vec3(staffOrigin.x, 0, staffOrigin.z);
    }

    public boolean isStaffReleaseable() {
        return state == PerchingCatStaffData.PerchingState.STAND && onGround;
    }

    public static VerticalMovement getVerticalMovement(boolean ascend, boolean descend) {
        return ascend && !descend ? VerticalMovement.ASCENDING : descend && !ascend ? VerticalMovement.DESCENDING : VerticalMovement.NEUTRAL;
    }

    public void update(Entity entity, PerchingCatStaffData newData) {
        if (this != newData) {
            newData.save(entity);
        }
    }

    public void save(Entity entity) {
        entity.setData(MineraculousAttachmentTypes.PERCHING_CAT_STAFF, this);
    }

    public static void remove(Entity entity) {
        entity.removeData(MineraculousAttachmentTypes.PERCHING_CAT_STAFF);
    }
    /**
     * LAUNCH state makes the staff extend itself downwards and rocket the user
     * into the air until it reaches the ground. The staff will never extend past
     * the limit set in server configuration for max tool length.
     * <p>
     * STAND state describes the moment when the staff is completely still and will
     * change its length only when the user uses the ascend and descend tool keybinds
     * which lately changes the VerticalMovement of the Perching State.
     * The staff length can never be negative and will always be bigger than the user's
     * height and smaller than the max tool length value set in configuration.
     * <p>
     * RELEASE state means the user slowly lets go of the staff, therefore slips and
     * descends quickly without fall damage. If they move horizontally too much, the
     * tool will retract completely, and they will get fall damage. The staff
     * can transition to RELEASE state only from STAND state and when the user
     * right-clicks the tool.
     * <p>
     * LEAN state will incline the staff causing a circular-motion fall. The user
     * won't experience damage as long as the staff has not retracted during the fall.
     * The staff can transition to LEAN state only from STAND state and when the user
     * left-clicks the tool. Jumping while falling or falling below the Y coordinate of
     * the staff's ground position will make the tool retract completely.
     */
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
        NEUTRAL;

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
                  tip          = %s
                }
                """.formatted(
                state,
                verticalMovement,
                isModeActive,
                onGround,
                userGravity,
                staffOrigin,
                staffTip);
    }
}
