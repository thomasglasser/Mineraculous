package dev.thomasglasser.mineraculous.client;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.gui.MineraculousHeartTypes;
import dev.thomasglasser.mineraculous.client.gui.components.kamiko.KamikoGui;
import dev.thomasglasser.mineraculous.client.gui.screens.KamikotizationChatScreen;
import dev.thomasglasser.mineraculous.client.gui.screens.RadialMenuOption;
import dev.thomasglasser.mineraculous.client.gui.screens.RadialMenuScreen;
import dev.thomasglasser.mineraculous.client.model.KamikoMaskModel;
import dev.thomasglasser.mineraculous.client.particle.CataclysmParticle;
import dev.thomasglasser.mineraculous.client.renderer.entity.KamikoRenderer;
import dev.thomasglasser.mineraculous.client.renderer.entity.KwamiRenderer;
import dev.thomasglasser.mineraculous.client.renderer.entity.ThrownCatStaffRenderer;
import dev.thomasglasser.mineraculous.client.renderer.entity.layers.KamikoMaskLayer;
import dev.thomasglasser.mineraculous.client.renderer.item.MineraculousItemProperties;
import dev.thomasglasser.mineraculous.client.renderer.item.curio.MiraculousRenderer;
import dev.thomasglasser.mineraculous.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterEntitySpectatorShadersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.event.entity.player.PlayerHeartTypeEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

public class MineraculousClientEvents {
    private static KamikoGui kamikoGui;

    public static void onFMLClientSetup(FMLClientSetupEvent event) {
        CuriosRendererRegistry.register(MineraculousItems.MIRACULOUS.get(), MiraculousRenderer::new);

        MineraculousItemProperties.init();
    }

