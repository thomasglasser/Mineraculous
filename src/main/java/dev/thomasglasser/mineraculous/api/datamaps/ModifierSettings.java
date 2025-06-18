package dev.thomasglasser.mineraculous.api.datamaps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Defines how an attribute modifier should be applied.
 * @param amount The amount to apply
 * @param operation The operation to use
 */
public record ModifierSettings(double amount, AttributeModifier.Operation operation) {
    public static final Codec<ModifierSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("amount").forGetter(ModifierSettings::amount),
            AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(ModifierSettings::operation))
            .apply(instance, ModifierSettings::new));
}
