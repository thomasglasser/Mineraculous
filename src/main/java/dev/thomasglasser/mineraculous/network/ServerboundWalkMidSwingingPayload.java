package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.mineraculous.world.level.storage.ThrownLadybugYoyoData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
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

    // ON SERVER
    @Override
    public void handle(Player player) {
        Level level = player.level();
        ThrownLadybugYoyoData data = player.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO);
        ThrownLadybugYoyo thrownYoyo = data.getThrownYoyo(level);
        if (thrownYoyo != null) {
            Vec3 fromProjectileToPlayer = new Vec3(player.getX() - thrownYoyo.getX(), player.getY() - thrownYoyo.getY(), player.getZ() - thrownYoyo.getZ());
            Vec3 fromPlayerToProjectile = new Vec3(fromProjectileToPlayer.scale(-1).toVector3f());
            double distance = fromProjectileToPlayer.length();
            float maxRopeLn = thrownYoyo.getServerMaxRopeLength();
            Vec3 ox = new Vec3(maxRopeLn, 0, 0);
            Vec3 oy = new Vec3(0, -1 * (double) maxRopeLn, 0);
            Vec3 Y = ox.add(oy.scale(1.7)).normalize().scale(maxRopeLn);
            if (thrownYoyo.inGround() && !player.isNoGravity() && !player.getAbilities().flying) {
                if (distance >= maxRopeLn - 0.2 && !player.onGround()) {
                    if (player.getY() < Y.y + thrownYoyo.getY() + 3 && !player.isCrouching()) { //player.getY() < Y.y + thrownYoyo.getY() &&
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
                        if (movement.y + player.getY() > Y.y + thrownYoyo.getY()) {
                            double newY = movement.y - (Y.y + thrownYoyo.getY());
                            newY = newY < 0 ? 0 : newY;
                            movement = new Vec3(movement.x, newY, movement.z);
                        }
                        player.setDeltaMovement(movement);
                        player.hurtMarked = true;
                    }
                }
                if (up && distance > 2) {
                    thrownYoyo.setServerMaxRopeLength(thrownYoyo.getServerMaxRopeLength() - 0.2f);
                    thrownYoyo.updateRenderMaxRopeLength(player);
                } else if (down && distance >= maxRopeLn - 0.2) {
                    thrownYoyo.setServerMaxRopeLength(thrownYoyo.getServerMaxRopeLength() + 0.3f);
                    thrownYoyo.updateRenderMaxRopeLength(player);
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
