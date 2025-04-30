package dev.thomasglasser.mineraculous.client;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.renderer.MineraculousRenderTypeEvents;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Mineraculous.MOD_ID, dist = Dist.CLIENT)
public class MineraculousClient {
    public MineraculousClient(IEventBus modBus, ModContainer modContainer) {
        if (FMLEnvironment.production && !modContainer.getModInfo().getVersion().getQualifier().isEmpty() && !MineraculousClientUtils.verifySnapshotTester(Minecraft.getInstance().getUser().getProfileId())) {
            throw new RuntimeException("You are running a snapshot version of Mineraculous and are not a part of the Snapshot Program. Please switch to a stable version.");
        }

        MineraculousKeyMappings.init();

        modContainer.registerConfig(ModConfig.Type.CLIENT, MineraculousClientConfig.get().getConfigSpec());
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modBus.addListener(MineraculousClientEvents::onRegisterAdditionalModels);
        modBus.addListener(MineraculousClientEvents::onRegisterRenderer);
        modBus.addListener(MineraculousClientEvents::onFMLClientSetup);
        modBus.addListener(MineraculousClientEvents::onRegisterParticleProviders);
        modBus.addListener(MineraculousClientEvents::onRegisterGuiLayers);
        modBus.addListener(MineraculousClientEvents::onRegisterEntitySpectatorShaders);
        modBus.addListener(MineraculousClientEvents::onRegisterLayerDefinitions);
        modBus.addListener(MineraculousClientEvents::onAddLayers);
        modBus.addListener(MineraculousClientEvents::onRegisterItemColorHandlers);
        modBus.addListener(MineraculousClientEvents::onBuildCreativeModeTabContents);
        modBus.addListener(MineraculousClientEvents::onClientConfigChanged);
        modBus.addListener(MineraculousClientEvents::onRegisterClientReloadListeners);
        modBus.addListener(MineraculousRenderTypeEvents::onRegisterRenderBuffers);
        modBus.addListener(MineraculousRenderTypeEvents::onRegisterShaders);

        NeoForge.EVENT_BUS.addListener(MineraculousClientEvents::onGetPlayerHeartType);
        NeoForge.EVENT_BUS.addListener(MineraculousClientEvents::onRenderHand);
        NeoForge.EVENT_BUS.addListener(MineraculousClientEvents::onKeyInput);
        NeoForge.EVENT_BUS.addListener(MineraculousClientEvents::onMouseScrollingInput);
        NeoForge.EVENT_BUS.addListener(MineraculousClientEvents::onMouseButtonClick);
        NeoForge.EVENT_BUS.addListener(MineraculousClientEvents::onClientTick);
        NeoForge.EVENT_BUS.addListener(MineraculousClientEvents::onClientChatReceived);
        NeoForge.EVENT_BUS.addListener(MineraculousClientEvents::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(MineraculousClientEvents::onInteractionKeyMappingTriggered);
    }
}
