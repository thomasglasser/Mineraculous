package dev.thomasglasser.mineraculous.impl.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.impl.client.MineraculousKeyMappings;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public record Active(boolean active, boolean showInTooltip) implements TooltipProvider {
    private static final String KEY = "item.mineraculous.active";
    public static final String PRESS_KEY_TO_TOGGLE = KEY + ".press_key_to_toggle";
    public static final Component ACTIVATE = Component.translatable(KEY + ".activate");
    public static final Component DEACTIVATE = Component.translatable(KEY + ".deactivate");

    public static final Active DEFAULT = new Active(false, true);
    public static final Codec<Active> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("active").forGetter(Active::active),
            Codec.BOOL.optionalFieldOf("show_in_tooltip", true).forGetter(Active::showInTooltip)).apply(instance, Active::new));
    public static final StreamCodec<ByteBuf, Active> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, Active::active,
            ByteBufCodecs.BOOL, Active::showInTooltip,
            Active::new);

    public Active toggle() {
        return new Active(!active(), showInTooltip);
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        if (showInTooltip) {
            tooltipAdder.accept(Component.translatable(PRESS_KEY_TO_TOGGLE, MineraculousKeyMappings.TOGGLE_ITEM_ACTIVE.getKey().getDisplayName(), active() ? DEACTIVATE : ACTIVATE).withStyle(ChatFormatting.GRAY));
        }
    }

    public static boolean isActive(ItemStack stack, boolean fallback) {
        Active active = stack.get(MineraculousDataComponents.ACTIVE);
        return active != null ? active.active() : fallback;
    }

    public static boolean isActive(ItemStack stack) {
        return isActive(stack, false);
    }
}
