package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.entity.Kwami;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.world.entity.EntityUtils;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ServerboundTransferMiraculousPayload(Optional<UUID> targetId, int kwamiId) implements ExtendedPacketPayload {
    public static final Type<ServerboundTransferMiraculousPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_transfer_miraculous"));
    public static final StreamCodec<ByteBuf, ServerboundTransferMiraculousPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), ServerboundTransferMiraculousPayload::targetId,
            ByteBufCodecs.INT, ServerboundTransferMiraculousPayload::kwamiId,
            ServerboundTransferMiraculousPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        ItemStack miraculous = player.getMainHandItem();
        Entity entity = player.level().getEntity(kwamiId);
        MinecraftServer server = player.getServer();
        if (entity instanceof Kwami kwami && server != null) {
            Player target;
            if (targetId.isPresent())
                target = player.level().getPlayerByUUID(targetId.get());
            else {
                List<ServerPlayer> players = new ReferenceArrayList<>(server.getPlayerList().getPlayers());
                players.removeIf(p -> p == player || EntityUtils.TARGET_TOO_FAR_PREDICATE.test(kwami, p));
                target = players.get(player.level().random.nextInt(players.size()));
            }
            if (target != null) {
                kwami.setTarget(target);
                kwami.setOwnerUUID(null);
                miraculous.set(MineraculousDataComponents.POWERED.get(), Unit.INSTANCE);
                kwami.setItemInHand(InteractionHand.MAIN_HAND, miraculous);
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
