package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.server.commands.MiraculousCommand;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

public record ServerboundReportSuitLookAbsentPayload(Optional<UUID> senderId, ResourceKey<Miraculous> miraculous, String look) implements ExtendedPacketPayload {

    public static final Type<ServerboundReportSuitLookAbsentPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_report_suit_look_absent"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundReportSuitLookAbsentPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), ServerboundReportSuitLookAbsentPayload::senderId,
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), ServerboundReportSuitLookAbsentPayload::miraculous,
            ByteBufCodecs.STRING_UTF8, ServerboundReportSuitLookAbsentPayload::look,
            ServerboundReportSuitLookAbsentPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        Player sender = senderId.map(uuid -> player.level().getPlayerByUUID(uuid)).orElse(null);
        CommandSourceStack commandSourceStack = sender == null ? player.getServer().createCommandSourceStack() : sender.createCommandSourceStack();
        commandSourceStack.sendFailure(Component.translatable(MiraculousCommand.LOOK_SUIT_SET_FAILURE, Component.translatable(Miraculous.toLanguageKey(miraculous)), look));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
