package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.network.NetworkUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;

public record ServerboundActivateToolPayload(boolean activate, InteractionHand hand, String controller, String animation) implements ExtendedPacketPayload {

    public static final Type<ServerboundActivateToolPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_activate_tool"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundActivateToolPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ServerboundActivateToolPayload::activate,
            NetworkUtils.enumCodec(InteractionHand.class), ServerboundActivateToolPayload::hand,
            ByteBufCodecs.STRING_UTF8, ServerboundActivateToolPayload::controller,
            ByteBufCodecs.STRING_UTF8, ServerboundActivateToolPayload::animation,
            ServerboundActivateToolPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        ItemStack itemInHand = player.getItemInHand(hand);
        if (activate) {
            itemInHand.set(MineraculousDataComponents.ACTIVE.get(), Unit.INSTANCE);
        } else {
            itemInHand.remove(MineraculousDataComponents.ACTIVE.get());
        }
        ((SingletonGeoAnimatable) itemInHand.getItem()).triggerAnim(player, GeoItem.getOrAssignId(itemInHand, (ServerLevel) player.level()), controller, animation);
        player.setItemInHand(hand, itemInHand);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
