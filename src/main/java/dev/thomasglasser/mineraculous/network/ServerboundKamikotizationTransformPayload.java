package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.item.component.KamikoData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record ServerboundKamikotizationTransformPayload(KamikotizationData data, KamikoData kamikoData, boolean transform) implements ExtendedPacketPayload {

    public static final Type<ServerboundKamikotizationTransformPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_kamikotization_transform"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundKamikotizationTransformPayload> CODEC = StreamCodec.composite(
            KamikotizationData.STREAM_CODEC, ServerboundKamikotizationTransformPayload::data,
            KamikoData.STREAM_CODEC, ServerboundKamikotizationTransformPayload::kamikoData,
            ByteBufCodecs.BOOL, ServerboundKamikotizationTransformPayload::transform,
            ServerboundKamikotizationTransformPayload::new);

    // ON SERVER
    @Override
    public void handle(@Nullable Player player) {
        MineraculousEntityEvents.handleKamikotizationTransformation((ServerPlayer) player, data, kamikoData, transform);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