    public static void openToolWheel(ResourceKey<Miraculous> miraculousType, ItemStack stack, Consumer<RadialMenuOption> onSelected, RadialMenuOption... options) {
        if (ClientUtils.getMinecraft().screen == null) {
            ClientUtils.setScreen(new RadialMenuScreen(Arrays.asList(options), stack, onSelected, MineraculousKeyMappings.OPEN_TOOL_WHEEL.getKey().getValue(), ClientUtils.getLevel().holderOrThrow(miraculousType).value().color().getValue()));
        }
    }

    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        Map<ResourceLocation, Resource> map = manager.listResources("models/item/miraculous", (location -> location.getPath().endsWith(".json")));
        for (ResourceLocation rl : map.keySet()) {
            ResourceLocation stripped = ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), rl.getPath().substring("models/".length(), rl.getPath().indexOf(".json")));
            event.register(ModelResourceLocation.standalone(stripped));
        }

        event.register(ModelResourceLocation.standalone(Mineraculous.modLoc("item/cat_staff_extended")));
    }

    public static void onRegisterRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MineraculousEntityTypes.KWAMI.get(), KwamiRenderer::new);
        event.registerEntityRenderer(MineraculousEntityTypes.KAMIKO.get(), KamikoRenderer::new);
        event.registerEntityRenderer(MineraculousEntityTypes.THROWN_CAT_STAFF.get(), ThrownCatStaffRenderer::new);
    }

    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(MineraculousParticleTypes.CATACLYSM.get(), CataclysmParticle.Provider::new);
    }

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Mineraculous.modLoc("stealing_progress_bar"), MineraculousClientEvents::renderStealingProgressBar);
        kamikoGui = new KamikoGui(Minecraft.getInstance());
        event.registerAboveAll(Mineraculous.modLoc("kamiko_hotbar"), ((guiGraphics, deltaTracker) -> kamikoGui.renderHotbar(guiGraphics)));
        event.registerAboveAll(Mineraculous.modLoc("kamiko_tooltip"), ((guiGraphics, deltaTracker) -> kamikoGui.renderTooltip(guiGraphics)));
    }

    private static void renderStealingProgressBar(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        LocalPlayer player = Minecraft.getInstance().player;
        CompoundTag data = TommyLibServices.ENTITY.getPersistentData(player);
        int width = data.getInt(MineraculousEntityEvents.TAG_TAKETICKS);
        if (player != null && width > 0) {
            int x = (guiGraphics.guiWidth() - 18) / 2;
            int y = (guiGraphics.guiHeight() + 12) / 2;
            guiGraphics.fill(RenderType.guiOverlay(), x, y, x + 20, y + 5, -16777216);
            guiGraphics.fill(RenderType.guiOverlay(), x, y, (int) (x + (width / 5.0)), y + 5, 0xFFFFFFF | -16777216);
        }
    }

    public static void onGetPlayerHeartType(PlayerHeartTypeEvent event) {
        if (MineraculousEntityEvents.isCataclysmed(event.getEntity()))
            event.setType(MineraculousHeartTypes.CATACLYSMED.getValue());
    }

    public static void onRegisterEntitySpectatorShaders(RegisterEntitySpectatorShadersEvent event) {
        event.register(MineraculousEntityTypes.KAMIKO.get(), Kamiko.SPECTATOR_SHADER);
    }

    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(KamikoMaskModel.LAYER_LOCATION, KamikoMaskModel::createBodyLayer);
    }

    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        EntityModelSet models = event.getEntityModels();

        for (PlayerSkin.Model skin : event.getSkins()) {
            PlayerRenderer player = event.getSkin(skin);

            if (player != null) {
                player.addLayer(new KamikoMaskLayer<>(player, models));
            }
        }
    }

    public static void onRenderHand(RenderHandEvent event) {
        if (MineraculousClientUtils.getCameraEntity() != ClientUtils.getMainClientPlayer()) {
            event.setCanceled(true);
        }
    }

    public static void onKeyInput(InputEvent.Key event) {
        if (canControlKamiko()) {
            for (int i = 0; i < 9; i++) {
                if (Minecraft.getInstance().options.keyHotbarSlots[i].consumeClick()) {
                    kamikoGui.onHotbarSelected(i);
                }
            }
        }
    }

    public static void onMouseScrollingInput(InputEvent.MouseScrollingEvent event) {
        if (canControlKamiko()) {
            int i = (int) (event.getScrollDeltaY() == 0 ? -event.getScrollDeltaX() : event.getScrollDeltaY());
            kamikoGui.onMouseScrolled(i);
        }
    }

    public static void onMouseButtonClick(InputEvent.MouseButton.Post event) {
        if (canControlKamiko() && event.getButton() == 2) {
            kamikoGui.onMouseMiddleClick();
        }
    }

    private static boolean canControlKamiko() {
        return MineraculousClientUtils.getCameraEntity() instanceof Kamiko kamiko && kamiko.isOwnedBy(ClientUtils.getMainClientPlayer()) && !ClientUtils.getMainClientPlayer().isSpectator() && kamikoGui != null;
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        if (MineraculousClientUtils.getCameraEntity() != null && MineraculousClientUtils.getCameraEntity().isRemoved())
            MineraculousClientUtils.setCameraEntity(null);
    }

    public static void onClientChatReceived(ClientChatReceivedEvent event) {
        if (Minecraft.getInstance().level != null && Minecraft.getInstance().player != null) {
            boolean playerInChatWindow = TommyLibServices.ENTITY.getPersistentData(Minecraft.getInstance().player).getBoolean(MineraculousEntityEvents.TAG_SHOW_KAMIKO_MASK) && Minecraft.getInstance().screen instanceof KamikotizationChatScreen;
            if (event.isSystem()) {
                if (playerInChatWindow) {
                    event.setCanceled(true);
                }
            } else {
                boolean senderHasKamikoMask = TommyLibServices.ENTITY.getPersistentData(Minecraft.getInstance().level.getPlayerByUUID(event.getSender())).getBoolean(MineraculousEntityEvents.TAG_SHOW_KAMIKO_MASK);
                if ((playerInChatWindow && !senderHasKamikoMask) || (senderHasKamikoMask && !playerInChatWindow)) {
                    event.setCanceled(true);
                }
            }
        }
    }
}
