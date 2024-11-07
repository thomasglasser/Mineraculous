package dev.thomasglasser.mineraculous.world.entity.miraculous;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.world.entity.miraculous.ability.Ability;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record Miraculous(TextColor color, List<String> includedLooks, String acceptableSlot, ItemStack tool, Optional<Holder<SoundEvent>> kwamiHungrySound, Optional<Holder<Ability>> activeAbility, List<Holder<Ability>> passiveAbilities) {

    public static final Codec<Miraculous> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TextColor.CODEC.fieldOf("color").forGetter(Miraculous::color),
            Codec.STRING.listOf().optionalFieldOf("included_looks", List.of()).forGetter(Miraculous::includedLooks),
            Codec.STRING.fieldOf("acceptable_slot").forGetter(Miraculous::acceptableSlot),
            ItemStack.OPTIONAL_CODEC.fieldOf("tool").forGetter(Miraculous::tool),
            SoundEvent.CODEC.optionalFieldOf("kwami_hungry_sound").forGetter(Miraculous::kwamiHungrySound),
            Ability.CODEC.optionalFieldOf("active_ability").forGetter(Miraculous::activeAbility),
            Ability.CODEC.listOf().fieldOf("passive_abilities").forGetter(Miraculous::passiveAbilities)).apply(instance, Miraculous::new));
    @Override
    public ItemStack tool() {
        return tool.copy();
    }

    public static String toLanguageKey(ResourceKey<Miraculous> key) {
        return key.location().toLanguageKey(key.registry().getPath());
    }

    public static TagKey<Item> createFoodsTag(ResourceKey<Miraculous> key) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(key.location().getNamespace(), "kwami_foods/" + key.location().getPath()));
    }

    public static TagKey<Item> createTreatsTag(ResourceKey<Miraculous> key) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(key.location().getNamespace(), "kwami_treats/" + key.location().getPath()));
    }
}
