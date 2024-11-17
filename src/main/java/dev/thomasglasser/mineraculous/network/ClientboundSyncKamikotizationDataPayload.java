package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public record ClientboundSyncKamikotizationDataPayload(KamikotizationData data, int entity) implements ExtendedPacketPayload {
    public static final Type<ClientboundSyncKamikotizationDataPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_sync_kamikotization_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncKamikotizationDataPayload> CODEC = StreamCodec.composite(
            KamikotizationData.STREAM_CODEC, ClientboundSyncKamikotizationDataPayload::data,
            ByteBufCodecs.INT, ClientboundSyncKamikotizationDataPayload::entity,
            ClientboundSyncKamikotizationDataPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        if (player.level().getEntity(entity) instanceof LivingEntity livingEntity)
            data.save(livingEntity, false);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
