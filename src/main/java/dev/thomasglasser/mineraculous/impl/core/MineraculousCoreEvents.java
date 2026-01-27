package dev.thomasglasser.mineraculous.impl.core;

import com.google.common.collect.ImmutableList;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.dispenser.ActiveProjectileDispenseBehavior;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousBuiltInRegistries;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.datamaps.MineraculousDataMaps;
import dev.thomasglasser.mineraculous.api.packs.MineraculousPacks;
import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.core.look.LookLoader;
import dev.thomasglasser.mineraculous.impl.server.look.ServerLookManager;
import dev.thomasglasser.tommylib.api.packs.PackInfo;
import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

public class MineraculousCoreEvents {
    // Setup
    public static void onFMLCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            DispenserBlock.registerBehavior(MineraculousItems.CAT_STAFF, new ActiveProjectileDispenseBehavior(MineraculousItems.CAT_STAFF.get()));
            DispenserBlock.registerProjectileBehavior(MineraculousItems.BUTTERFLY_CANE);
        });
    }

    // Registration
    public static void onNewRegistry(NewRegistryEvent event) {
        event.register(MineraculousBuiltInRegistries.ABILITY_SERIALIZER);
        event.register(MineraculousBuiltInRegistries.MIRACULOUS_LADYBUG_TARGET_TYPE);
        event.register(MineraculousBuiltInRegistries.LOOK_CONTEXT);
        event.register(MineraculousBuiltInRegistries.LOOK_METADATA_TYPE);
    }

    public static void onNewDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(MineraculousRegistries.MIRACULOUS, Miraculous.DIRECT_CODEC, Miraculous.DIRECT_CODEC);
        event.dataPackRegistry(MineraculousRegistries.ABILITY, Ability.DIRECT_CODEC, Ability.DIRECT_CODEC);
        event.dataPackRegistry(MineraculousRegistries.KAMIKOTIZATION, Kamikotization.DIRECT_CODEC, Kamikotization.DIRECT_CODEC);
    }

    public static void onAddPackFinders(AddPackFindersEvent event) {
        for (PackInfo info : MineraculousPacks.getPacks()) {
            if (event.getPackType() == info.type()) {
                Path resourcePath = ModList.get().getModFileById(MineraculousConstants.MOD_ID).getFile().findResource("packs/" + info.knownPack().namespace() + "/" + info.knownPack().id());
                Pack pack = Pack.readMetaAndCreate(new PackLocationInfo("builtin/" + info.knownPack().id(), info.title(), info.source(), Optional.of(info.knownPack())), new Pack.ResourcesSupplier() {
                    @Override
                    public PackResources openFull(PackLocationInfo p_326241_, Pack.Metadata p_325959_) {
                        return new PathPackResources(p_326241_, resourcePath);
                    }

                    @Override
                    public PackResources openPrimary(PackLocationInfo p_326301_) {
                        return new PathPackResources(p_326301_, resourcePath);
                    }
                }, info.type(), PackInfo.BUILT_IN_SELECTION_CONFIG);
                event.addRepositorySource((packConsumer) -> packConsumer.accept(pack));
            }
        }
    }

    public static void onRegisterDataMapTypes(RegisterDataMapTypesEvent event) {
        event.register(MineraculousDataMaps.KAMIKOTIZATION_LUCKY_CHARMS);
        event.register(MineraculousDataMaps.MIRACULOUS_LUCKY_CHARMS);
        event.register(MineraculousDataMaps.ENTITY_LUCKY_CHARMS);

        event.register(MineraculousDataMaps.MIRACULOUS_EFFECTS);
        event.register(MineraculousDataMaps.MIRACULOUS_ATTRIBUTE_MODIFIERS);

        event.register(MineraculousDataMaps.AGEABLES);
    }

    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener((preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor) -> preparationBarrier.wait(Unit.INSTANCE).thenRunAsync(() -> {
            ServerLookManager.refresh();
            LookLoader.loadLoaded((hash, root, source, file, equippable) -> ServerLookManager.add(hash, source, equippable));
        }, gameExecutor));
    }

    // Start
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        if (server.registryAccess().registryOrThrow(MineraculousRegistries.KAMIKOTIZATION).size() == 0) {
            MineraculousConstants.LOGGER.warn(Kamikotization.NO_KAMIKOTIZATIONS.getString());
        }
    }

    // Misc
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation name = event.getName();
        LootTable table = event.getTable();
        if (name.equals(BuiltInLootTables.SNIFFER_DIGGING.location())) {
            addLootToTable(table, LootItem.lootTableItem(MineraculousBlocks.HIBISCUS_BUSH.asItem()));
        } else if (name.equals(BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY.location())) {
            addLootToTable(table,
                    LootItem.lootTableItem(MineraculousItems.LADYBUG_POTTERY_SHERD),
                    LootItem.lootTableItem(MineraculousItems.CAT_POTTERY_SHERD),
                    LootItem.lootTableItem(MineraculousItems.BUTTERFLY_POTTERY_SHERD),
                    LootItem.lootTableItem(MineraculousItems.BUTTERFLY_ARMOR_TRIM_SMITHING_TEMPLATE),
                    LootItem.lootTableItem(MineraculousItems.CAT_ARMOR_TRIM_SMITHING_TEMPLATE),
                    LootItem.lootTableItem(MineraculousItems.LADYBUG_ARMOR_TRIM_SMITHING_TEMPLATE));
        }
    }

    public static void addLootToTable(LootTable table, LootPoolEntryContainer.Builder<?>... entries) {
        LootPool main = table.getPool("main");
        if (main != null) {
            ImmutableList.Builder<LootPoolEntryContainer> list = new ImmutableList.Builder<>();
            list.addAll(main.entries);
            for (LootPoolEntryContainer.Builder<?> entry : entries) {
                list.add(entry.build());
            }
            main.entries = list.build();
        }
    }
}
