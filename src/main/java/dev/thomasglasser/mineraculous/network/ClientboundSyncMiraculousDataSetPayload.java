package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record ClientboundSyncMiraculousDataSetPayload(MiraculousDataSet miraculousDataSet, int entity) implements ExtendedPacketPayload {
    public static final Type<ClientboundSyncMiraculousDataSetPayload> TYPE = new Type<>(Mineraculous.modLoc("sync_miraculous_data_set"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncMiraculousDataSetPayload> CODEC = StreamCodec.composite(
            MiraculousDataSet.STREAM_CODEC, ClientboundSyncMiraculousDataSetPayload::miraculousDataSet,
            ByteBufCodecs.INT, ClientboundSyncMiraculousDataSetPayload::entity,
            ClientboundSyncMiraculousDataSetPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        Entity e = player.level().getEntity(entity);
        if (e != null)
            miraculousDataSet.save(e, false);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
