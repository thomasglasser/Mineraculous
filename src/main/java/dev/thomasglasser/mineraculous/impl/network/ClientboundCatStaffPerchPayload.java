package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.world.level.storage.PerchCatStaffData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record ClientboundCatStaffPerchPayload(int senderID, PerchCatStaffData data) implements ExtendedPacketPayload {
    public static final Type<ClientboundCatStaffPerchPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_cat_staff_perch"));
    public static final StreamCodec<ByteBuf, ClientboundCatStaffPerchPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ClientboundCatStaffPerchPayload::senderID,
            PerchCatStaffData.STREAM_CODEC, ClientboundCatStaffPerchPayload::data,
            ClientboundCatStaffPerchPayload::new);

    @Override
    public void handle(Player player) {
        Entity sender = player.level().getEntity(senderID);
        sender.setData(MineraculousAttachmentTypes.PERCH_CAT_STAFF, data);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
