package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers.SpecialPlayerData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

public record ServerboundUpdateSpecialPlayerDataPayload(UUID uuid, SpecialPlayerData specialPlayerData) implements ExtendedPacketPayload {
    public static final Type<ServerboundUpdateSpecialPlayerDataPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_update_special_player_data"));
    public static final StreamCodec<FriendlyByteBuf, ServerboundUpdateSpecialPlayerDataPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ServerboundUpdateSpecialPlayerDataPayload::uuid,
            SpecialPlayerData.STREAM_CODEC, ServerboundUpdateSpecialPlayerDataPayload::specialPlayerData,
            ServerboundUpdateSpecialPlayerDataPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        TommyLibServices.NETWORK.sendToAllClients(new ClientboundUpdateSpecialPlayerDataPayload(uuid, specialPlayerData.verify(uuid)), player.getServer());
    }

    @Override
    public Type<? extends ExtendedPacketPayload> type() {
        return TYPE;
    }
}
