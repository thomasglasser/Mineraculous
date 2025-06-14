package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public record ServerboundStartKamikotizationDetransformationPayload(Optional<UUID> targetId, KamikotizationData data, boolean instant) implements ExtendedPacketPayload {

    public static final Type<ServerboundStartKamikotizationDetransformationPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_start_kamikotization_detransformation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundStartKamikotizationDetransformationPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), ServerboundStartKamikotizationDetransformationPayload::targetId,
            KamikotizationData.STREAM_CODEC, ServerboundStartKamikotizationDetransformationPayload::data,
            ByteBufCodecs.BOOL, ServerboundStartKamikotizationDetransformationPayload::instant,
            ServerboundStartKamikotizationDetransformationPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        Player target = targetId.map(id -> player.level().getPlayerByUUID(id)).orElse(player);
        if (target != null) {
            data.detransform(player, (ServerLevel) player.level(), player.position().add(0, 1, 0), instant);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
