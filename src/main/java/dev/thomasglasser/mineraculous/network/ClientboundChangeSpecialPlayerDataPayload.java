package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.renderer.entity.layers.SpecialPlayerData;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record ClientboundChangeSpecialPlayerDataPayload(UUID uuid, SpecialPlayerData specialPlayerData) implements ExtendedPacketPayload {
    public static final Type<ClientboundChangeSpecialPlayerDataPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_change_special_player_data"));
    public static final StreamCodec<FriendlyByteBuf, ClientboundChangeSpecialPlayerDataPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ClientboundChangeSpecialPlayerDataPayload::uuid,
            SpecialPlayerData.STREAM_CODEC, ClientboundChangeSpecialPlayerDataPayload::specialPlayerData,
            ClientboundChangeSpecialPlayerDataPayload::new);

    // ON CLIENT
    @Override
    public void handle(@Nullable Player player) {
        MineraculousClientUtils.setSpecialPlayerData(ClientUtils.getPlayerByUUID(uuid), specialPlayerData);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
