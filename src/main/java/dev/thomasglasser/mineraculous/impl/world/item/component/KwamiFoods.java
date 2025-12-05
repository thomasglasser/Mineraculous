package dev.thomasglasser.mineraculous.impl.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.impl.world.entity.Kwami;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public record KwamiFoods(TagKey<Item> preferredFoods, TagKey<Item> treats, int defaultEatTicks, boolean isLeftHanded) {

    public static final Codec<KwamiFoods> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TagKey.codec(Registries.ITEM).fieldOf("preferred_foods").forGetter(KwamiFoods::preferredFoods),
            TagKey.codec(Registries.ITEM).fieldOf("treats").forGetter(KwamiFoods::treats),
            Codec.INT.fieldOf("default_eat_ticks").forGetter(KwamiFoods::defaultEatTicks),
            Codec.BOOL.optionalFieldOf("is_left_handed", false).forGetter(KwamiFoods::isLeftHanded)).apply(instance, KwamiFoods::new));
    private static final StreamCodec<ByteBuf, TagKey<Item>> ITEM_TAG_STREAM_CODEC = ResourceLocation.STREAM_CODEC.map(loc -> TagKey.create(Registries.ITEM, loc), TagKey::location);
    public static final StreamCodec<RegistryFriendlyByteBuf, KwamiFoods> STREAM_CODEC = StreamCodec.composite(
            ITEM_TAG_STREAM_CODEC, KwamiFoods::preferredFoods,
            ITEM_TAG_STREAM_CODEC, KwamiFoods::treats,
            ByteBufCodecs.INT, KwamiFoods::defaultEatTicks,
            ByteBufCodecs.BOOL, KwamiFoods::isLeftHanded,
            KwamiFoods::new);

    public static final Component PREFERRED_FOODS = Component.translatable("item.mineraculous.kwami_foods.preferred_foods");
    public static final Component TREATS = Component.translatable("item.mineraculous.kwami_foods.treats");
    public KwamiFoods(Kwami kwami) {
        this(kwami.getPreferredFoodsTag(), kwami.getTreatsTag(), kwami.getDefaultEatTicks(), kwami.isLeftHanded());
    }
}
