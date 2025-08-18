package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
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
                Vec3 fromProjectileToPlayer = new Vec3(player.getX() - thrownYoyo.getX(), player.getY() - thrownYoyo.getY(), player.getZ() - thrownYoyo.getZ());
                if (fromProjectileToPlayer.length() >= maxRopeLn && !player.onGround() && player.getY() < thrownYoyo.getY()) {
                    Vec3 movement = new Vec3(0, 0, 0);

                    double yawRad = Math.toRadians(player.getYRot());
                    Vec3 forwardVec = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad));
                    Vec3 backVec = forwardVec.scale(-1);
                    Vec3 leftVec = new Vec3(forwardVec.z, 0, -forwardVec.x);
                    Vec3 rightVec = leftVec.scale(-1);

                    if (front) movement = movement.add(forwardVec);
                    if (back) movement = movement.add(backVec);
                    if (left) movement = movement.add(leftVec);
                    if (right) movement = movement.add(rightVec);

                    movement = movement.normalize();
                    movement = projectOnCircle(fromProjectileToPlayer.scale(-1), movement);

                    Vec3 ropeDir = fromProjectileToPlayer.normalize();
                    double cosAngle = ropeDir.dot(new Vec3(0, -1, 0));

                    // Pump factor: max at bottom, fades near top
                    double pumpFactor = Math.max(0.2, Math.pow(cosAngle * cosAngle, 2));
                    double inertiaBoost = pumpFactor / 10;

                    double distance = fromProjectileToPlayer.length();
                    double dampingFactor = Math.max(1.06, 1 - Math.abs(distance - thrownYoyo.getMaxRopeLength()) * 0.02); // Less damping near center
                    Vec3 dampedVelocity = movement.scale(dampingFactor);

                    Vec3 radialForce = fromProjectileToPlayer.normalize();

                    Vec3 correctiveForce = radialForce.scale((distance - thrownYoyo.getMaxRopeLength()) * 0.005);
                    Vec3 newVelocity = dampedVelocity.add(correctiveForce);

                    player.addDeltaMovement(newVelocity.scale(inertiaBoost));
                    player.hurtMarked = true;
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
