package dev.thomasglasser.mineraculous.api.datamaps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

/**
 * Specifies an amplifier to apply for an effect.
 * 
 * @param amplifier The amplifier to apply
 */
public record EffectAmplifier(int amplifier) {
    public static final Codec<EffectAmplifier> EFFECT_AMPLIFIER_CODEC = ExtraCodecs.intRange(0, 255)
            .xmap(EffectAmplifier::new, EffectAmplifier::amplifier);
    public static final Codec<EffectAmplifier> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(instance -> instance.group(
                    ExtraCodecs.intRange(0, 255).fieldOf("amplifier").forGetter(EffectAmplifier::amplifier)).apply(instance, EffectAmplifier::new)),
            EFFECT_AMPLIFIER_CODEC);
}
