package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record ServerboundWalkMidSwingingPayload(boolean front, boolean back, boolean left, boolean right, boolean up, boolean down) implements ExtendedPacketPayload {

    public static final Type<ServerboundWalkMidSwingingPayload> TYPE = new Type<>(Mineraculous.modLoc("walk_mid_swinging"));
    public static final StreamCodec<ByteBuf, ServerboundWalkMidSwingingPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ServerboundWalkMidSwingingPayload::front,
            ByteBufCodecs.BOOL, ServerboundWalkMidSwingingPayload::back,
            ByteBufCodecs.BOOL, ServerboundWalkMidSwingingPayload::left,
            ByteBufCodecs.BOOL, ServerboundWalkMidSwingingPayload::right,
            ByteBufCodecs.BOOL, ServerboundWalkMidSwingingPayload::up,
            ByteBufCodecs.BOOL, ServerboundWalkMidSwingingPayload::down,
            ServerboundWalkMidSwingingPayload::new);
    @Override
    public void handle(Player player) {
        Optional<UUID> uuid = player.getData(MineraculousAttachmentTypes.LADYBUG_YOYO);
        if (uuid.isPresent()) {
            Level level = player.level();
            if (level instanceof ServerLevel serverLevel && serverLevel.getEntity(uuid.get()) instanceof ThrownLadybugYoyo thrownLadybugYoyo) {
                Vec3 fromProjectileToPlayer = new Vec3(player.getX() - thrownLadybugYoyo.getX(), player.getY() - thrownLadybugYoyo.getY(), player.getZ() - thrownLadybugYoyo.getZ());
                Vec3 fromPlayerToProjectile = new Vec3(fromProjectileToPlayer.scale(-1).toVector3f());
                double distance = fromProjectileToPlayer.length();
                float maxRopeLn = thrownLadybugYoyo.getServerMaxRopeLength();
                Vec3 ox = new Vec3(maxRopeLn, 0, 0);
                Vec3 oy = new Vec3(0, -1 * (double) maxRopeLn, 0);
                Vec3 Y = ox.add(oy).normalize().scale(maxRopeLn);
                if (thrownLadybugYoyo.inGround() && !player.isNoGravity() && !player.getAbilities().flying) {
                    if (distance >= maxRopeLn - 0.2 && !player.onGround()) {
                        if (player.getY() < Y.y + thrownLadybugYoyo.getY() + 3 && !player.isCrouching()) { //player.getY() < Y.y + thrownLadybugYoyo.getY() &&
                            Vec3 movement = new Vec3(0, 0, 0);
                            if (front) {
                                Vec3 up = new Vec3(player.getLookAngle().normalize().x, 0, player.getLookAngle().normalize().z);
                                up = up.normalize();
                                movement = movement.add(up);
                            }
                            if (back) {
                                Vec3 down = new Vec3(player.getLookAngle().normalize().x, 0, player.getLookAngle().normalize().z);
                                down = down.scale(-1d);
                                down = down.normalize();
                                movement = movement.add(down);
                            }
                            if (left) {
                                Vec3 vertical = new Vec3(0, 1, 0);
                                Vec3 up = new Vec3(player.getLookAngle().normalize().x, 0, player.getLookAngle().normalize().z);
                                up.normalize();
                                Vec3 left = new Vec3(vertical.cross(up).toVector3f());
                                left = left.normalize();
                                movement = movement.add(left);
                            }
                            if (right) {
                                Vec3 vertical = new Vec3(0, 1, 0);
                                Vec3 up = new Vec3(player.getLookAngle().normalize().x, 0, player.getLookAngle().normalize().z);
                                up = up.normalize();
                                Vec3 right = new Vec3(up.cross(vertical).toVector3f());
                                right = right.normalize();
                                movement = movement.add(right);
                            }
                            movement = movement.normalize();
                            movement.scale(0.2);

                            movement = projectOnCircle(fromPlayerToProjectile, movement);
                            if (movement.y + player.getY() > Y.y + thrownLadybugYoyo.getY()) {
                                double newY = movement.y - (Y.y + thrownLadybugYoyo.getY());
                                newY = newY < 0 ? 0 : newY;
                                movement = new Vec3(movement.x, newY, movement.z);
                                Mineraculous.LOGGER.info(String.valueOf(movement.y));
                            }
                            //Mineraculous.LOGGER.info(String.valueOf(Y.y + thrownLadybugYoyo.getY()));
                            player.setDeltaMovement(movement);
                            player.hurtMarked = true;
                        }
                    }
                    if (up && distance > 2) {
                        thrownLadybugYoyo.setServerMaxRopeLength(thrownLadybugYoyo.getServerMaxRopeLength() - 0.2f);
                        thrownLadybugYoyo.updateRenderMaxRopeLength(player);
                    } else if (down) {
                        thrownLadybugYoyo.setServerMaxRopeLength(thrownLadybugYoyo.getServerMaxRopeLength() + 0.3f);
                        thrownLadybugYoyo.updateRenderMaxRopeLength(player);
                        Vec3 constrainedPosition = player.position()
                                .add(fromProjectileToPlayer.normalize().scale(distance - thrownLadybugYoyo.getServerMaxRopeLength()));
                        thrownLadybugYoyo.normalCollisions(false, player);
                        if (player.level().isEmptyBlock(new BlockPos((int) constrainedPosition.x, (int) (constrainedPosition.y + 0.5), (int) constrainedPosition.z))) {
                            player.setPos(constrainedPosition.x, constrainedPosition.y, constrainedPosition.z);
                        }
                    }
                }
            }
        }
    }

    private Vec3 projectOnCircle(Vec3 fromPointToCenter, Vec3 vec3) {
        Vec3 crossProd = fromPointToCenter.cross(vec3);
        Vec3 t = crossProd.cross(fromPointToCenter);

        double cosTheta = t.dot(vec3) / (t.length() * vec3.length());
        double tln = cosTheta * vec3.length();
        t = t.normalize().scale(tln);

        return t;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
