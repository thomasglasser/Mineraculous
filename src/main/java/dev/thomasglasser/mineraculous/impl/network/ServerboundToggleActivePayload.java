package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.item.ActiveItem;
import dev.thomasglasser.mineraculous.api.world.item.component.ActiveSettings;
import dev.thomasglasser.mineraculous.impl.world.item.component.Active;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;

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
            ActiveSettings settings = stack.get(MineraculousDataComponents.ACTIVE_SETTINGS);
            if (settings != null) {
                if (item instanceof SingletonGeoAnimatable animatable) {
                    settings.controller().ifPresent(controller -> {
                        Optional<String> animation = active.active() ? settings.onAnim() : settings.offAnim();
                        animation.ifPresent(anim -> animatable.triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) player.level()), controller, anim));
                    });
                }
                Optional<Holder<SoundEvent>> soundEvent = active.active() ? settings.onSound() : settings.offSound();
                soundEvent.ifPresent(sound -> player.level().playSound(null, player.getX(), player.getY() + (player.getBbHeight() / 2), player.getZ(), sound.value(), SoundSource.PLAYERS, 1.0F, 1.0F));
            }
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
