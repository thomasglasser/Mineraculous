package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.item.ActiveItem;
import dev.thomasglasser.mineraculous.impl.world.item.component.Active;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ServerboundToggleActivePayload implements ExtendedPacketPayload {
    public static final ServerboundToggleActivePayload INSTANCE = new ServerboundToggleActivePayload();
    public static final Type<ServerboundToggleActivePayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_toggle_active"));
    public static final StreamCodec<ByteBuf, ServerboundToggleActivePayload> CODEC = StreamCodec.unit(INSTANCE);

    private ServerboundToggleActivePayload() {}

    @Override
    public void handle(Player player) {
        if (player.getMainHandItem().has(MineraculousDataComponents.ACTIVE)) {
            toggleActive(player.getMainHandItem(), player);
        } else if (player.getOffhandItem().has(MineraculousDataComponents.ACTIVE)) {
            toggleActive(player.getOffhandItem(), player);
        }
    }

    private void toggleActive(ItemStack stack, Player player) {
        Item item = stack.getItem();
        Active oldActive = stack.get(MineraculousDataComponents.ACTIVE);
        if (oldActive != null) {
            Active active = oldActive.toggle();
            stack.set(MineraculousDataComponents.ACTIVE, active);
            if (item instanceof ActiveItem activeItem) {
                activeItem.onToggle(stack, player, active);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
