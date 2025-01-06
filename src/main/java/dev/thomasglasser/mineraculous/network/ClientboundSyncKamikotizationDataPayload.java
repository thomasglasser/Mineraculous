package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public record ClientboundSyncKamikotizationDataPayload(Optional<KamikotizationData> data, int entity) implements ExtendedPacketPayload {
    public static final Type<ClientboundSyncKamikotizationDataPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_sync_kamikotization_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncKamikotizationDataPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(KamikotizationData.STREAM_CODEC), ClientboundSyncKamikotizationDataPayload::data,
            ByteBufCodecs.INT, ClientboundSyncKamikotizationDataPayload::entity,
            ClientboundSyncKamikotizationDataPayload::new);

    public ClientboundSyncKamikotizationDataPayload(KamikotizationData data, int entity) {
        this(Optional.of(data), entity);
    }

    public ClientboundSyncKamikotizationDataPayload(int entity) {
        this(Optional.empty(), entity);
    }

    // ON CLIENT
    @Override
    public void handle(Player player) {
        if (player.level().getEntity(entity) instanceof LivingEntity livingEntity) {
            livingEntity.setData(MineraculousAttachmentTypes.KAMIKOTIZATION, data);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
