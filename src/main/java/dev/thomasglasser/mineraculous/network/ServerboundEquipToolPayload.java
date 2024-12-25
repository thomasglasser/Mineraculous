package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosUtils;
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
        CuriosUtils.setStackInFirstValidSlot(player, player.getItemInHand(hand), true);
        player.setItemInHand(hand, ItemStack.EMPTY);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
