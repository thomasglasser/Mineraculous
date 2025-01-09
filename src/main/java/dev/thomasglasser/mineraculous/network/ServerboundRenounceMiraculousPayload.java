package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public record ServerboundRenounceMiraculousPayload(int slot) implements ExtendedPacketPayload {
    public static final Type<ServerboundRenounceMiraculousPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_renounce_miraculous"));
    public static final StreamCodec<ByteBuf, ServerboundRenounceMiraculousPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundRenounceMiraculousPayload::slot,
            ServerboundRenounceMiraculousPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        MineraculousEntityEvents.renounceMiraculous(player.getInventory().getItem(slot), (ServerLevel) player.level());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
