package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

public record ServerboundOpenKamikotizationCommunicationCuriosPayload(UUID targetId, CuriosData curiosData, ResourceKey<Kamikotization> key, String name) implements ExtendedPacketPayload {

    public static final Type<ServerboundOpenKamikotizationCommunicationCuriosPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_kamikotize_curios"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundOpenKamikotizationCommunicationCuriosPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ServerboundOpenKamikotizationCommunicationCuriosPayload::targetId,
            CuriosData.STREAM_CODEC, ServerboundOpenKamikotizationCommunicationCuriosPayload::curiosData,
            ResourceKey.streamCodec(MineraculousRegistries.KAMIKOTIZATION), ServerboundOpenKamikotizationCommunicationCuriosPayload::key,
            ByteBufCodecs.STRING_UTF8, ServerboundOpenKamikotizationCommunicationCuriosPayload::name,
            ServerboundOpenKamikotizationCommunicationCuriosPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        // TODO: Implement
        Holder<Kamikotization> kamikotization = player.level().registryAccess().holderOrThrow(key);
        Player target = player.level().getPlayerByUUID(targetId);
        CompoundTag targetData = TommyLibServices.ENTITY.getPersistentData(target);
        targetData.putBoolean(MineraculousEntityEvents.TAG_SHOW_KAMIKO_MASK, true);
        TommyLibServices.ENTITY.setPersistentData(target, targetData, true);
        player.displayClientMessage(Component.literal("Kamikotizing " + target.getDisplayName() + " with " + name + " in slot " + curiosData), false);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
