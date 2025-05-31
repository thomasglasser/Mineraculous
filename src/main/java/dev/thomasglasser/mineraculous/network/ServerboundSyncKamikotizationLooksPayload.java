package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedKamikotizationLookData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record ServerboundSyncKamikotizationLooksPayload(UUID targetId, List<FlattenedKamikotizationLookData> looks) implements ExtendedPacketPayload {
    public static final Type<ServerboundSyncKamikotizationLooksPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_sync_kamikotization_looks"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSyncKamikotizationLooksPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ServerboundSyncKamikotizationLooksPayload::targetId,
            FlattenedKamikotizationLookData.CODEC.apply(ByteBufCodecs.list()), ServerboundSyncKamikotizationLooksPayload::looks,
            ServerboundSyncKamikotizationLooksPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        if (player.level().getPlayerByUUID(targetId) instanceof ServerPlayer target && MineraculousServerConfig.get().isCustomizationAllowed(player)) {
            TommyLibServices.NETWORK.sendToClient(new ClientboundSyncKamikotizationLooksPayload(player.getUUID(), looks), target);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
