package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public record ClientboundSendRightHandParticlesPayload(int id, ParticleOptions particle) implements ExtendedPacketPayload {
    public static final Type<ClientboundSendRightHandParticlesPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_send_right_hand_particles"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSendRightHandParticlesPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ClientboundSendRightHandParticlesPayload::id,
            ParticleTypes.STREAM_CODEC, ClientboundSendRightHandParticlesPayload::particle,
            ClientboundSendRightHandParticlesPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        Entity entity = player.level().getEntity(id);
        if (entity instanceof LivingEntity livingEntity) {
            Level level = livingEntity.level();
            double randomShiftForward = 1.0 / level.random.nextInt(8, 15);
            double randomShiftRight = 1.0 / level.random.nextInt(8, 15);
            double randomShiftUp = 1.0 / level.random.nextInt(15, 50);
            if (level.random.nextBoolean())
                randomShiftForward = -randomShiftForward;
            if (level.random.nextBoolean())
                randomShiftRight = -randomShiftRight;
            if (livingEntity == player && MineraculousClientUtils.isFirstPerson()) {
                renderParticlesFollowingEntity(livingEntity, particle, 0.3, 0.1 + randomShiftForward, 0.23 + randomShiftRight, -0.1 - randomShiftUp, true);
            } else {
                renderParticlesFollowingEntity(livingEntity, particle, 0, 0.1 + randomShiftForward, 0.35 + randomShiftRight, 0.6 + randomShiftUp, false);
            }

        }
    }

    private static void renderParticlesFollowingEntity(LivingEntity entity, ParticleOptions type, double distanceFromSkin, double forwardShift, double rightShift, double upShift, boolean firstPerson) {
        Vec3 angle = firstPerson ? entity.getLookAngle() : new Vec3(0, 0, 0);
        Vec3 worldUp = new Vec3(0, 1, 0);
        Vec3 localForward = Vec3.directionFromRotation(new Vec2(0, firstPerson ? entity.getYRot() : entity.yBodyRot));
        Vec3 right = localForward.cross(worldUp);
        Vec3 up = localForward.cross(right);

        double x = entity.getX() + (rightShift * right.x()) - (upShift * up.x());
        double y = entity.getY() + (rightShift * right.y()) - (upShift * up.y());
        double z = entity.getZ() + (rightShift * right.z()) - (upShift * up.z());

        if (firstPerson) {
            x += (distanceFromSkin * angle.x());
            y += entity.getEyeHeight() + (distanceFromSkin * angle.y());
            z += (distanceFromSkin * angle.z());
        } else {
            Vec3 forward = localForward.scale(forwardShift);
            x += forward.x();
            y += forward.y();
            z += forward.z();
        }

        entity.level().addParticle(type, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    @Override
    public Type<? extends ExtendedPacketPayload> type() {
        return TYPE;
    }
}
