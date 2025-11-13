package dev.thomasglasser.mineraculous.impl.data.worldgen;

import com.mojang.datafixers.util.Pair;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.worldgen.lithostitched.registry.LithostitchedRegistryKeys;
import dev.worldgen.lithostitched.worldgen.modifier.AddTemplatePoolElementsModifier;
import dev.worldgen.lithostitched.worldgen.modifier.Modifier;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import org.jetbrains.annotations.Nullable;

public class MineraculousWorldgenModifiers {
    public static void bootstrap(BootstrapContext<Modifier> context) {
        HolderGetter<StructureProcessorList> processors = context.lookup(Registries.PROCESSOR_LIST);
        Holder<StructureProcessorList> mossify10 = processors.getOrThrow(ProcessorLists.MOSSIFY_10_PERCENT);
        Holder<StructureProcessorList> zombieDesert = processors.getOrThrow(ProcessorLists.ZOMBIE_DESERT);
        Holder<StructureProcessorList> zombiePlains = processors.getOrThrow(ProcessorLists.ZOMBIE_PLAINS);
        Holder<StructureProcessorList> zombieSavanna = processors.getOrThrow(ProcessorLists.ZOMBIE_SAVANNA);
        Holder<StructureProcessorList> zombieSnowy = processors.getOrThrow(ProcessorLists.ZOMBIE_SNOWY);
        Holder<StructureProcessorList> zombieTaiga = processors.getOrThrow(ProcessorLists.ZOMBIE_TAIGA);

        // Bakery
        addStructureToVillage(context, "desert", "bakery", 4, null, zombieDesert);
        addStructureToVillage(context, "plains", "bakery", 4, mossify10, zombiePlains);
        addStructureToVillage(context, "savanna", "bakery", 3, null, zombieSavanna);
        addStructureToVillage(context, "snowy", "bakery_1", 2, null, zombieSnowy);
        addStructureToVillage(context, "snowy", "bakery_2", 2, null, zombieSnowy);
        addStructureToVillage(context, "taiga", "bakery", 4, mossify10, zombieTaiga);

        // Creamery
        addStructureToVillage(context, "desert", "creamery", 8, null, zombieDesert);
        addStructureToVillage(context, "plains", "creamery", 10, mossify10, zombiePlains);
        addStructureToVillage(context, "savanna", "creamery", 9, null, zombieSavanna);
        addStructureToVillage(context, "snowy", "creamery", 9, null, zombieSnowy);
        addStructureToVillage(context, "taiga", "creamery", 8, mossify10, zombieTaiga);
    }

    private static void addStructureToVillage(BootstrapContext<Modifier> context, String type, String structure, int weight, @Nullable Holder<StructureProcessorList> processor, Holder<StructureProcessorList> zombieProcessor) {
        HolderGetter<StructureTemplatePool> templatePools = context.lookup(Registries.TEMPLATE_POOL);
        context.register(create("add_" + structure + "_" + type), new AddTemplatePoolElementsModifier(HolderSet.direct(templatePools.getOrThrow(mcPoolKey("village/" + type + "/houses"))), List.of(Pair.of((processor == null ? SinglePoolElement.legacy(MineraculousConstants.modLoc("village/" + type + "/houses/" + structure).toString()) : SinglePoolElement.legacy(MineraculousConstants.modLoc("village/" + type + "/houses/" + structure).toString(), processor)).apply(StructureTemplatePool.Projection.RIGID), weight))));
        context.register(create("add_" + structure + "_" + type + "_zombie"), new AddTemplatePoolElementsModifier(HolderSet.direct(templatePools.getOrThrow(mcPoolKey("village/" + type + "/zombie/houses"))), List.of(Pair.of(SinglePoolElement.legacy(MineraculousConstants.modLoc("village/" + type + "/houses/" + structure).toString(), zombieProcessor).apply(StructureTemplatePool.Projection.RIGID), weight))));
    }

    private static ResourceKey<Modifier> create(String name) {
        return ResourceKey.create(LithostitchedRegistryKeys.WORLDGEN_MODIFIER, MineraculousConstants.modLoc(name));
    }

    private static ResourceKey<StructureTemplatePool> poolKey(ResourceLocation path) {
        return ResourceKey.create(Registries.TEMPLATE_POOL, path);
    }

    private static ResourceKey<StructureTemplatePool> mcPoolKey(String path) {
        return poolKey(ResourceLocation.withDefaultNamespace(path));
    }
}
