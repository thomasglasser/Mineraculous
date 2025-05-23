package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.network.NetworkUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ServerboundToggleActivePayload(InteractionHand hand) implements ExtendedPacketPayload {
    public static final Type<ServerboundToggleActivePayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_toggle_active"));
    public static final StreamCodec<FriendlyByteBuf, ServerboundToggleActivePayload> CODEC = StreamCodec.composite(
            NetworkUtils.enumCodec(InteractionHand.class), ServerboundToggleActivePayload::hand,
            ServerboundToggleActivePayload::new);

    @Override
    public void handle(Player player) {
        ItemStack stack = player.getItemInHand(hand);
        Boolean active = stack.get(MineraculousDataComponents.ACTIVE);
        if (active != null) {
            stack.set(MineraculousDataComponents.ACTIVE, !active);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
