package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ClientboundSetPlayerRotationPayload(float x, float y, float z, UUID senderID) implements ExtendedPacketPayload {

    public static final Type<ClientboundSetPlayerRotationPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_player_rotation"));
    public static final StreamCodec<ByteBuf, ClientboundSetPlayerRotationPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ClientboundSetPlayerRotationPayload::x, //0 - length; 1 - tick; 2 - groundY
            ByteBufCodecs.FLOAT, ClientboundSetPlayerRotationPayload::y,
            ByteBufCodecs.FLOAT, ClientboundSetPlayerRotationPayload::z,
            UUIDUtil.STREAM_CODEC, ClientboundSetPlayerRotationPayload::senderID,
            ClientboundSetPlayerRotationPayload::new);
    @Override
    public void handle(Player player) {
        Player sender = player.level().getPlayerByUUID(senderID);
        if (sender != null) { //TODO add some bool parameters deciding to set the rot or not, passing the current rot value wont be smooth
            sender.setYRot(y);
            sender.setYBodyRot(z);
            //sender.setXRot(x);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
