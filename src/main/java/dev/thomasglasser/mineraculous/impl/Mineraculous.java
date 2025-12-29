package dev.thomasglasser.mineraculous.impl;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
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
import dev.thomasglasser.mineraculous.api.world.entity.ai.memory.MineraculousMemoryModuleTypes;
import dev.thomasglasser.mineraculous.api.world.entity.ai.sensing.MineraculousSensorTypes;
import dev.thomasglasser.mineraculous.api.world.entity.npc.MineraculousVillagerProfessions;
import dev.thomasglasser.mineraculous.api.world.inventory.MineraculousMenuTypes;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmorMaterials;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.api.world.item.crafting.MineraculousRecipeTypes;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.api.world.level.block.entity.MineraculousBlockEntityTypes;
import dev.thomasglasser.mineraculous.api.world.level.storage.loot.functions.MineraculousLootItemFunctionTypes;
import dev.thomasglasser.mineraculous.api.world.level.storage.loot.parameters.MineraculousLootContextParamSets;
import dev.thomasglasser.mineraculous.impl.commands.MineraculousCommandEvents;
import dev.thomasglasser.mineraculous.impl.core.MineraculousCoreEvents;
import dev.thomasglasser.mineraculous.impl.data.MineraculousDataGenerators;
import dev.thomasglasser.mineraculous.impl.event.MiraculousEvents;
import dev.thomasglasser.mineraculous.impl.network.MineraculousPayloads;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.impl.world.entity.ai.village.poi.MineraculousPoiTypes;
import dev.thomasglasser.mineraculous.impl.world.entity.npc.MineraculousVillagerTrades;
import dev.thomasglasser.mineraculous.impl.world.item.MineraculousCreativeModeTabs;
import dev.thomasglasser.mineraculous.impl.world.item.MineraculousItemEvents;
import dev.thomasglasser.mineraculous.impl.world.item.crafting.MineraculousRecipeSerializers;
import dev.thomasglasser.mineraculous.impl.world.level.block.MineraculousBlockEvents;
import dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget.MiraculousLadybugTargetTypes;
import dev.thomasglasser.mineraculous.impl.world.level.storage.loot.predicates.MineraculousLootItemConditions;
import dev.thomasglasser.mineraculous.impl.world.level.storage.loot.providers.number.MineraculousNumberProviders;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(MineraculousConstants.MOD_ID)
public class Mineraculous {
    public Mineraculous(IEventBus modBus, ModContainer modContainer) {
        MineraculousConstants.LOGGER.info("Initializing {} for {} in a {} environment...", MineraculousConstants.MOD_NAME, TommyLibServices.PLATFORM.getPlatformName(), TommyLibServices.PLATFORM.getEnvironmentName());

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
        MineraculousBlockEntityTypes.init();
        MineraculousMenuTypes.init();
        MineraculousArmorMaterials.init();
        MineraculousDataComponents.init();
        MineraculousRecipeTypes.init();
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
        MineraculousLootItemFunctionTypes.init();
        MineraculousMemoryModuleTypes.init();
        MiraculousLadybugTargetTypes.init();
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
        NeoForge.EVENT_BUS.addListener(MiraculousEvents::onCanEquipMiraculous);
        NeoForge.EVENT_BUS.addListener(MiraculousEvents::onEquipMiraculous);
        NeoForge.EVENT_BUS.addListener(MiraculousEvents::onPreTransformMiraculous);

        NeoForge.EVENT_BUS.addListener(MineraculousVillagerTrades::onRegisterVillagerTrades);

        NeoForge.EVENT_BUS.addListener(MineraculousCommandEvents::onCommandsRegister);

        NeoForge.EVENT_BUS.addListener(MineraculousCoreEvents::onServerStarted);
        NeoForge.EVENT_BUS.addListener(MineraculousCoreEvents::onLootTableLoad);

        NeoForge.EVENT_BUS.addListener(MineraculousBlockEvents::onBlockDrops);

        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onEntityJoinLevel);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onPreEntityTick);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onPostEntityTick);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onLivingFall);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onEntityTeleport);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onAttackEntity);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onPostLivingDamage);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onBlockInteract);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onBlockLeftClick);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onEmptyLeftClick);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onEffectRemoved);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onEffectAdded);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onEffectExpired);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onLivingHeal);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onEntityTravelToDimension);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onLivingSwapHands);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onLivingDrops);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onEntityLeaveLevel);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onPlayerLoggedOut);

        NeoForge.EVENT_BUS.addListener(MineraculousItemEvents::onItemTooltip);
        NeoForge.EVENT_BUS.addListener(MineraculousItemEvents::onItemToss);
    }
}
