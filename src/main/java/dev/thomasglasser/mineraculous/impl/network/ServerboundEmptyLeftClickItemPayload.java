package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.world.item.LeftClickTrackingItem;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ServerboundEmptyLeftClickItemPayload implements ExtendedPacketPayload {
    public static final ServerboundEmptyLeftClickItemPayload INSTANCE = new ServerboundEmptyLeftClickItemPayload();
    public static final Type<ServerboundEmptyLeftClickItemPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_empty_left_click_item"));
    public static final StreamCodec<ByteBuf, ServerboundEmptyLeftClickItemPayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public void handle(Player player) {
        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.getItem() instanceof LeftClickTrackingItem leftClickTrackingItem) {
            leftClickTrackingItem.onLeftClick(mainHandItem, player);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
