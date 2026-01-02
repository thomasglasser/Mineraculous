package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.item.ability.newCatStaffPerchHandler;
import dev.thomasglasser.mineraculous.impl.world.level.storage.newPerchingCatStaffData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public record ServerboundUpdateStaffInputPayload(int input, newPerchingCatStaffData perchingData) implements ExtendedPacketPayload {
    public static final Type<ServerboundUpdateStaffInputPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_update_staff_input"));
    public static final StreamCodec<ByteBuf, ServerboundUpdateStaffInputPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ServerboundUpdateStaffInputPayload::input,
            newPerchingCatStaffData.STREAM_CODEC, ServerboundUpdateStaffInputPayload::perchingData,
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
        if (perchingData.perchState() == newPerchingCatStaffData.PerchingState.STAND) {
            if (hasInput()) {
                Vec3 playerToStaffHorizontal = new Vec3(perchingData.staffOrigin().x - player.getX(), 0, perchingData.staffOrigin().z - player.getZ());
                Vec3 movement = MineraculousMathUtils.getMovementVector(player, up(), down(), left(), right());
                movement = MineraculousMathUtils.projectOnCircle(playerToStaffHorizontal, movement);
                if (movement.length() > newCatStaffPerchHandler.HORIZONTAL_MOVEMENT_THRESHOLD) {
                    movement = movement.scale(newCatStaffPerchHandler.HORIZONTAL_MOVEMENT_SCALE);
                }
                player.setDeltaMovement(movement);
                player.hurtMarked = true;
            }
        }

        /*boolean isFalling = perchData.isFalling();
        boolean fastDescending = perchData.fastDescending();
        if (fastDescending) {
            player.hurtMarked = true;
            player.setDeltaMovement(0, player.getDeltaMovement().y, 0);
        } else {
            Vector3f initPos = perchData.initPos();
            int tick = perchData.tick();
            Vec3 movement = Vec3.ZERO;
            if (isFalling) {
                if (jump()) {
                    PerchingCatStaffData.remove(player);
                    movement = new Vec3(player.getDeltaMovement().x, 1.5, player.getDeltaMovement().z);
                }
            } else {
                Vector3f staffPosition = new Vector3f(initPos.x, 0, initPos.z);
                if (tick > CatStaffPerchHandler.MAX_TICKS && hasInput()) {
                    Vec3 staffPositionRelativeToThePlayer = new Vec3(staffPosition.x - player.getX(), 0, staffPosition.z - player.getZ());
                    movement = MineraculousMathUtils.getMovementVector(player, up(), down(), left(), right());
                    movement = MineraculousMathUtils.projectOnCircle(staffPositionRelativeToThePlayer, movement);
                    if (movement.length() > CatStaffPerchHandler.MOVEMENT_THRESHOLD)
                        movement = movement.scale(CatStaffPerchHandler.MOVEMENT_SCALE);
                }
            }
            if (!movement.equals(Vec3.ZERO)) {
                player.setDeltaMovement(movement);
                player.hurtMarked = true;
                player.resetFallDistance();
            }
        }*/
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
