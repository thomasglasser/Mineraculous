package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record ServerboundKamikotizationTransformPayload(Optional<UUID> target, KamikotizationData data, boolean transform) implements ExtendedPacketPayload {

    public static final Type<ServerboundKamikotizationTransformPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_kamikotization_transform"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundKamikotizationTransformPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString)), ServerboundKamikotizationTransformPayload::target,
            KamikotizationData.STREAM_CODEC, ServerboundKamikotizationTransformPayload::data,
            ByteBufCodecs.BOOL, ServerboundKamikotizationTransformPayload::transform,
            ServerboundKamikotizationTransformPayload::new);
    public ServerboundKamikotizationTransformPayload(KamikotizationData data, boolean transform) {
        this(Optional.empty(), data, transform);
    }

    // ON SERVER
    @Override
    public void handle(@Nullable Player player) {
        if (target.isPresent())
            player = player.level().getPlayerByUUID(target.get());
        MineraculousEntityEvents.handleKamikotizationTransformation((ServerPlayer) player, data, transform);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
