package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemUtils;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ServerboundTryBreakItemPayload implements ExtendedPacketPayload {
    public static final ServerboundTryBreakItemPayload INSTANCE = new ServerboundTryBreakItemPayload();
    public static final Type<ServerboundTryBreakItemPayload> TYPE = new Type<>(MineraculousConstants.modLoc("try_break_item"));
    public static final StreamCodec<ByteBuf, ServerboundTryBreakItemPayload> CODEC = StreamCodec.unit(INSTANCE);

    private ServerboundTryBreakItemPayload() {}

    // ON SERVER
    @Override
    public void handle(Player player) {
        MineraculousItemUtils.BreakResult result = MineraculousItemUtils.tryBreakItem(player.getMainHandItem(), (ServerLevel) player.level(), player.position().add(0, 1, 0), player);
        ItemStack mainHandItem = result.original();
        ItemStack rest = result.remainder();
        if (mainHandItem.isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, rest);
        } else {
            player.setItemInHand(InteractionHand.MAIN_HAND, mainHandItem);
            player.addItem(rest);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
