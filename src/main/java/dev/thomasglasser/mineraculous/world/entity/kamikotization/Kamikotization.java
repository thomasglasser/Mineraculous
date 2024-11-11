package dev.thomasglasser.mineraculous.world.entity.kamikotization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.world.entity.miraculous.ability.Ability;
import java.util.List;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;

public record Kamikotization(List<String> includedLooks, ItemPredicate itemPredicate, List<Holder<Ability>> abilities) {
    public static final Codec<Kamikotization> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().fieldOf("included_looks").forGetter(Kamikotization::includedLooks),
            ItemPredicate.CODEC.fieldOf("item_predicate").forGetter(Kamikotization::itemPredicate),
            Ability.CODEC.listOf().fieldOf("abilities").forGetter(Kamikotization::abilities)).apply(instance, Kamikotization::new));
}
