package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.api.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record ServerboundTriggerKamikotizationAdvancementsPayload(UUID performerId, UUID victimId, ResourceKey<Kamikotization> kamikotization) implements ExtendedPacketPayload {

    public static final Type<ServerboundTriggerKamikotizationAdvancementsPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_trigger_kamikotization_advancements"));
    public static final StreamCodec<ByteBuf, ServerboundTriggerKamikotizationAdvancementsPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ServerboundTriggerKamikotizationAdvancementsPayload::performerId,
            UUIDUtil.STREAM_CODEC, ServerboundTriggerKamikotizationAdvancementsPayload::victimId,
            ResourceKey.streamCodec(MineraculousRegistries.KAMIKOTIZATION), ServerboundTriggerKamikotizationAdvancementsPayload::kamikotization,
            ServerboundTriggerKamikotizationAdvancementsPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        ServerPlayer performer = (ServerPlayer) player.level().getPlayerByUUID(performerId);
        ServerPlayer victim = (ServerPlayer) player.level().getPlayerByUUID(victimId);
        if (performer != null && victim != null) {
            boolean self = performer == victim;
            MineraculousCriteriaTriggers.TRANSFORMED_KAMIKOTIZATION.get().trigger(victim, kamikotization, self);
            MineraculousCriteriaTriggers.KAMIKOTIZED_ENTITY.get().trigger(performer, victim, kamikotization, self);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
