package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosUtils;
import dev.thomasglasser.mineraculous.impl.world.item.component.Kamikotizing;
import dev.thomasglasser.mineraculous.impl.world.level.storage.SlotInfo;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ServerboundSetItemKamikotizingPayload(Optional<UUID> targetId, boolean kamikotizing, SlotInfo slotInfo) implements ExtendedPacketPayload {

    public static final Type<ServerboundSetItemKamikotizingPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_set_item_kamikotizing"));
    public static StreamCodec<ByteBuf, ServerboundSetItemKamikotizingPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), ServerboundSetItemKamikotizingPayload::targetId,
            ByteBufCodecs.BOOL, ServerboundSetItemKamikotizingPayload::kamikotizing,
            SlotInfo.STREAM_CODEC, ServerboundSetItemKamikotizingPayload::slotInfo,
            ServerboundSetItemKamikotizingPayload::new);
    @Override
    public void handle(Player player) {
        Player target = targetId.isPresent() ? player.level().getPlayerByUUID(targetId.get()) : player;
        if (target != null) {
            ItemStack stack = slotInfo.getSlot().map(slot -> target.getInventory().getItem(slot), curiosData -> CuriosUtils.getStackInSlot(target, curiosData));
            stack.set(MineraculousDataComponents.KAMIKOTIZING, kamikotizing ? new Kamikotizing(slotInfo) : null);
            slotInfo.getSlot().ifLeft(slot -> target.getInventory().setItem(slot, stack)).ifRight(curiosData -> CuriosUtils.setStackInSlot(target, curiosData, stack));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
