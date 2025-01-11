package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.ability.Ability;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public record ServerboundSetKamikotizationPowerActivatedPayload(ResourceKey<Kamikotization> kamikotization) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetKamikotizationPowerActivatedPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_set_kamikotization_power_activated"));
    public static final StreamCodec<FriendlyByteBuf, ServerboundSetKamikotizationPowerActivatedPayload> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.KAMIKOTIZATION), ServerboundSetKamikotizationPowerActivatedPayload::kamikotization,
            ServerboundSetKamikotizationPowerActivatedPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        Level level = player.level();
        Ability power = level.registryAccess().holderOrThrow(kamikotization).value().activeAbility().get().value();
        power.playStartSound(level, player.blockPosition());
        player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).orElseThrow().withMainPowerActive(true).save(player, true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
