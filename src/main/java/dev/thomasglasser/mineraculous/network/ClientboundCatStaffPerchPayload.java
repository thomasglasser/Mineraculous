package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.item.CatStaffItem;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ClientboundCatStaffPerchPayload(int field, float value, UUID senderID) implements ExtendedPacketPayload {

    public static final Type<ClientboundCatStaffPerchPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_cat_staff_perch_length"));
    public static final StreamCodec<ByteBuf, ClientboundCatStaffPerchPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ClientboundCatStaffPerchPayload::field, //0 - length; 1 - tick; 2 - groundY
            ByteBufCodecs.FLOAT, ClientboundCatStaffPerchPayload::value,
            UUIDUtil.STREAM_CODEC, ClientboundCatStaffPerchPayload::senderID,
            ClientboundCatStaffPerchPayload::new);
    @Override
    public void handle(Player player) {
        Player sender = player.level().getPlayerByUUID(senderID);
        if (sender != null) {
            switch (field) {
                case 0:
                    sender.setData(MineraculousAttachmentTypes.CAT_STAFF_PERCH_LENGTH, Optional.of(value));
                    break;
                case 1:
                    CatStaffItem.perchTickClient = (int) value;
                    break;
                case 2:
                    CatStaffItem.groundRYClient = value;
                    break;
                case 3:
                    CatStaffItem.catStaffPerchRender.put(senderID, value != 0);
                    break;
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
