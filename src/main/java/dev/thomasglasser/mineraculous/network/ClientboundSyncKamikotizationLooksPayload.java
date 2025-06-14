package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedKamikotizationLookData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationLookData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ClientboundSyncKamikotizationLooksPayload(UUID targetId, List<FlattenedKamikotizationLookData> looks) implements ExtendedPacketPayload {
    public static final Type<ClientboundSyncKamikotizationLooksPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_sync_kamikotization_looks"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncKamikotizationLooksPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ClientboundSyncKamikotizationLooksPayload::targetId,
            FlattenedKamikotizationLookData.CODEC.apply(ByteBufCodecs.list()), ClientboundSyncKamikotizationLooksPayload::looks,
            ClientboundSyncKamikotizationLooksPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        Player target = player.level().getPlayerByUUID(targetId);
        if (target != null) {
            Reference2ReferenceOpenHashMap<Holder<Kamikotization>, KamikotizationLookData> kamikotizationLooks = new Reference2ReferenceOpenHashMap<>();
            for (FlattenedKamikotizationLookData data : looks) {
                kamikotizationLooks.put(data.kamikotization(), data.unpack(target));
            }
            target.setData(MineraculousAttachmentTypes.KAMIKOTIZATION_LOOKS, kamikotizationLooks);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
