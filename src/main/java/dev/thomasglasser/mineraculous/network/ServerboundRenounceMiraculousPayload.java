package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.network.codec.ExtraStreamCodecs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public record ServerboundRenounceMiraculousPayload(InteractionHand hand) implements ExtendedPacketPayload {
    public static final Type<ServerboundRenounceMiraculousPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_renounce_miraculous"));
    public static final StreamCodec<FriendlyByteBuf, ServerboundRenounceMiraculousPayload> CODEC = StreamCodec.composite(
            ExtraStreamCodecs.forEnum(InteractionHand.class), ServerboundRenounceMiraculousPayload::hand,
            ServerboundRenounceMiraculousPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        MineraculousEntityEvents.renounceMiraculous(player.getItemInHand(hand), (ServerLevel) player.level());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
