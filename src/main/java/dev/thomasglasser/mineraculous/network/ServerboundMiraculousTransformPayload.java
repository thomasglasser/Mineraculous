package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record ServerboundMiraculousTransformPayload(ResourceKey<Miraculous> miraculousType, MiraculousData data, boolean transform) implements ExtendedPacketPayload {

    public static final Type<ServerboundMiraculousTransformPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_miraculous_transform"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundMiraculousTransformPayload> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), ServerboundMiraculousTransformPayload::miraculousType,
            MiraculousData.STREAM_CODEC, ServerboundMiraculousTransformPayload::data,
            ByteBufCodecs.BOOL, ServerboundMiraculousTransformPayload::transform,
            ServerboundMiraculousTransformPayload::new);

    // ON SERVER
    @Override
    public void handle(@Nullable Player player) {
        MineraculousEntityEvents.handleMiraculousTransformation((ServerPlayer) player, miraculousType, data, transform);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
