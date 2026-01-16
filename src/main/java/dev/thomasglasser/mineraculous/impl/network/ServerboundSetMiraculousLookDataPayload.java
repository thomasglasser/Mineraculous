package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.look.LookData;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.server.look.ServerLookManager;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record ServerboundSetMiraculousLookDataPayload(Holder<Miraculous> miraculous, LookData lookData) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetMiraculousLookDataPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_set_miraculous_look_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSetMiraculousLookDataPayload> CODEC = StreamCodec.composite(
            Miraculous.STREAM_CODEC, ServerboundSetMiraculousLookDataPayload::miraculous,
            LookData.STREAM_CODEC, ServerboundSetMiraculousLookDataPayload::lookData,
            ServerboundSetMiraculousLookDataPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        player.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous).withLookData(lookData).save(miraculous, player);
        ServerLookManager.requestMissingLooks(lookData, (ServerPlayer) player);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
