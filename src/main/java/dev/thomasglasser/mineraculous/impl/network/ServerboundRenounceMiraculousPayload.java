package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ServerboundRenounceMiraculousPayload implements ExtendedPacketPayload {
    public static final ServerboundRenounceMiraculousPayload INSTANCE = new ServerboundRenounceMiraculousPayload();
    public static final Type<ServerboundRenounceMiraculousPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_renounce_miraculous"));
    public static final StreamCodec<ByteBuf, ServerboundRenounceMiraculousPayload> CODEC = StreamCodec.unit(INSTANCE);

    private ServerboundRenounceMiraculousPayload() {}

    // ON SERVER
    @Override
    public void handle(Player player) {
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offhandItem = player.getOffhandItem();
        if (mainHandItem.getItem() instanceof MiraculousItem && !mainHandItem.has(MineraculousDataComponents.POWERED)) {
            renounceMiraculous(mainHandItem, player);
        } else if (offhandItem.getItem() instanceof MiraculousItem && !offhandItem.has(MineraculousDataComponents.POWERED)) {
            renounceMiraculous(offhandItem, player);
        }
    }

    private void renounceMiraculous(ItemStack stack, Player player) {
        Holder<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
        if (miraculous != null) {
            MineraculousEntityUtils.renounceKwami(true, stack, player);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
