package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.world.item.MiraculousTool;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ServerboundSetMiraculousToolMode(ItemStack selectedToolCopy) implements ExtendedPacketPayload {
    public static final CustomPacketPayload.Type<ServerboundSetMiraculousToolMode> TYPE = new CustomPacketPayload.Type<>(MineraculousConstants.modLoc("serverbound_miraculous_tool_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSetMiraculousToolMode> CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, ServerboundSetMiraculousToolMode::selectedToolCopy,
            ServerboundSetMiraculousToolMode::new);

    @Override
    public void handle(Player player) {
        ItemStack itemStack = player.getInventory().getSelected();
        if (itemStack.getItem() instanceof MiraculousTool tool) {
            tool.setToolMode(itemStack, tool.getToolMode(selectedToolCopy), player);
        }
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
