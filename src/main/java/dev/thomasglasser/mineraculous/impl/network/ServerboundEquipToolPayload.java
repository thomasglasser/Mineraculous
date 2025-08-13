package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosUtils;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ServerboundEquipToolPayload(InteractionHand hand) implements ExtendedPacketPayload {
    public static final Type<ServerboundEquipToolPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_equip_tool"));
    public static final StreamCodec<ByteBuf, ServerboundEquipToolPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(InteractionHand::valueOf, InteractionHand::name), ServerboundEquipToolPayload::hand,
            ServerboundEquipToolPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty() && CuriosUtils.setStackInFirstValidSlot(player, stack))
            player.setItemInHand(hand, ItemStack.EMPTY);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
