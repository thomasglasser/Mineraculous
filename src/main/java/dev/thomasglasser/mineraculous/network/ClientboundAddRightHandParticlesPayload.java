package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record ClientboundAddRightHandParticlesPayload(Optional<Integer> targetId, ParticleOptions particle) implements ExtendedPacketPayload {
    public static final Type<ClientboundAddRightHandParticlesPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_add_right_hand_particles"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundAddRightHandParticlesPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.INT), ClientboundAddRightHandParticlesPayload::targetId,
            ParticleTypes.STREAM_CODEC, ClientboundAddRightHandParticlesPayload::particle,
            ClientboundAddRightHandParticlesPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        Entity target = targetId.map(id -> player.level().getEntity(id)).orElse(player);
        Vec3 handPos;
        if (target == ClientUtils.getLocalPlayer() && MineraculousClientUtils.isFirstPerson()) {
            handPos = MineraculousClientUtils.getFirstPersonHandPosition(false, true, 0.575f, -0.75f);
        } else {
            handPos = MineraculousClientUtils.getHumanoidEntityHandPos(target, false, 0, -0.9, 0.35);
        }
        Level level = player.level();
        particles(level, handPos);
        particles(level, handPos);
    }

    private void particles(Level level, Vec3 handPos) {
        double randomShiftForward = 1.0 / level.random.nextInt(8, 15);
        double randomShiftRight = 1.0 / level.random.nextInt(8, 15);
        double randomShiftUp = 1.0 / level.random.nextInt(8, 15);
        if (level.random.nextBoolean())
            randomShiftForward = -randomShiftForward;
        if (level.random.nextBoolean())
            randomShiftRight = -randomShiftRight;
        if (level.random.nextBoolean())
            randomShiftUp = -randomShiftUp;

        if (handPos != Vec3.ZERO)
            level.addParticle(particle,
                    handPos.x + randomShiftForward,
                    handPos.y + randomShiftUp,
                    handPos.z + randomShiftRight,
                    0, 0, 0);
    }

    @Override
    public Type<? extends ExtendedPacketPayload> type() {
        return TYPE;
    }
}
