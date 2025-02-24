package dev.thomasglasser.mineraculous;

import dev.thomasglasser.mineraculous.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.advancements.critereon.MineraculousEntitySubPredicates;
import dev.thomasglasser.mineraculous.client.MineraculousClientConfig;
import dev.thomasglasser.mineraculous.client.MineraculousClientEvents;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.commands.MineraculousCommandEvents;
import dev.thomasglasser.mineraculous.core.MineraculousCoreEvents;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.core.registries.MineraculousBuiltInRegistries;
import dev.thomasglasser.mineraculous.data.MineraculousDataGenerators;
import dev.thomasglasser.mineraculous.network.MineraculousPayloads;
import dev.thomasglasser.mineraculous.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.effect.MineraculousMobEffects;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityDataSerializers;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.entity.ability.MineraculousAbilitySerializers;
import dev.thomasglasser.mineraculous.world.entity.ai.sensing.MineraculousSensorTypes;
import dev.thomasglasser.mineraculous.world.entity.ai.village.poi.MineraculousPoiTypes;
import dev.thomasglasser.mineraculous.world.entity.npc.MineraculousVillagerProfessions;
import dev.thomasglasser.mineraculous.world.item.MineraculousCreativeModeTabs;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmorMaterials;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.world.item.crafting.MineraculousRecipeSerializers;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.world.level.storage.loot.parameters.MineraculousLootContextParamSets;
import dev.thomasglasser.mineraculous.world.level.storage.loot.predicates.MineraculousLootItemConditions;
import dev.thomasglasser.mineraculous.world.level.storage.loot.providers.number.MineraculousNumberProviders;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Mineraculous.MOD_ID)
public class Mineraculous {
    public static final String MOD_ID = "mineraculous";
    public static final String MOD_NAME = "Mineraculous";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public Mineraculous(IEventBus bus, ModContainer modContainer) {
        LOGGER.info("Initializing {} for {} in a {} environment...", MOD_NAME, TommyLibServices.PLATFORM.getPlatformName(), TommyLibServices.PLATFORM.getEnvironmentName());

        if (FMLEnvironment.production && FMLEnvironment.dist.isClient() && !modContainer.getModInfo().getVersion().getQualifier().isEmpty() && !MineraculousClientUtils.verifySnapshotTester(Minecraft.getInstance().getUser().getProfileId())) {
            throw new RuntimeException("You are running a snapshot version of Mineraculous and are not a part of the Snapshot Program. Please switch to a stable version.");
        }

        MineraculousBuiltInRegistries.init();
        MineraculousItems.init();
        MineraculousArmors.init();
        MineraculousCreativeModeTabs.init();
        MineraculousEntityTypes.init();
        MineraculousPayloads.init();
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
        MineraculousAbilitySerializers.init();
        MineraculousEntitySubPredicates.init();
        MineraculousSoundEvents.init();
        MineraculousSensorTypes.init();
        MineraculousNumberProviders.init();
        MineraculousLootItemConditions.init();
        MineraculousLootContextParamSets.init();

        if (TommyLibServices.PLATFORM.isClientSide()) {
            MineraculousKeyMappings.init();
            MineraculousClientUtils.init();
        }

        registerConfigs(modContainer);

        bus.addListener(MineraculousDataGenerators::onGatherData);

        bus.addListener(MineraculousEntityEvents::onEntityAttributeCreation);
        bus.addListener(MineraculousCoreEvents::onRegisterPackets);
        bus.addListener(MineraculousCoreEvents::onNewDataPackRegistry);
        bus.addListener(MineraculousCoreEvents::onNewRegistry);
        bus.addListener(MineraculousCoreEvents::onRegisterDataMapTypes);
        bus.addListener(MineraculousCoreEvents::onAddPackFinders);

        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onBlockInteract);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onAttackEntity);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onLivingAttack);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onBlockLeftClick);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onEmptyLeftClick);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onEffectRemoved);
        NeoForge.EVENT_BUS.addListener(MineraculousCommandEvents::onCommandsRegister);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onLivingHeal);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onEntityTick);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onRegisterVillagerTrades);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onEntityJoinLevel);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(MineraculousCoreEvents::onLoadLootTable);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onPlayerBreakSpeed);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onServerPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(MineraculousCoreEvents::onServerStarted);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onMobEffectRemoved);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onBlockDrops);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onLivingDrops);

        if (TommyLibServices.PLATFORM.isClientSide()) {
            bus.addListener(MineraculousClientEvents::onRegisterAdditionalModels);
            bus.addListener(MineraculousClientEvents::onRegisterRenderer);
            bus.addListener(MineraculousClientEvents::onFMLClientSetup);
            bus.addListener(MineraculousClientEvents::onRegisterParticleProviders);
            bus.addListener(MineraculousClientEvents::onRegisterGuiLayers);
            bus.addListener(MineraculousClientEvents::onRegisterEntitySpectatorShaders);
            bus.addListener(MineraculousClientEvents::onRegisterLayerDefinitions);
            bus.addListener(MineraculousClientEvents::onAddLayers);
            bus.addListener(MineraculousClientEvents::onRegisterItemColorHandlers);
            bus.addListener(MineraculousClientEvents::onBuildCreativeModeTabContents);
            bus.addListener(MineraculousClientEvents::onClientConfigChanged);
            bus.addListener(MineraculousClientEvents::onRegisterRenderBuffers);

            NeoForge.EVENT_BUS.addListener(MineraculousClientEvents::onGetPlayerHeartType);
            NeoForge.EVENT_BUS.addListener(MineraculousClientEvents::onRenderHand);
            NeoForge.EVENT_BUS.addListener(MineraculousClientEvents::onKeyInput);
            NeoForge.EVENT_BUS.addListener(MineraculousClientEvents::onMouseScrollingInput);
            NeoForge.EVENT_BUS.addListener(MineraculousClientEvents::onMouseButtonClick);
            NeoForge.EVENT_BUS.addListener(MineraculousClientEvents::onClientTick);
            NeoForge.EVENT_BUS.addListener(MineraculousClientEvents::onClientChatReceived);
            NeoForge.EVENT_BUS.addListener(MineraculousClientEvents::onPlayerLoggedIn);
            NeoForge.EVENT_BUS.addListener(MineraculousClientEvents::onInteractionKeyMappingTriggered);
            NeoForge.EVENT_BUS.addListener(MineraculousClientEvents::onPlayerRendererPre);
        }
    }

    private static void registerConfigs(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, MineraculousServerConfig.get().getConfigSpec());
        modContainer.registerConfig(ModConfig.Type.CLIENT, MineraculousClientConfig.get().getConfigSpec());
        if (FMLEnvironment.dist.isClient()) modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    public static ResourceLocation modLoc(String s) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, s);
    }

    public enum Dependencies {
        CURIOS("curios"),
        MODONOMICON("modonomicon");

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
    }
}
