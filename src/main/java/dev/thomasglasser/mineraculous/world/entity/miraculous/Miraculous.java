package dev.thomasglasser.mineraculous.world.entity.miraculous;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.sounds.MineraculousSoundEvents;
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
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record Miraculous(TextColor color, String acceptableSlot, int transformationFrames, ItemStack tool, Optional<Holder<SoundEvent>> kwamiHungrySound, Optional<Holder<Ability>> activeAbility, List<Holder<Ability>> passiveAbilities, Holder<SoundEvent> transformSound, Holder<SoundEvent> detransformSound) {

    public static final Codec<Miraculous> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TextColor.CODEC.fieldOf("color").forGetter(Miraculous::color),
            Codec.STRING.fieldOf("acceptable_slot").forGetter(Miraculous::acceptableSlot),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("transformation_frames", 0).forGetter(Miraculous::transformationFrames),
            ItemStack.OPTIONAL_CODEC.fieldOf("tool").forGetter(Miraculous::tool),
            SoundEvent.CODEC.optionalFieldOf("kwami_hungry_sound").forGetter(Miraculous::kwamiHungrySound),
            Ability.CODEC.optionalFieldOf("active_ability").forGetter(Miraculous::activeAbility),
            Ability.CODEC.listOf().fieldOf("passive_abilities").forGetter(Miraculous::passiveAbilities),
            SoundEvent.CODEC.optionalFieldOf("transform_sound", MineraculousSoundEvents.GENERIC_TRANSFORM).forGetter(Miraculous::transformSound),
            SoundEvent.CODEC.optionalFieldOf("detransform_sound", MineraculousSoundEvents.GENERIC_DETRANSFORM).forGetter(Miraculous::detransformSound)).apply(instance, Miraculous::new));
    public Miraculous(TextColor color, String acceptableSlot, int transformationFrames, ItemStack tool, Optional<Holder<SoundEvent>> kwamiHungrySound, Optional<Holder<Ability>> activeAbility, List<Holder<Ability>> passiveAbilities, Holder<SoundEvent> transformSound) {
        this(color, acceptableSlot, transformationFrames, tool, kwamiHungrySound, activeAbility, passiveAbilities, transformSound, MineraculousSoundEvents.GENERIC_DETRANSFORM);
    }

    public Miraculous(TextColor color, String acceptableSlot, int transformationFrames, ItemStack tool, Optional<Holder<SoundEvent>> kwamiHungrySound, Optional<Holder<Ability>> activeAbility, List<Holder<Ability>> passiveAbilities) {
        this(color, acceptableSlot, transformationFrames, tool, kwamiHungrySound, activeAbility, passiveAbilities, MineraculousSoundEvents.GENERIC_TRANSFORM, MineraculousSoundEvents.GENERIC_DETRANSFORM);
    }

    public Miraculous(TextColor color, String acceptableSlot, ItemStack tool, Optional<Holder<SoundEvent>> kwamiHungrySound, Optional<Holder<Ability>> activeAbility, List<Holder<Ability>> passiveAbilities) {
        this(color, acceptableSlot, 0, tool, kwamiHungrySound, activeAbility, passiveAbilities);
    }

    public Miraculous(TextColor color, String acceptableSlot, ItemStack tool, Optional<Holder<SoundEvent>> kwamiHungrySound, Optional<Holder<Ability>> activeAbility, List<Holder<Ability>> passiveAbilities, Holder<SoundEvent> transformSound) {
        this(color, acceptableSlot, 0, tool, kwamiHungrySound, activeAbility, passiveAbilities, transformSound, MineraculousSoundEvents.GENERIC_DETRANSFORM);
    }

    @Override
    public ItemStack tool() {
        return tool;
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

    public static ItemStack createItemStack(Item item, ResourceKey<Miraculous> key) {
        ItemStack stack = new ItemStack(item);
        stack.set(MineraculousDataComponents.MIRACULOUS, key);
        return stack;
    }
}
