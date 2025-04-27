package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.level.storage.TravelCatStaffData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record ClientboundCatStaffTravelPayload(int senderID, TravelCatStaffData data) implements ExtendedPacketPayload {
    public static final Type<ClientboundCatStaffTravelPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_cat_staff_travel"));
    public static final StreamCodec<ByteBuf, ClientboundCatStaffTravelPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ClientboundCatStaffTravelPayload::senderID,
            TravelCatStaffData.STREAM_CODEC, ClientboundCatStaffTravelPayload::data,
            ClientboundCatStaffTravelPayload::new);

    @Override
    public void handle(Player player) {
        Entity sender = player.level().getEntity(senderID);
        sender.setData(MineraculousAttachmentTypes.TRAVEL_CAT_STAFF, data);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
