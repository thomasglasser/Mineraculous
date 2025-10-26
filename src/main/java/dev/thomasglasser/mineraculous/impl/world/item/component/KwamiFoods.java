package dev.thomasglasser.mineraculous.impl.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.impl.world.entity.Kwami;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public record KwamiFoods(TagKey<Item> foods, TagKey<Item> treats) {
    public static final Codec<KwamiFoods> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TagKey.codec(Registries.ITEM).fieldOf("foods").forGetter(KwamiFoods::foods),
            TagKey.codec(Registries.ITEM).fieldOf("treats").forGetter(KwamiFoods::treats)).apply(instance, KwamiFoods::new));
    private static final StreamCodec<ByteBuf, TagKey<Item>> ITEM_TAG_STREAM_CODEC = ResourceLocation.STREAM_CODEC.map(loc -> TagKey.create(Registries.ITEM, loc), TagKey::location);
    public static final StreamCodec<RegistryFriendlyByteBuf, KwamiFoods> STREAM_CODEC = StreamCodec.composite(
            ITEM_TAG_STREAM_CODEC, KwamiFoods::foods,
            ITEM_TAG_STREAM_CODEC, KwamiFoods::treats,
            KwamiFoods::new);

    public static final Component FOODS = Component.translatable("item.mineraculous.kwami_foods.foods");
    public static final Component TREATS = Component.translatable("item.mineraculous.kwami_foods.treats");

    public KwamiFoods(Kwami kwami) {
        this(kwami.getFoodsTag(), kwami.getTreatsTag());
    }
}
