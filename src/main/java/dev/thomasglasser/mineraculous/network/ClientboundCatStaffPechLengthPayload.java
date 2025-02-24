package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ClientboundCatStaffPechLengthPayload(float value, UUID senderID) implements ExtendedPacketPayload {
    public static final Type<ClientboundCatStaffPechLengthPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_cat_staff_perch_length"));
    public static final StreamCodec<ByteBuf, ClientboundCatStaffPechLengthPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ClientboundCatStaffPechLengthPayload::value,
            UUIDUtil.STREAM_CODEC, ClientboundCatStaffPechLengthPayload::senderID,
            ClientboundCatStaffPechLengthPayload::new);

    @Override
    public void handle(Player player) {
        Player sender = player.level().getPlayerByUUID(senderID);
        if (sender != null)
            sender.setData(MineraculousAttachmentTypes.CAT_STAFF_PERCH_LENGTH, Optional.of(value));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
