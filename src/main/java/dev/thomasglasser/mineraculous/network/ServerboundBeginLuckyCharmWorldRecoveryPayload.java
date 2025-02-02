package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.ability.LuckyCharmWorldRecoveryAbility;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;

public record ServerboundBeginLuckyCharmWorldRecoveryPayload(AbilityData data, Optional<ParticleOptions> spreadParticle, Optional<Holder<SoundEvent>> startSound) implements ExtendedPacketPayload {

    public static final Type<ServerboundBeginLuckyCharmWorldRecoveryPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_begin_lucky_charm_world_recovery"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundBeginLuckyCharmWorldRecoveryPayload> CODEC = StreamCodec.composite(
            AbilityData.STREAM_CODEC, ServerboundBeginLuckyCharmWorldRecoveryPayload::data,
            ByteBufCodecs.optional(ParticleTypes.STREAM_CODEC), ServerboundBeginLuckyCharmWorldRecoveryPayload::spreadParticle,
            ByteBufCodecs.optional(SoundEvent.STREAM_CODEC), ServerboundBeginLuckyCharmWorldRecoveryPayload::startSound,
            ServerboundBeginLuckyCharmWorldRecoveryPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        LuckyCharmWorldRecoveryAbility.beginRecovery(data, (ServerLevel) player.level(), player.blockPosition(), player, spreadParticle, startSound);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
