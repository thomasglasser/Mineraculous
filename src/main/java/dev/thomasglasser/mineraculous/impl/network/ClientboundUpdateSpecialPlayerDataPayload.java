package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers.SpecialPlayerData;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record ClientboundUpdateSpecialPlayerDataPayload(UUID uuid, SpecialPlayerData specialPlayerData) implements ExtendedPacketPayload {
    public static final Type<ClientboundUpdateSpecialPlayerDataPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_update_special_player_data"));
    public static final StreamCodec<FriendlyByteBuf, ClientboundUpdateSpecialPlayerDataPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ClientboundUpdateSpecialPlayerDataPayload::uuid,
            SpecialPlayerData.STREAM_CODEC, ClientboundUpdateSpecialPlayerDataPayload::specialPlayerData,
            ClientboundUpdateSpecialPlayerDataPayload::new);

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
