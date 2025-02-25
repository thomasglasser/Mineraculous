package dev.thomasglasser.mineraculous.world.level.storage.loot.providers.number;

import com.google.common.collect.Sets;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.world.level.storage.loot.parameters.MineraculousLootContextParams;
import java.util.Set;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public record PowerLevelMultiplierGenerator(NumberProvider base) implements NumberProvider {
    public static final MapCodec<PowerLevelMultiplierGenerator> CODEC = RecordCodecBuilder.mapCodec(
            p_298748_ -> p_298748_.group(
                    NumberProviders.CODEC.fieldOf("base").forGetter(PowerLevelMultiplierGenerator::base))
                    .apply(p_298748_, PowerLevelMultiplierGenerator::new));

    @Override
    public LootNumberProviderType getType() {
        return MineraculousNumberProviders.POWER_LEVEL.get();
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
        return powerLevel == null ? 1F : Math.max(1, base.getFloat(lootContext) * (powerLevel / 10F));
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Sets.union(this.base.getReferencedContextParams(), Set.of(MineraculousLootContextParams.POWER_LEVEL));
    }
}
