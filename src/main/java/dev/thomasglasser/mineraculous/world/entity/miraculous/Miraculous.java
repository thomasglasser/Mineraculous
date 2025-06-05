package dev.thomasglasser.mineraculous.world.entity.miraculous;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.world.entity.ability.Ability;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record Miraculous(TextColor color, String acceptableSlot, Optional<Integer> transformationFrames, ItemStack tool, Optional<String> toolSlot, Holder<Ability> activeAbility, HolderSet<Ability> passiveAbilities, Holder<SoundEvent> transformSound, Holder<SoundEvent> detransformSound, Holder<SoundEvent> timerBeepSound, Holder<SoundEvent> timerEndSound) {

    public static final Codec<Miraculous> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TextColor.CODEC.fieldOf("color").forGetter(Miraculous::color),
            Codec.STRING.fieldOf("acceptable_slot").forGetter(Miraculous::acceptableSlot),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("transformation_frames").forGetter(Miraculous::transformationFrames),
            ItemStack.SINGLE_ITEM_CODEC.fieldOf("tool").forGetter(Miraculous::tool),
            Codec.STRING.optionalFieldOf("tool_slot").forGetter(Miraculous::toolSlot),
            Ability.CODEC.fieldOf("active_ability").forGetter(Miraculous::activeAbility),
            Ability.HOLDER_SET_CODEC.optionalFieldOf("passive_abilities", HolderSet.empty()).forGetter(Miraculous::passiveAbilities),
            SoundEvent.CODEC.optionalFieldOf("transform_sound", MineraculousSoundEvents.GENERIC_TRANSFORM).forGetter(Miraculous::transformSound),
            SoundEvent.CODEC.optionalFieldOf("detransform_sound", MineraculousSoundEvents.GENERIC_DETRANSFORM).forGetter(Miraculous::detransformSound),
            SoundEvent.CODEC.optionalFieldOf("timer_beep_sound", MineraculousSoundEvents.GENERIC_TIMER_BEEP).forGetter(Miraculous::timerBeepSound),
            SoundEvent.CODEC.optionalFieldOf("timer_end_sound", MineraculousSoundEvents.GENERIC_TIMER_END).forGetter(Miraculous::timerEndSound)).apply(instance, Miraculous::new));
    public Miraculous(TextColor color, String acceptableSlot, Optional<Integer> transformationFrames, ItemStack tool, Optional<String> toolSlot, Holder<Ability> activeAbility, HolderSet<Ability> passiveAbilities) {
        this(color, acceptableSlot, transformationFrames, tool, toolSlot, activeAbility, passiveAbilities, MineraculousSoundEvents.GENERIC_TRANSFORM, MineraculousSoundEvents.GENERIC_DETRANSFORM, MineraculousSoundEvents.GENERIC_TIMER_BEEP, MineraculousSoundEvents.GENERIC_TIMER_END);
    }

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

    public static ItemStack createItemStack(Item item, ResourceKey<Miraculous> key) {
        ItemStack stack = item.getDefaultInstance();
        stack.set(MineraculousDataComponents.MIRACULOUS, key);
        return stack;
    }

    public static ItemStack createMiraculousStack(ResourceKey<Miraculous> key) {
        return createItemStack(MineraculousItems.MIRACULOUS.get(), key);
    }
}
