package dev.thomasglasser.mineraculous.impl;

import dev.thomasglasser.mineraculous.api.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.api.advancements.critereon.MineraculousEntitySubPredicates;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousBuiltInRegistries;
import dev.thomasglasser.mineraculous.api.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.api.world.ability.AbilitySerializers;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.effect.MineraculousMobEffects;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityDataSerializers;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.api.world.entity.ai.sensing.MineraculousSensorTypes;
import dev.thomasglasser.mineraculous.api.world.entity.npc.MineraculousVillagerProfessions;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmorMaterials;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.api.world.level.storage.loot.parameters.MineraculousLootContextParamSets;
import dev.thomasglasser.mineraculous.impl.commands.MineraculousCommandEvents;
import dev.thomasglasser.mineraculous.impl.core.MineraculousCoreEvents;
import dev.thomasglasser.mineraculous.impl.data.MineraculousDataGenerators;
import dev.thomasglasser.mineraculous.impl.network.MineraculousPayloads;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.impl.world.entity.ai.village.poi.MineraculousPoiTypes;
import dev.thomasglasser.mineraculous.impl.world.entity.npc.MineraculousVillagerTrades;
import dev.thomasglasser.mineraculous.impl.world.item.MineraculousCreativeModeTabs;
import dev.thomasglasser.mineraculous.impl.world.item.crafting.MineraculousRecipeSerializers;
import dev.thomasglasser.mineraculous.impl.world.level.block.MineraculousBlockEvents;
import dev.thomasglasser.mineraculous.impl.world.level.storage.loot.predicates.MineraculousLootItemConditions;
import dev.thomasglasser.mineraculous.impl.world.level.storage.loot.providers.number.MineraculousNumberProviders;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Mineraculous.MOD_ID)
public class Mineraculous {
    public static final String MOD_ID = "mineraculous";
    public static final String MOD_NAME = "Mineraculous";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public Mineraculous(IEventBus modBus, ModContainer modContainer) {
        LOGGER.info("Initializing {} for {} in a {} environment...", MOD_NAME, TommyLibServices.PLATFORM.getPlatformName(), TommyLibServices.PLATFORM.getEnvironmentName());

        initRegistries();

        addEventListeners(modBus);

        modContainer.registerConfig(ModConfig.Type.SERVER, MineraculousServerConfig.get().getConfigSpec());
    }

    private void initRegistries() {
        MineraculousBuiltInRegistries.init();
        MineraculousItems.init();
        MineraculousArmors.init();
        MineraculousCreativeModeTabs.init();
        MineraculousEntityTypes.init();
        MineraculousParticleTypes.init();
        MineraculousBlocks.init();
        MineraculousArmorMaterials.init();
        MineraculousDataComponents.init();
        MineraculousRecipeSerializers.init();
        MineraculousAttachmentTypes.init();
        MineraculousEntityDataSerializers.init();
        MineraculousPoiTypes.init();
        MineraculousVillagerProfessions.init();
        MineraculousCriteriaTriggers.init();
        MineraculousMobEffects.init();
        AbilitySerializers.init();
        MineraculousEntitySubPredicates.init();
        MineraculousSoundEvents.init();
        MineraculousSensorTypes.init();
        MineraculousNumberProviders.init();
        MineraculousLootItemConditions.init();
        MineraculousLootContextParamSets.init();
    }

    private void addEventListeners(IEventBus modBus) {
        // Mod Bus
        modBus.addListener(MineraculousDataGenerators::onGatherData);

        modBus.addListener(MineraculousPayloads::onRegisterPackets);

        modBus.addListener(MineraculousCoreEvents::onFMLCommonSetup);
        modBus.addListener(MineraculousCoreEvents::onNewRegistry);
        modBus.addListener(MineraculousCoreEvents::onNewDataPackRegistry);
        modBus.addListener(MineraculousCoreEvents::onAddPackFinders);
        modBus.addListener(MineraculousCoreEvents::onRegisterDataMapTypes);

        modBus.addListener(MineraculousEntityEvents::onEntityAttributeCreation);

        // Neo Bus
        NeoForge.EVENT_BUS.addListener(MineraculousVillagerTrades::onRegisterVillagerTrades);

        NeoForge.EVENT_BUS.addListener(MineraculousCommandEvents::onCommandsRegister);

        NeoForge.EVENT_BUS.addListener(MineraculousCoreEvents::onServerStarted);
        NeoForge.EVENT_BUS.addListener(MineraculousCoreEvents::onLootTableLoad);

        NeoForge.EVENT_BUS.addListener(MineraculousBlockEvents::onBlockDrops);

        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onEntityJoinLevel);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onServerPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onPreEntityTick);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onPostEntityTick);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onPlayerBreakSpeed);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onLivingFall);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onAttackEntity);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onLivingAttack);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onBlockInteract);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onBlockLeftClick);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onEmptyLeftClick);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onEffectRemoved);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onEffectAdded);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onEffectExpired);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onLivingHeal);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onLivingDrops);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onEntityLeaveLevel);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onPlayerLoggedOut);
    }

    public static ResourceLocation modLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public enum Dependencies {
        CURIOS("curios"),
        MODONOMICON("modonomicon"),
        TOMMYTECH("tommytech");

        private String id;

        Dependencies(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public ResourceLocation modLoc(String s) {
            return ResourceLocation.fromNamespaceAndPath(getId(), s);
        }

        public boolean isLoaded() {
            return ModList.get().isLoaded(getId());
        }
    }
}
