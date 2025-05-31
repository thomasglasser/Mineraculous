package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.renderer.entity.ThrownLadybugYoyoRenderer;
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
            // TODO: Improve particle positioning
            Vec3 handPos = ThrownLadybugYoyoRenderer.getPlayerHandPos(player, 0, 0, false);
            entity.level().addParticle(particle, handPos.x + 0.1 + randomShiftForward, handPos.y + -0.1 - randomShiftUp, handPos.z + 0.23 + randomShiftRight, 0, 0, 0);
        }
    }

    @Override
    public Type<? extends ExtendedPacketPayload> type() {
        return TYPE;
    }
}
