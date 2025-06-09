package dev.thomasglasser.mineraculous.world.level.storage.loot.parameters;

import dev.thomasglasser.mineraculous.Mineraculous;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class MineraculousLootContextParamSets {
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
        ResourceLocation loc = Mineraculous.modLoc(name);
        LootContextParamSet existing = LootContextParamSets.REGISTRY.put(loc, paramSet);
        if (existing != null) {
            throw new IllegalStateException("Loot table parameter set " + loc + " is already registered");
        } else {
            return paramSet;
        }
    }

    public static void init() {}
}
