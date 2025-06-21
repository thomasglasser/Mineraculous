package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.api.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public record ServerboundMiraculousTransformPayload(Holder<Miraculous> miraculous, MiraculousData data, boolean transform) implements ExtendedPacketPayload {

    public static final Type<ServerboundMiraculousTransformPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_miraculous_transform"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundMiraculousTransformPayload> CODEC = StreamCodec.composite(
            Miraculous.STREAM_CODEC, ServerboundMiraculousTransformPayload::miraculous,
            MiraculousData.STREAM_CODEC, ServerboundMiraculousTransformPayload::data,
            ByteBufCodecs.BOOL, ServerboundMiraculousTransformPayload::transform,
            ServerboundMiraculousTransformPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        ServerLevel level = (ServerLevel) player.level();
        if (transform) {
            data.transform(player, level, miraculous);
        } else {
            data.detransform(player, level, miraculous, false);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
