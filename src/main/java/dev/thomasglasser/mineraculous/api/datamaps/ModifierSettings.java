package dev.thomasglasser.mineraculous.api.datamaps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record ModifierSettings(double amount, AttributeModifier.Operation operation) {
    public static final Codec<ModifierSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("amount").forGetter(ModifierSettings::amount),
            AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(ModifierSettings::operation))
            .apply(instance, instance.stable(ModifierSettings::new)));
}
