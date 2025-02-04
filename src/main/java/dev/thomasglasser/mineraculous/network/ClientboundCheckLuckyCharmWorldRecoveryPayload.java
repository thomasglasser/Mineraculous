package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;

public record ClientboundCheckLuckyCharmWorldRecoveryPayload(AbilityData data, Optional<ParticleOptions> spreadParticle, Optional<Holder<SoundEvent>> startSound) implements ExtendedPacketPayload {

    public static final Type<ClientboundCheckLuckyCharmWorldRecoveryPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_check_lucky_charm_world_recovery"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundCheckLuckyCharmWorldRecoveryPayload> CODEC = StreamCodec.composite(
            AbilityData.STREAM_CODEC, ClientboundCheckLuckyCharmWorldRecoveryPayload::data,
            ByteBufCodecs.optional(ParticleTypes.STREAM_CODEC), ClientboundCheckLuckyCharmWorldRecoveryPayload::spreadParticle,
            ByteBufCodecs.optional(SoundEvent.STREAM_CODEC), ClientboundCheckLuckyCharmWorldRecoveryPayload::startSound,
            ClientboundCheckLuckyCharmWorldRecoveryPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        if (MineraculousKeyMappings.ACTIVATE_POWER.get().isDown()) {
            TommyLibServices.NETWORK.sendToServer(new ServerboundBeginLuckyCharmWorldRecoveryPayload(data, spreadParticle, startSound));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
