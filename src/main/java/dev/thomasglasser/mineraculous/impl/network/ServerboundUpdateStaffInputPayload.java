package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.impl.world.level.storage.newPerchingCatStaffData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public record ServerboundUpdateStaffInputPayload(int input) implements ExtendedPacketPayload {
    public static final Type<ServerboundUpdateStaffInputPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_update_staff_input"));
    public static final StreamCodec<ByteBuf, ServerboundUpdateStaffInputPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ServerboundUpdateStaffInputPayload::input,
            ServerboundUpdateStaffInputPayload::new);

    // helpers
    public boolean up() {
        return (input & ServerboundUpdateYoyoInputPayload.UP) != 0;
    }

    public boolean down() {
        return (input & ServerboundUpdateYoyoInputPayload.DOWN) != 0;
    }

    public boolean left() {
        return (input & ServerboundUpdateYoyoInputPayload.LEFT) != 0;
    }

    public boolean right() {
        return (input & ServerboundUpdateYoyoInputPayload.RIGHT) != 0;
    }

    public boolean jump() {
        return (input & ServerboundUpdateYoyoInputPayload.JUMP) != 0;
    }

    public boolean hasInput() {
        return up() || down() || left() || right() || jump();
    }

    @Override
    public void handle(Player player) {
        newPerchingCatStaffData perchingData = player.getData(MineraculousAttachmentTypes.newPERCHING_CAT_STAFF);
        boolean properState = perchingData.perchState() == newPerchingCatStaffData.PerchingState.STAND ||
                perchingData.perchState() == newPerchingCatStaffData.PerchingState.LAUNCH ||
                perchingData.perchState() == newPerchingCatStaffData.PerchingState.RELEASE;

        if (perchingData.enabled() && properState && hasInput()) {
            Vec3 playerToStaffHorizontal = new Vec3(perchingData.staffOrigin().x - player.getX(), 0, perchingData.staffOrigin().z - player.getZ());
            Vec3 movement = MineraculousMathUtils.getMovementVector(player, up(), down(), left(), right());
            movement = MineraculousMathUtils.projectOnCircle(playerToStaffHorizontal, movement);
            if (movement.length() > CatStaffItem.HORIZONTAL_MOVEMENT_THRESHOLD) {
                movement = movement.scale(CatStaffItem.HORIZONTAL_MOVEMENT_SCALE);
            }
            movement = player.getDeltaMovement().multiply(0, 1, 0).add(movement);
            player.setDeltaMovement(movement);
            player.hurtMarked = true;
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
