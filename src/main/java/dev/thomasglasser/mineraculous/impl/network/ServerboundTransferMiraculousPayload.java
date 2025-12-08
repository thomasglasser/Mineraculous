package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.impl.world.entity.Kwami;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.tslat.smartbrainlib.util.BrainUtils;

public record ServerboundTransferMiraculousPayload(Optional<UUID> targetId, int kwamiId) implements ExtendedPacketPayload {
    public static final Type<ServerboundTransferMiraculousPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_transfer_miraculous"));
    public static final StreamCodec<ByteBuf, ServerboundTransferMiraculousPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), ServerboundTransferMiraculousPayload::targetId,
            ByteBufCodecs.VAR_INT, ServerboundTransferMiraculousPayload::kwamiId,
            ServerboundTransferMiraculousPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        ItemStack miraculous = player.getMainHandItem();
        ServerLevel level = (ServerLevel) player.level();
        if (level.getEntity(kwamiId) instanceof Kwami kwami) {
            Player target = targetId.map(level::getPlayerByUUID).orElseGet(kwami::findRandomRenounceTarget);
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
