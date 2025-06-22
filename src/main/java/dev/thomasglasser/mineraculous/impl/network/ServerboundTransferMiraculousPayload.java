package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.world.entity.Kwami;
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
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.tslat.smartbrainlib.util.BrainUtils;

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
        MinecraftServer server = player.getServer();
        if (player.level().getEntity(kwamiId) instanceof Kwami kwami && server != null) {
            Player target = targetId.map(id -> player.level().getPlayerByUUID(id)).orElseGet(() -> {
                List<ServerPlayer> players = new ReferenceArrayList<>(server.getPlayerList().getPlayers());
                players.removeIf(p -> p == player || EntityUtils.TARGET_TOO_FAR_PREDICATE.test(kwami, p));
                return players.get(player.level().random.nextInt(players.size()));
            });
            if (target != null) {
                BrainUtils.setMemory(kwami, MemoryModuleType.LIKED_PLAYER, target.getUUID());
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
