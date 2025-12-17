package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.server.look.ServerLookManager;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record ServerboundSetLookPayload(Holder<Miraculous> miraculous, String id, String hash) implements ExtendedPacketPayload {

    public static final Type<ServerboundSetLookPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_set_look"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSetLookPayload> CODEC = StreamCodec.composite(
            Miraculous.STREAM_CODEC, ServerboundSetLookPayload::miraculous,
            ByteBufCodecs.STRING_UTF8, ServerboundSetLookPayload::id,
            ByteBufCodecs.STRING_UTF8, ServerboundSetLookPayload::hash,
            ServerboundSetLookPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        // TODO: Check permissions for allowed
        if (!ServerLookManager.hasLook(hash)) {
            TommyLibServices.NETWORK.sendToClient(new ClientboundRequestLookPayload(id, hash), (ServerPlayer) player);
        }
        TommyLibServices.NETWORK.sendToAllClients(new ClientboundSetLookPayload(player.getUUID(), miraculous, id, hash), player.getServer());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
