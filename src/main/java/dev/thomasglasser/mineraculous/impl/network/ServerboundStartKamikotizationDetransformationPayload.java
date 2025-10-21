package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public record ServerboundStartKamikotizationDetransformationPayload(Optional<UUID> targetId, boolean instant) implements ExtendedPacketPayload {
    public static final Type<ServerboundStartKamikotizationDetransformationPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_start_kamikotization_detransformation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundStartKamikotizationDetransformationPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), ServerboundStartKamikotizationDetransformationPayload::targetId,
            ByteBufCodecs.BOOL, ServerboundStartKamikotizationDetransformationPayload::instant,
            ServerboundStartKamikotizationDetransformationPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        ServerLevel level = (ServerLevel) player.level();
        LivingEntity target;
        if (targetId.isPresent()) {
            target = level.getEntity(targetId.get()) instanceof LivingEntity l ? l : null;
        } else {
            target = player;
        }
        if (target != null) {
            target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> data.detransform(target, level, target.position().add(0, 1, 0), instant));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
