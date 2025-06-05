package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedMiraculousLookData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousLookData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

public record ClientboundSyncMiraculousLookPayload(UUID targetId, ResourceKey<Miraculous> miraculous, FlattenedMiraculousLookData data) implements ExtendedPacketPayload {

    public static final Type<ClientboundSyncMiraculousLookPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_sync_miraculous_look"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncMiraculousLookPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ClientboundSyncMiraculousLookPayload::targetId,
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), ClientboundSyncMiraculousLookPayload::miraculous,
            FlattenedMiraculousLookData.CODEC, ClientboundSyncMiraculousLookPayload::data,
            ClientboundSyncMiraculousLookPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        Player target = player.level().getPlayerByUUID(targetId);
        if (target != null) {
            MiraculousLookData lookData = data.unpack(miraculous, target);
            if (lookData == null)
                return;
            target.getData(MineraculousAttachmentTypes.MIRACULOUS_MIRACULOUS_LOOKS).put(miraculous, data.look(), lookData);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
