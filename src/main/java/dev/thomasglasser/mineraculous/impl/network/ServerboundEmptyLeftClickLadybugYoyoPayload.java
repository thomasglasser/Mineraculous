package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ServerboundEmptyLeftClickLadybugYoyoPayload implements ExtendedPacketPayload {
    public static final ServerboundEmptyLeftClickLadybugYoyoPayload INSTANCE = new ServerboundEmptyLeftClickLadybugYoyoPayload();
    public static final Type<ServerboundEmptyLeftClickLadybugYoyoPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_empty_left_click_ladybug_yoyo"));
    public static final StreamCodec<ByteBuf, ServerboundEmptyLeftClickLadybugYoyoPayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public void handle(Player player) {
        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.is(MineraculousItems.LADYBUG_YOYO)) {
            MineraculousItems.LADYBUG_YOYO.get().onLeftClick(mainHandItem, player);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
