package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizedMiraculousData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public record ClientboundSyncKamikotizedMiraculousDataPayload(KamikotizedMiraculousData data, int entity) implements ExtendedPacketPayload {
    public static final Type<ClientboundSyncKamikotizedMiraculousDataPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_sync_kamikotized_miraculous_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncKamikotizedMiraculousDataPayload> CODEC = StreamCodec.composite(
            KamikotizedMiraculousData.STREAM_CODEC, ClientboundSyncKamikotizedMiraculousDataPayload::data,
            ByteBufCodecs.INT, ClientboundSyncKamikotizedMiraculousDataPayload::entity,
            ClientboundSyncKamikotizedMiraculousDataPayload::new);

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
