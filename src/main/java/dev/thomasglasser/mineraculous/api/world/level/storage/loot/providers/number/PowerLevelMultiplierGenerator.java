package dev.thomasglasser.mineraculous.api.world.level.storage.loot.providers.number;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.level.storage.loot.parameters.MineraculousLootContextParams;
import dev.thomasglasser.mineraculous.impl.world.level.storage.loot.providers.number.MineraculousNumberProviders;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Set;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

/**
 * Generates a number with the provided {@link NumberProvider}
 * and multiplies it by the provided power level parameter divided by 10.
 *
 * @param base The base number provider to generate the result with
 */
public record PowerLevelMultiplierGenerator(NumberProvider base) implements NumberProvider {
    public static final MapCodec<PowerLevelMultiplierGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberProviders.CODEC.fieldOf("base").forGetter(PowerLevelMultiplierGenerator::base)).apply(instance, PowerLevelMultiplierGenerator::new));

    @Override
    public LootNumberProviderType getType() {
        return MineraculousNumberProviders.POWER_LEVEL_MULTIPLIER.get();
    }

    public static PowerLevelMultiplierGenerator apply(NumberProvider base) {
        return new PowerLevelMultiplierGenerator(base);
    }

    @Override
    public int getInt(LootContext lootContext) {
        Integer powerLevel = lootContext.getParamOrNull(MineraculousLootContextParams.POWER_LEVEL);
        return powerLevel == null ? 1 : Math.max(1, base.getInt(lootContext) * (powerLevel / 10));
    }

    @Override
    public float getFloat(LootContext lootContext) {
        Integer powerLevel = lootContext.getParamOrNull(MineraculousLootContextParams.POWER_LEVEL);
        return powerLevel == null ? 1 : Math.max(1, base.getFloat(lootContext) * (powerLevel / 10F));
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        ReferenceOpenHashSet<LootContextParam<?>> params = new ReferenceOpenHashSet<>(this.base.getReferencedContextParams());
        params.add(MineraculousLootContextParams.POWER_LEVEL);
        return params;
    }
}
