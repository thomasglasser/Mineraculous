package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.impl.server.look.ServerLookManager;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record ServerboundSetLookDataPayload(Holder<Miraculous> miraculous, MiraculousData.LookData lookData) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetLookDataPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_set_look_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSetLookDataPayload> CODEC = StreamCodec.composite(
            Miraculous.STREAM_CODEC, ServerboundSetLookDataPayload::miraculous,
            MiraculousData.LookData.STREAM_CODEC, ServerboundSetLookDataPayload::lookData,
            ServerboundSetLookDataPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        // TODO: Check permissions for allowed
        player.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous).withLookData(lookData).save(miraculous, player);
        Set<String> missing = new ObjectOpenHashSet<>();
        for (String hash : lookData.hashes().values()) {
            if (!ServerLookManager.hasLook(hash)) {
                missing.add(hash);
            }
        }
        if (!missing.isEmpty())
            TommyLibServices.NETWORK.sendToClient(new ClientboundRequestLooksPayload(missing), (ServerPlayer) player);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
