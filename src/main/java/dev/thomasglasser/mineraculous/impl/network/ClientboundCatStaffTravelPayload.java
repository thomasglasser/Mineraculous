package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.world.level.storage.TravelCatStaffData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record ClientboundCatStaffTravelPayload(int entityId, TravelCatStaffData data) implements ExtendedPacketPayload {
    public static final Type<ClientboundCatStaffTravelPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_cat_staff_travel"));
    public static final StreamCodec<ByteBuf, ClientboundCatStaffTravelPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ClientboundCatStaffTravelPayload::entityId,
            TravelCatStaffData.STREAM_CODEC, ClientboundCatStaffTravelPayload::data,
            ClientboundCatStaffTravelPayload::new);

    @Override
    public void handle(Player player) {
        Entity entity = player.level().getEntity(entityId);
        if (entity != null)
            entity.setData(MineraculousAttachmentTypes.TRAVEL_CAT_STAFF, data);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
