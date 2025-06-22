package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.world.item.RadialMenuProvider;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ServerboundSetRadialMenuProviderOptionPayload(InteractionHand hand, int index) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetRadialMenuProviderOptionPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_set_radial_menu_provider_option"));
    public static final StreamCodec<FriendlyByteBuf, ServerboundSetRadialMenuProviderOptionPayload> CODEC = StreamCodec.composite(
            TommyLibExtraStreamCodecs.forEnum(InteractionHand.class), ServerboundSetRadialMenuProviderOptionPayload::hand,
            ByteBufCodecs.INT, ServerboundSetRadialMenuProviderOptionPayload::index,
            ServerboundSetRadialMenuProviderOptionPayload::new);

    @Override
    public void handle(Player player) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof RadialMenuProvider<?> provider) {
            provider.setOption(stack, hand, player, index);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
