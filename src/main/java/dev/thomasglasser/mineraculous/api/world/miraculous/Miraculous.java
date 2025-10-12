package dev.thomasglasser.mineraculous.api.world.miraculous;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Data-driven entity transformation provided by a {@link MiraculousItem}.
 *
 * @param color                The color of the Miraculous and holder name
 * @param acceptableSlot       The Curios slot the {@link MiraculousItem} can be placed in to transform
 * @param transformationFrames The number of frames it takes to transform
 * @param tool                 The tool to give when transformed
 * @param toolSlot             The Curios slot to place the tool when given
 * @param activeAbility        The {@link Ability} that provides the main power
 * @param passiveAbilities     Passive {@link Ability}s
 * @param transformSound       The sound to play when transforming
 * @param detransformSound     The sound to play when detransforming
 * @param timerWarningSound    The sound to play when the detransformation timer hits a milestone
 * @param timerEndSound        The sound to play when the detransformation timer is over
 */
public record Miraculous(TextColor color, String acceptableSlot, Optional<Integer> transformationFrames, ItemStack tool, Optional<String> toolSlot, Holder<Ability> activeAbility, HolderSet<Ability> passiveAbilities, Holder<SoundEvent> transformSound, Holder<SoundEvent> detransformSound, Holder<SoundEvent> timerWarningSound, Holder<SoundEvent> timerEndSound) {

    public static final Codec<Miraculous> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TextColor.CODEC.fieldOf("color").forGetter(Miraculous::color),
            Codec.STRING.fieldOf("acceptable_slot").forGetter(Miraculous::acceptableSlot),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("transformation_frames").forGetter(Miraculous::transformationFrames),
            ItemStack.SINGLE_ITEM_CODEC.fieldOf("tool").forGetter(Miraculous::tool),
            Codec.STRING.optionalFieldOf("tool_slot").forGetter(Miraculous::toolSlot),
            Ability.CODEC.fieldOf("active_ability").forGetter(Miraculous::activeAbility),
            Ability.HOLDER_SET_CODEC.optionalFieldOf("passive_abilities", HolderSet.empty()).forGetter(Miraculous::passiveAbilities),
            SoundEvent.CODEC.optionalFieldOf("transform_sound", MineraculousSoundEvents.GENERIC_TRANSFORM).forGetter(Miraculous::transformSound),
            SoundEvent.CODEC.optionalFieldOf("detransform_sound", MineraculousSoundEvents.GENERIC_DETRANSFORM).forGetter(Miraculous::detransformSound),
            SoundEvent.CODEC.optionalFieldOf("timer_warning_sound", MineraculousSoundEvents.GENERIC_TIMER_WARNING).forGetter(Miraculous::timerWarningSound),
            SoundEvent.CODEC.optionalFieldOf("timer_end_sound", MineraculousSoundEvents.GENERIC_TIMER_END).forGetter(Miraculous::timerEndSound)).apply(instance, Miraculous::new));
    public static final Codec<Holder<Miraculous>> CODEC = RegistryFixedCodec.create(MineraculousRegistries.MIRACULOUS);

    public static final StreamCodec<RegistryFriendlyByteBuf, Miraculous> DIRECT_STREAM_CODEC = TommyLibExtraStreamCodecs.composite(
            ByteBufCodecs.fromCodec(TextColor.CODEC), Miraculous::color,
            ByteBufCodecs.STRING_UTF8, Miraculous::acceptableSlot,
            ByteBufCodecs.optional(ByteBufCodecs.INT), Miraculous::transformationFrames,
            ItemStack.STREAM_CODEC, Miraculous::tool,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), Miraculous::toolSlot,
            Ability.STREAM_CODEC, Miraculous::activeAbility,
            Ability.HOLDER_SET_STREAM_CODEC, Miraculous::passiveAbilities,
            SoundEvent.STREAM_CODEC, Miraculous::transformSound,
            SoundEvent.STREAM_CODEC, Miraculous::detransformSound,
            SoundEvent.STREAM_CODEC, Miraculous::timerWarningSound,
            SoundEvent.STREAM_CODEC, Miraculous::timerEndSound,
            Miraculous::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Miraculous>> STREAM_CODEC = ByteBufCodecs.holder(MineraculousRegistries.MIRACULOUS, DIRECT_STREAM_CODEC);
    public Miraculous(TextColor color, String acceptableSlot, Optional<Integer> transformationFrames, ItemStack tool, Optional<String> toolSlot, Holder<Ability> activeAbility, HolderSet<Ability> passiveAbilities) {
        this(color, acceptableSlot, transformationFrames, tool, toolSlot, activeAbility, passiveAbilities, MineraculousSoundEvents.GENERIC_TRANSFORM, MineraculousSoundEvents.GENERIC_DETRANSFORM, MineraculousSoundEvents.GENERIC_TIMER_WARNING, MineraculousSoundEvents.GENERIC_TIMER_END);
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

    public static ItemStack createItemStack(ItemLike item, Holder<Miraculous> miraculous) {
        ItemStack stack = item.asItem().getDefaultInstance();
        stack.set(MineraculousDataComponents.MIRACULOUS, miraculous);
        return stack;
    }

    public static ItemStack createMiraculousStack(Holder<Miraculous> miraculous) {
        return createItemStack(MineraculousItems.MIRACULOUS, miraculous);
    }

    public static Component formatItemName(ItemStack stack, Component original) {
        Holder<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
        if (miraculous != null) {
            ResourceKey<Miraculous> key = miraculous.getKey();
            if (key != null) {
                return Component.translatable(toLanguageKey(key)).append(" ").append(original).withColor(miraculous.value().color().getValue());
            }
        }
        return original;
    }
}
