package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public record ServerboundKamikotizationTransformPayload(Optional<UUID> targetId, KamikotizationData data, boolean transform, boolean instant, boolean itemBroken, Vec3 kamikoSpawnPos) implements ExtendedPacketPayload {

    public static final Type<ServerboundKamikotizationTransformPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_kamikotization_transform"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundKamikotizationTransformPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString)), ServerboundKamikotizationTransformPayload::targetId,
            KamikotizationData.STREAM_CODEC, ServerboundKamikotizationTransformPayload::data,
            ByteBufCodecs.BOOL, ServerboundKamikotizationTransformPayload::transform,
            ByteBufCodecs.BOOL, ServerboundKamikotizationTransformPayload::instant,
            ByteBufCodecs.BOOL, ServerboundKamikotizationTransformPayload::itemBroken,
            ByteBufCodecs.VECTOR3F.map(Vec3::new, Vec3::toVector3f), ServerboundKamikotizationTransformPayload::kamikoSpawnPos,
            ServerboundKamikotizationTransformPayload::new);
    public ServerboundKamikotizationTransformPayload(KamikotizationData data, boolean transform, boolean instant, boolean itemBroken, Vec3 kamikoSpawnPos) {
        this(Optional.empty(), data, transform, instant, itemBroken, kamikoSpawnPos);
    }

    // ON SERVER
    @Override
    public void handle(Player player) {
        MineraculousEntityEvents.handleKamikotizationTransformation((ServerPlayer) targetId.map(player.level()::getPlayerByUUID).orElse(player), data, transform, instant, kamikoSpawnPos);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
