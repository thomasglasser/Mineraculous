package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public record ServerboundMiraculousTransformPayload(ResourceKey<Miraculous> miraculous, MiraculousData data, boolean transform) implements ExtendedPacketPayload {

    public static final Type<ServerboundMiraculousTransformPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_miraculous_transform"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundMiraculousTransformPayload> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), ServerboundMiraculousTransformPayload::miraculous,
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
