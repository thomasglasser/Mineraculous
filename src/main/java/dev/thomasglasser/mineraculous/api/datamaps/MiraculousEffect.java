package dev.thomasglasser.mineraculous.api.datamaps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

/**
 * Specifies an amplifier to apply for an effect.
 * 
 * @param amplifier  The amplifier to apply
 * @param toggleable Whether the effect can be toggled by {@link dev.thomasglasser.mineraculous.impl.client.MineraculousKeyMappings#TOGGLE_BUFFS}
 */
public record MiraculousEffect(int amplifier, boolean toggleable) {
    public static final Codec<MiraculousEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.intRange(0, 255).fieldOf("amplifier").forGetter(MiraculousEffect::amplifier),
            Codec.BOOL.optionalFieldOf("toggleable", false).forGetter(MiraculousEffect::toggleable)).apply(instance, MiraculousEffect::new));

    public MiraculousEffect(int amplifier) {
        this(amplifier, false);
    }
}
