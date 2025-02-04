package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.network.NetworkUtils;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;

public record ServerboundActivateToolPayload(boolean activate, InteractionHand hand, Optional<String> controller, Optional<String> animation, Optional<Holder<SoundEvent>> sound) implements ExtendedPacketPayload {

    public static final Type<ServerboundActivateToolPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_activate_tool"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundActivateToolPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ServerboundActivateToolPayload::activate,
            NetworkUtils.enumCodec(InteractionHand.class), ServerboundActivateToolPayload::hand,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), ServerboundActivateToolPayload::controller,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), ServerboundActivateToolPayload::animation,
            ByteBufCodecs.optional(SoundEvent.STREAM_CODEC), ServerboundActivateToolPayload::sound,
            ServerboundActivateToolPayload::new);
    public ServerboundActivateToolPayload(boolean activate, InteractionHand hand, String controller, String animation, Holder<SoundEvent> sound) {
        this(activate, hand, Optional.of(controller), Optional.of(animation), Optional.of(sound));
    }

    public ServerboundActivateToolPayload(boolean activate, InteractionHand hand) {
        this(activate, hand, Optional.empty(), Optional.empty(), Optional.empty());
    }

    // ON SERVER
    @Override
    public void handle(Player player) {
        ItemStack itemInHand = player.getItemInHand(hand);
        if (activate) {
            itemInHand.set(MineraculousDataComponents.ACTIVE.get(), Unit.INSTANCE);
        } else {
            itemInHand.remove(MineraculousDataComponents.ACTIVE.get());
        }
        if (controller.isPresent() && animation.isPresent())
            ((SingletonGeoAnimatable) itemInHand.getItem()).triggerAnim(player, GeoItem.getOrAssignId(itemInHand, (ServerLevel) player.level()), controller.get(), animation.get());
        sound.ifPresent(holder -> player.level().playSound(null, player.getX(), player.getY() + 1, player.getZ(), holder.value(), SoundSource.PLAYERS, 1.0F, 1.0F));
        player.setItemInHand(hand, itemInHand);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
