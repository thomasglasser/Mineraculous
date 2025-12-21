package dev.thomasglasser.mineraculous.api.world.level.storage.loot.parameters;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousLootContextParamSets {
    /// Used in {@link dev.thomasglasser.mineraculous.api.world.ability.SummonTargetDependentLuckyCharmAbility}.
    public static final LootContextParamSet LUCKY_CHARM = register("lucky_charm", builder -> builder
            .required(LootContextParams.THIS_ENTITY)
            .required(LootContextParams.ORIGIN)
            .required(MineraculousLootContextParams.POWER_LEVEL)
            .optional(LootContextParams.ATTACKING_ENTITY)
            .optional(LootContextParams.TOOL)
            .optional(LootContextParams.DAMAGE_SOURCE)
            .build());

    private static LootContextParamSet register(String name, Consumer<LootContextParamSet.Builder> builderConsumer) {
        LootContextParamSet.Builder builder = new LootContextParamSet.Builder();
        builderConsumer.accept(builder);
        LootContextParamSet paramSet = builder.build();
        ResourceLocation loc = MineraculousConstants.modLoc(name);
        LootContextParamSet existing = LootContextParamSets.REGISTRY.put(loc, paramSet);
        if (existing != null) {
            throw new IllegalStateException("Loot table parameter set " + loc + " is already registered");
        } else {
            return paramSet;
        }
    }

    @ApiStatus.Internal
    public static void init() {}
}
