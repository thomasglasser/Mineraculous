package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedKamikotizationLookData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationLookData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ClientboundSyncKamikotizationLookPayload(UUID targetId, FlattenedKamikotizationLookData data) implements ExtendedPacketPayload {
    public static final Type<ClientboundSyncKamikotizationLookPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_sync_kamikotization_look"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncKamikotizationLookPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ClientboundSyncKamikotizationLookPayload::targetId,
            FlattenedKamikotizationLookData.CODEC, ClientboundSyncKamikotizationLookPayload::data,
            ClientboundSyncKamikotizationLookPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        Player target = player.level().getPlayerByUUID(targetId);
        if (target != null) {
            KamikotizationLookData lookData = data.unpack(target);
            if (lookData == null)
                return;
            target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION_LOOKS).put(data.kamikotization(), lookData);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
