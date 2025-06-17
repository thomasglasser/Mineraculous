package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.item.component.ActiveSettings;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;

public record ServerboundToggleActivePayload(InteractionHand hand) implements ExtendedPacketPayload {
    public static final Type<ServerboundToggleActivePayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_toggle_active"));
    public static final StreamCodec<FriendlyByteBuf, ServerboundToggleActivePayload> CODEC = StreamCodec.composite(
            TommyLibExtraStreamCodecs.forEnum(InteractionHand.class), ServerboundToggleActivePayload::hand,
            ServerboundToggleActivePayload::new);

    @Override
    public void handle(Player player) {
        ItemStack stack = player.getItemInHand(hand);
        Boolean oldActive = stack.get(MineraculousDataComponents.ACTIVE);
        if (oldActive != null) {
            boolean active = !oldActive;
            stack.set(MineraculousDataComponents.ACTIVE, active);
            ActiveSettings settings = stack.get(MineraculousDataComponents.ACTIVE_SETTINGS);
            if (settings != null) {
                settings.controller().ifPresent(controller -> {
                    Optional<String> animation = active ? settings.onAnim() : settings.offAnim();
                    animation.ifPresent(anim -> ((SingletonGeoAnimatable) stack.getItem()).triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) player.level()), controller, anim));
                });
                Optional<Holder<SoundEvent>> soundEvent = active ? settings.onSound() : settings.offSound();
                soundEvent.ifPresent(sound -> player.level().playSound(null, player.getX(), player.getY() + (player.getBbHeight() / 2), player.getZ(), sound.value(), SoundSource.PLAYERS, 1.0F, 1.0F));
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
