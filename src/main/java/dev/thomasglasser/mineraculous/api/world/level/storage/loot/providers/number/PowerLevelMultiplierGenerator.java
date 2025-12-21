package dev.thomasglasser.mineraculous.api.world.level.storage.loot.providers.number;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.level.storage.loot.parameters.MineraculousLootContextParams;
import dev.thomasglasser.mineraculous.impl.world.level.storage.loot.providers.number.MineraculousNumberProviders;
import java.util.Set;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

/**
 * Generates a number with the provided {@link NumberProvider}
 * and multiplies it by the provided power level parameter divided by the provided divisor.
 *
 * @param base    The base number provider to generate the result with
 * @param divisor The divisor to divide the power level parameter by
 */
public record PowerLevelMultiplierGenerator(NumberProvider base, int divisor) implements NumberProvider {
    public static final int DEFAULT_DIVISOR = 10;
    public static final MapCodec<PowerLevelMultiplierGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberProviders.CODEC.fieldOf("base").forGetter(PowerLevelMultiplierGenerator::base),
            Codec.INT.optionalFieldOf("divisor", DEFAULT_DIVISOR).forGetter(PowerLevelMultiplierGenerator::divisor)).apply(instance, PowerLevelMultiplierGenerator::new));

    @Override
    public LootNumberProviderType getType() {
        return MineraculousNumberProviders.POWER_LEVEL_MULTIPLIER.get();
    }

    public static PowerLevelMultiplierGenerator apply(NumberProvider base, int divisor) {
        return new PowerLevelMultiplierGenerator(base, divisor);
    }

    public static PowerLevelMultiplierGenerator apply(NumberProvider base) {
        return apply(base, DEFAULT_DIVISOR);
    }

    @Override
    public int getInt(LootContext lootContext) {
        Integer powerLevel = lootContext.getParamOrNull(MineraculousLootContextParams.POWER_LEVEL);
        return powerLevel == null ? 1 : Math.max(1, base.getInt(lootContext) * (powerLevel / divisor));
    }

    @Override
    public float getFloat(LootContext lootContext) {
        Integer powerLevel = lootContext.getParamOrNull(MineraculousLootContextParams.POWER_LEVEL);
        return powerLevel == null ? 1 : Math.max(1, base.getFloat(lootContext) * (powerLevel / (float) divisor));
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        ImmutableSet.Builder<LootContextParam<?>> params = new ImmutableSet.Builder<>();
        params.addAll(base.getReferencedContextParams());
        params.add(MineraculousLootContextParams.POWER_LEVEL);
        return params.build();
    }
}
