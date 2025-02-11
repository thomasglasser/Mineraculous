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
            .required(LootContextParams.TOOL)
            .optional(LootContextParams.DAMAGE_SOURCE)
            .optional(LootContextParams.ATTACKING_ENTITY)
            .optional(MineraculousLootContextParams.POWER_LEVEL)
            .build());

    private static LootContextParamSet register(String registryName, Consumer<LootContextParamSet.Builder> builderConsumer) {
        LootContextParamSet.Builder lootcontextparamset$builder = new LootContextParamSet.Builder();
        builderConsumer.accept(lootcontextparamset$builder);
        LootContextParamSet lootcontextparamset = lootcontextparamset$builder.build();
        ResourceLocation resourcelocation = Mineraculous.modLoc(registryName);
        LootContextParamSet lootcontextparamset1 = LootContextParamSets.REGISTRY.put(resourcelocation, lootcontextparamset);
        if (lootcontextparamset1 != null) {
            throw new IllegalStateException("Loot table parameter set " + resourcelocation + " is already registered");
        } else {
            return lootcontextparamset;
        }
    }

    public static void init() {}
}
