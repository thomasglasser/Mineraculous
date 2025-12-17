package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.client.look.LookManager;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ClientboundSetLookPayload(UUID playerId, Holder<Miraculous> miraculous, String id, String hash) implements ExtendedPacketPayload {

    public static final Type<ClientboundSetLookPayload> TYPE = new Type<>(MineraculousConstants.modLoc("clientbound_set_look"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSetLookPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ClientboundSetLookPayload::playerId,
            Miraculous.STREAM_CODEC, ClientboundSetLookPayload::miraculous,
            ByteBufCodecs.STRING_UTF8, ClientboundSetLookPayload::id,
            ByteBufCodecs.STRING_UTF8, ClientboundSetLookPayload::hash,
            ClientboundSetLookPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        // TODO: Check permissions
        LookManager.assign(playerId, miraculous, id);
        if (!LookManager.hasLook(id, hash)) {
            TommyLibServices.NETWORK.sendToServer(new ServerboundRequestLookPayload(hash));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
