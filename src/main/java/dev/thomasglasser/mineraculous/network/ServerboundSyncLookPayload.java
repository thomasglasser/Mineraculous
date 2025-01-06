package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.server.commands.MiraculousCommand;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.LookData;
import dev.thomasglasser.mineraculous.world.level.storage.LookDataHolder;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ServerboundSyncLookPayload(Optional<UUID> senderId, LookData data) implements ExtendedPacketPayload {
    public static final Type<ServerboundSyncLookPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_sync_look"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSyncLookPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), ServerboundSyncLookPayload::senderId,
            LookData.CODEC, ServerboundSyncLookPayload::data,
            ServerboundSyncLookPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        Player sender = senderId.map(uuid -> player.level().getPlayerByUUID(uuid)).orElse(null);
        CommandSourceStack commandSourceStack = sender == null ? player.getServer().createCommandSourceStack() : sender.createCommandSourceStack();
        commandSourceStack.sendSuccess(() -> sender == player ? Component.translatable(MiraculousCommand.LOOK_SET_SUCCESS_SELF, Component.translatable(Miraculous.toLanguageKey(data.miraculous())), data.look()) : Component.translatable(MiraculousCommand.LOOK_SET_SUCCESS_OTHER, player.getDisplayName(), Component.translatable(Miraculous.toLanguageKey(data.miraculous())), data.look()), true);
        MiraculousDataSet miraculousDataSet = player.getData(MineraculousAttachmentTypes.MIRACULOUS);
        miraculousDataSet.put(player, data.miraculous(), miraculousDataSet.get(data.miraculous()).withLook(data.look()), false);
        ((LookDataHolder) player.level()).mineraculous$addLookData(player.getUUID(), data);
        TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncLookPayload(data), player.getServer());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
