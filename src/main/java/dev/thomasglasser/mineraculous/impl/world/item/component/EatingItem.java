package dev.thomasglasser.mineraculous.impl.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public record EatingItem(ItemStack item, int remainingTicks) {
    public static final Codec<EatingItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.SINGLE_ITEM_CODEC.fieldOf("item").forGetter(EatingItem::item),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("remaining_ticks").forGetter(EatingItem::remainingTicks)).apply(instance, EatingItem::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EatingItem> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, EatingItem::item,
            ByteBufCodecs.VAR_INT, EatingItem::remainingTicks,
            EatingItem::new);

    public EatingItem tick() {
        return new EatingItem(item, remainingTicks - 1);
    }
}
