package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public record ServerboundUpdateYoyoInputPayload(boolean front, boolean back, boolean left, boolean right) implements ExtendedPacketPayload {

    public static final Type<ServerboundUpdateYoyoInputPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_update_yoyo_input"));
    public static final StreamCodec<ByteBuf, ServerboundUpdateYoyoInputPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ServerboundUpdateYoyoInputPayload::front,
            ByteBufCodecs.BOOL, ServerboundUpdateYoyoInputPayload::back,
            ByteBufCodecs.BOOL, ServerboundUpdateYoyoInputPayload::left,
            ByteBufCodecs.BOOL, ServerboundUpdateYoyoInputPayload::right,
            ServerboundUpdateYoyoInputPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        ThrownLadybugYoyo thrownYoyo = player.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO).getThrownYoyo(player.level());
        if (thrownYoyo != null) {
            if (thrownYoyo.inGround() && !player.isNoGravity() && !player.getAbilities().flying) {
                float maxRopeLn = thrownYoyo.getMaxRopeLength();
                Vec3 oX = new Vec3(maxRopeLn, 0, 0);
                Vec3 oY = new Vec3(0, -1 * maxRopeLn, 0);
                double y = oX.add(oY.scale(1.7)).normalize().scale(maxRopeLn).y;
                Vec3 fromProjectileToPlayer = new Vec3(player.getX() - thrownYoyo.getX(), player.getY() - thrownYoyo.getY(), player.getZ() - thrownYoyo.getZ());
                if (fromProjectileToPlayer.length() >= maxRopeLn - 0.2 && !player.onGround()) {
                    if (player.getY() < y + thrownYoyo.getY() + 3 && !player.isCrouching()) {
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

                        movement = projectOnCircle(fromProjectileToPlayer.scale(-1), movement);
                        if (movement.y + player.getY() > y + thrownYoyo.getY()) {
                            double newY = movement.y - (y + thrownYoyo.getY());
                            newY = newY < 0 ? 0 : newY;
                            movement = new Vec3(movement.x, newY, movement.z);
                        }
                        player.setDeltaMovement(movement);
                        player.hurtMarked = true;
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
