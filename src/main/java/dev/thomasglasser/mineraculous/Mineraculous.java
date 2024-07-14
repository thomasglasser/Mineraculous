package dev.thomasglasser.mineraculous;

import dev.thomasglasser.mineraculous.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.client.MineraculousClientConfig;
import dev.thomasglasser.mineraculous.client.MineraculousClientEvents;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.commands.MineraculousCommandEvents;
import dev.thomasglasser.mineraculous.commands.arguments.MineraculousCommandArgumentTypes;
import dev.thomasglasser.mineraculous.core.MineraculousCoreEvents;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.data.MineraculousDataGenerators;
import dev.thomasglasser.mineraculous.network.MineraculousPayloads;
import dev.thomasglasser.mineraculous.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.effect.MineraculousMobEffects;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityDataSerializers;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.entity.ai.village.poi.MineraculousPoiTypes;
import dev.thomasglasser.mineraculous.world.entity.npc.MineraculousVillagerProfessions;
import dev.thomasglasser.mineraculous.world.item.MineraculousCreativeModeTabs;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmorMaterials;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.world.item.crafting.MineraculousRecipeSerializers;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.world.level.storage.loot.modifier.MineraculousLootModifiers;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
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

    public Mineraculous(IEventBus bus, ModContainer modContainer) {
        LOGGER.info("Initializing {} for {} in a {} environment...", MOD_NAME, TommyLibServices.PLATFORM.getPlatformName(), TommyLibServices.PLATFORM.getEnvironmentName());

        MineraculousItems.init();
        MineraculousArmors.init();
        MineraculousCreativeModeTabs.init();
        MineraculousEntityTypes.init();
        MineraculousKeyMappings.init();
        MineraculousPayloads.init();
        MineraculousParticleTypes.init();
        MineraculousCommandArgumentTypes.init();
        MineraculousBlocks.init();
        MineraculousArmorMaterials.init();
        MineraculousDataComponents.init();
        MineraculousRecipeSerializers.init();
        MineraculousAttachmentTypes.init();
        MineraculousLootModifiers.init();
        MineraculousEntityDataSerializers.init();
        MineraculousPoiTypes.init();
        MineraculousVillagerProfessions.init();
        MineraculousCriteriaTriggers.init();
        MineraculousMobEffects.init();

        registerConfigs(modContainer);

        if (TommyLibServices.PLATFORM.isClientSide()) MineraculousClientUtils.init();

        bus.addListener(MineraculousDataGenerators::onGatherData);

        addModListeners(bus);

        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onBlockInteract);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onAttackEntity);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onLivingAttack);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onBlockLeftClick);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onEffectRemoved);
        NeoForge.EVENT_BUS.addListener(MineraculousCommandEvents::onCommandsRegister);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onLivingHeal);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onLivingTick);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(MineraculousEntityEvents::onRegisterVillagerTrades);

        if (TommyLibServices.PLATFORM.isClientSide()) {
            bus.addListener(MineraculousClientEvents::onRegisterAdditionalModels);
            bus.addListener(MineraculousClientEvents::onRegisterRenderer);
            bus.addListener(MineraculousClientEvents::onFMLClientSetup);
            bus.addListener(MineraculousClientEvents::onRegisterParticleProviders);
            bus.addListener(MineraculousClientEvents::onRegisterGuiLayers);

            NeoForge.EVENT_BUS.addListener(MineraculousClientEvents::onEntityJoinLevel);
            NeoForge.EVENT_BUS.addListener(MineraculousClientEvents::onGetPlayerHeartType);
        }
    }

    private void addModListeners(IEventBus bus) {
        bus.addListener(MineraculousEntityEvents::onEntityAttributeCreation);
        bus.addListener(MineraculousCoreEvents::onRegisterPackets);
    }

    private static void registerConfigs(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, MineraculousServerConfig.INSTANCE.getConfigSpec());
        modContainer.registerConfig(ModConfig.Type.CLIENT, MineraculousClientConfig.INSTANCE.getConfigSpec());
    }

    public static ResourceLocation modLoc(String s) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, s);
    }

    public enum Dependencies {
        CURIOS("curios");

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
