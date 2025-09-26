package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.mineraculous.impl.world.level.storage.ThrownLadybugYoyoData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record ServerboundUpdateYoyoInputPayload(int input) implements ExtendedPacketPayload {
    public static final Type<ServerboundUpdateYoyoInputPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_update_yoyo_input"));
    public static final StreamCodec<ByteBuf, ServerboundUpdateYoyoInputPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundUpdateYoyoInputPayload::input,
            ServerboundUpdateYoyoInputPayload::new);

    // bit masks
    public static final int UP = 1 << 0;
    public static final int DOWN = 1 << 1;
    public static final int LEFT = 1 << 2;
    public static final int RIGHT = 1 << 3;
    public static final int JUMP = 1 << 4;

    // helpers
    public boolean up() {
        return (input & UP) != 0;
    }

    public boolean down() {
        return (input & DOWN) != 0;
    }

    public boolean left() {
        return (input & LEFT) != 0;
    }

    public boolean right() {
        return (input & RIGHT) != 0;
    }

    public boolean jump() {
        return (input & JUMP) != 0;
    }

    // ON SERVER
    @Override
    public void handle(Player player) {
        Level level = player.level();
        ThrownLadybugYoyoData data = player.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO);
        ThrownLadybugYoyo thrownYoyo = data.getThrownYoyo(level);
        if (thrownYoyo != null) {
            Vec3 fromProjectileToPlayer = new Vec3(
                    player.getX() - thrownYoyo.getX(),
                    player.getY() - thrownYoyo.getY(),
                    player.getZ() - thrownYoyo.getZ());
            double distance = fromProjectileToPlayer.length();
            float maxRopeLn = thrownYoyo.getMaxRopeLength();
            if (inTension(thrownYoyo, player, distance, maxRopeLn)) {
                if (jump()) { //JUMP
                    Vec3 projection = new Vec3(fromProjectileToPlayer.x, 0, fromProjectileToPlayer.z);
                    double cosAngle = fromProjectileToPlayer.normalize().dot(projection.normalize());
                    if (fromProjectileToPlayer.y > 0 && cosAngle > 0.8d) {
                        player.addDeltaMovement(
                                new Vec3(player.getDeltaMovement().x, 1.2d,
                                        player.getDeltaMovement().z));
                        player.hurtMarked = true;
                        data.startSafeFall().save(player, true);
                        thrownYoyo.recall();
                    }
                } else if (up() || down() || left() || right()) { //WASD
                    if (player.getY() < thrownYoyo.getY()) {

                        Vec3 movement = MineraculousMathUtils.getMovementVector(player, up(), down(), left(), right());
                        movement = MineraculousMathUtils.projectOnCircle(fromProjectileToPlayer.scale(-1), movement);

                        Vec3 ropeDir = fromProjectileToPlayer.normalize();
                        double cosAngle = ropeDir.dot(new Vec3(0, -1, 0));

                        // Pump factor: max at bottom, fades near top
                        double pumpFactor = Math.max(0.2, Math.pow(cosAngle * cosAngle, 2));
                        double inertiaBoost = pumpFactor / 10;

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
    }

    private static boolean inTension(ThrownLadybugYoyo thrownYoyo, Player player, double distance, double maxRopeLn) {
        boolean yoyoAnchored = thrownYoyo.inGround();
        boolean playerAffected = !player.isNoGravity() && !player.getAbilities().flying;
        boolean ropeTensioned = distance > maxRopeLn && maxRopeLn > 3;
        boolean playerAirborne = !player.onGround();
        boolean noBlockBelow = !player.level().getBlockState(
                new BlockPos(
                        (int) player.getX(),
                        (int) player.getY() - 1,
                        (int) player.getZ()))
                .isSolid();

        return yoyoAnchored &&
                playerAffected &&
                ropeTensioned &&
                playerAirborne &&
                noBlockBelow;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
