package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedSuitLookData;
import dev.thomasglasser.mineraculous.world.level.storage.SuitLookData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ClientboundSyncSuitLookPayload(UUID targetId, Holder<Miraculous> miraculous, FlattenedSuitLookData data) implements ExtendedPacketPayload {

    public static final Type<ClientboundSyncSuitLookPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_sync_suit_look"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncSuitLookPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ClientboundSyncSuitLookPayload::targetId,
            Miraculous.STREAM_CODEC, ClientboundSyncSuitLookPayload::miraculous,
            FlattenedSuitLookData.CODEC, ClientboundSyncSuitLookPayload::data,
            ClientboundSyncSuitLookPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        Player target = player.level().getPlayerByUUID(targetId);
        if (target != null) {
            SuitLookData suitLookData = data.unpack(miraculous.getKey(), target);
            if (suitLookData != null) {
                target.getData(MineraculousAttachmentTypes.MIRACULOUS_SUIT_LOOKS).put(miraculous, data.look(), suitLookData);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
