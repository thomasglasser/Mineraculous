package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.renderer.entity.layers.VipData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

public record ServerboundChangeVipDataPayload(UUID uuid, VipData vipData) implements ExtendedPacketPayload {
    public static final Type<ServerboundChangeVipDataPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_change_vip_data"));
    public static final StreamCodec<FriendlyByteBuf, ServerboundChangeVipDataPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ServerboundChangeVipDataPayload::uuid,
            VipData.STREAM_CODEC, ServerboundChangeVipDataPayload::vipData,
            ServerboundChangeVipDataPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        TommyLibServices.NETWORK.sendToAllClients(new ClientboundChangeVipDataPayload(uuid, vipData), player.getServer());
    }

    @Override
    public Type<? extends ExtendedPacketPayload> type() {
        return TYPE;
    }
}
