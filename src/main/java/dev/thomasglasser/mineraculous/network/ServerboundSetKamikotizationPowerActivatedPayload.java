package dev.thomasglasser.mineraculous.network;

import com.mojang.datafixers.util.Either;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.ability.Ability;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

// TODO: Match to miraculous power activation
public record ServerboundSetKamikotizationPowerActivatedPayload(ResourceKey<Kamikotization> kamikotization) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetKamikotizationPowerActivatedPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_set_kamikotization_power_activated"));
    public static final StreamCodec<FriendlyByteBuf, ServerboundSetKamikotizationPowerActivatedPayload> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.KAMIKOTIZATION), ServerboundSetKamikotizationPowerActivatedPayload::kamikotization,
            ServerboundSetKamikotizationPowerActivatedPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        ServerLevel level = (ServerLevel) player.level();
        Ability power = level.registryAccess().holderOrThrow(kamikotization).value().powerSource().right().map(Holder::value).orElse(null);
        if (power != null && power.canActivate(new AbilityData(0, Either.right(kamikotization)), level, player.blockPosition(), player)) {
            power.playStartSound(level, player.blockPosition());
            player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).orElseThrow().withMainPowerActive(true).save(player, true);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
