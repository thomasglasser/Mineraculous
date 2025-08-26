package dev.thomasglasser.mineraculous.impl.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thomasglasser.mineraculous.api.client.particle.HoveringOrbParticle;
import dev.thomasglasser.mineraculous.api.client.particle.KamikotizationParticle;
import dev.thomasglasser.mineraculous.api.client.renderer.MineraculousRenderTypes;
import dev.thomasglasser.mineraculous.api.client.renderer.item.MineraculousItemProperties;
import dev.thomasglasser.mineraculous.api.client.renderer.item.curio.ContextDependentCurioRenderer;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.effect.MineraculousMobEffects;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.api.world.level.block.AgeingCheese;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.client.gui.MineraculousGuis;
import dev.thomasglasser.mineraculous.impl.client.gui.MineraculousHeartTypes;
import dev.thomasglasser.mineraculous.impl.client.model.BeardModel;
import dev.thomasglasser.mineraculous.impl.client.model.DerbyHatModel;
import dev.thomasglasser.mineraculous.impl.client.model.FaceMaskModel;
import dev.thomasglasser.mineraculous.impl.client.renderer.armor.KamikotizationArmorItemRenderer;
import dev.thomasglasser.mineraculous.impl.client.renderer.armor.MiraculousArmorItemRenderer;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.KwamiRenderer;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.LuckyCharmItemSpawnerRenderer;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.ThrownButterflyCaneRenderer;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.ThrownCatStaffRenderer;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.ThrownLadybugYoyoRenderer;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers.BetaTesterLayer;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers.FaceMaskLayer;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers.LegacyDevTeamLayer;
import dev.thomasglasser.mineraculous.impl.client.renderer.item.CatStaffRenderer;
import dev.thomasglasser.mineraculous.impl.client.renderer.item.LadybugYoyoRenderer;
import dev.thomasglasser.mineraculous.impl.client.renderer.item.MiraculousItemRenderer;
import dev.thomasglasser.mineraculous.impl.network.ServerboundSwingOffhandPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundUpdateYoyoInputPayload;
import dev.thomasglasser.mineraculous.impl.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.mineraculous.impl.world.item.armor.MineraculousArmorUtils;
import dev.thomasglasser.mineraculous.impl.world.level.storage.ThrownLadybugYoyoData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Map;
import java.util.SortedMap;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlyStraightTowardsParticle;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterEntitySpectatorShadersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerHeartTypeEvent;
import org.lwjgl.glfw.GLFW;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

public class MineraculousClientEvents {
    // Setup
    static void onFMLClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            CuriosRendererRegistry.register(MineraculousItems.MIRACULOUS.get(), ContextDependentCurioRenderer::new);
            CuriosRendererRegistry.register(MineraculousItems.CAT_STAFF.get(), ContextDependentCurioRenderer::new);
            CuriosRendererRegistry.register(MineraculousItems.LADYBUG_YOYO.get(), ContextDependentCurioRenderer::new);

            MineraculousItemProperties.init();
        });
    }

    static void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {

        } else if (event.getTabKey() == CreativeModeTabs.COLORED_BLOCKS) {

        } else if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.insertAfter(Items.PINK_PETALS.getDefaultInstance(), MineraculousBlocks.HIBISCUS_BUSH.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(Items.COBWEB.getDefaultInstance(), MineraculousBlocks.CATACLYSM_BLOCK.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        } else if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.insertAfter(Items.LOOM.getDefaultInstance(), MineraculousBlocks.CHEESE_POT.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            addCheeses(event, MineraculousBlocks.CHEESE_POT.toStack(), MineraculousItems.WAXED_CHEESE);
            addCheeses(event, MineraculousItems.WAXED_CHEESE.get(AgeingCheese.Age.TIME_HONORED).toStack(), MineraculousBlocks.WAXED_CHEESE);
            addCheeses(event, MineraculousBlocks.WAXED_CHEESE.get(AgeingCheese.Age.TIME_HONORED).toStack(), MineraculousItems.WAXED_CAMEMBERT);
            addCheeses(event, MineraculousItems.WAXED_CAMEMBERT.get(AgeingCheese.Age.TIME_HONORED).toStack(), MineraculousBlocks.WAXED_CAMEMBERT);
        } else if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {

        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {

        } else if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.insertAfter(Items.SHIELD.getDefaultInstance(), MineraculousItems.LADYBUG_YOYO.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.LADYBUG_YOYO.toStack(), MineraculousItems.CAT_STAFF.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.CAT_STAFF.toStack(), MineraculousItems.BUTTERFLY_CANE.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.BUTTERFLY_CANE.toStack(), MineraculousItems.GREAT_SWORD.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        } else if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            addCheeses(event, Items.PUMPKIN_PIE.getDefaultInstance(), MineraculousItems.CHEESE);
            addCheeses(event, MineraculousItems.CHEESE.get(AgeingCheese.Age.TIME_HONORED).toStack(), MineraculousBlocks.CHEESE);
            addCheeses(event, MineraculousBlocks.CHEESE.get(AgeingCheese.Age.TIME_HONORED).toStack(), MineraculousItems.CAMEMBERT);
            addCheeses(event, MineraculousItems.CAMEMBERT.get(AgeingCheese.Age.TIME_HONORED).toStack(), MineraculousBlocks.CAMEMBERT);
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.insertAfter(Items.BLAZE_POWDER.getDefaultInstance(), MineraculousItems.CATACLYSM_DUST.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);

            // Armor Trims
            event.insertAfter(Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE.getDefaultInstance(), MineraculousItems.LADYBUG_ARMOR_TRIM_SMITHING_TEMPLATE.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.LADYBUG_ARMOR_TRIM_SMITHING_TEMPLATE.toStack(), MineraculousItems.CAT_ARMOR_TRIM_SMITHING_TEMPLATE.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.CAT_ARMOR_TRIM_SMITHING_TEMPLATE.toStack(), MineraculousItems.BUTTERFLY_ARMOR_TRIM_SMITHING_TEMPLATE.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        } else if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            // Must be in alphabetical order
            event.insertAfter(Items.IRON_GOLEM_SPAWN_EGG.getDefaultInstance(), MineraculousItems.KAMIKO_SPAWN_EGG.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        } else if (event.getTabKey() == CreativeModeTabs.OP_BLOCKS) {

        }
    }

    private static <T extends ItemLike> void addCheeses(BuildCreativeModeTabContentsEvent event, ItemStack before, SortedMap<AgeingCheese.Age, T> cheeses) {
        event.insertAfter(before, cheeses.get(AgeingCheese.Age.FRESH).asItem().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        for (int i = 1; i < AgeingCheese.Age.values().length; i++) {
            AgeingCheese.Age age = AgeingCheese.Age.values()[i];
            AgeingCheese.Age previous = AgeingCheese.Age.values()[i - 1];
            event.insertAfter(cheeses.get(previous).asItem().getDefaultInstance(), cheeses.get(age).asItem().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    // Registration
    static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        Map<ResourceLocation, Resource> miraculous = manager.listResources("models/item/miraculous", (location -> location.getPath().endsWith(".json")));
        for (ResourceLocation rl : miraculous.keySet()) {
            ResourceLocation stripped = ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), rl.getPath().substring("models/".length(), rl.getPath().indexOf(".json")));
            event.register(ModelResourceLocation.standalone(stripped));
        }
    }

    static void onRegisterRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MineraculousEntityTypes.KWAMI.get(), KwamiRenderer::new);
        event.registerEntityRenderer(MineraculousEntityTypes.KAMIKO.get(), context -> new GeoEntityRenderer<>(context, new DefaultedEntityGeoModel<>(Mineraculous.modLoc("kamiko"))));
        event.registerEntityRenderer(MineraculousEntityTypes.LUCKY_CHARM_ITEM_SPAWNER.get(), LuckyCharmItemSpawnerRenderer::new);
        event.registerEntityRenderer(MineraculousEntityTypes.THROWN_CAT_STAFF.get(), ThrownCatStaffRenderer::new);
        event.registerEntityRenderer(MineraculousEntityTypes.THROWN_BUTTERFLY_CANE.get(), ThrownButterflyCaneRenderer::new);
        event.registerEntityRenderer(MineraculousEntityTypes.THROWN_LADYBUG_YOYO.get(), ThrownLadybugYoyoRenderer::new);
    }

    static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(MineraculousParticleTypes.BLACK_ORB.get(), HoveringOrbParticle.Provider::new);
        event.registerSpriteSet(MineraculousParticleTypes.KAMIKOTIZATION.get(), KamikotizationParticle.Provider::new);
        event.registerSpriteSet(MineraculousParticleTypes.SUMMONING_LADYBUG.get(), sprites -> (type, level, x, y, z, xSpeed, ySpeed, zSpeed) -> {
            FlyStraightTowardsParticle flystraighttowardsparticle = new FlyStraightTowardsParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, -1, -1);
            flystraighttowardsparticle.pickSprite(sprites);
            return flystraighttowardsparticle;
        });
    }

    static void onRegisterEntitySpectatorShaders(RegisterEntitySpectatorShadersEvent event) {
        event.register(MineraculousEntityTypes.KAMIKO.get(), Kamiko.SPECTATOR_SHADER);
    }

    static void onRegisterEntityRendererLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FaceMaskModel.LAYER_LOCATION, FaceMaskModel::createBodyLayer);
        event.registerLayerDefinition(DerbyHatModel.LAYER_LOCATION, DerbyHatModel::createBodyLayer);
        event.registerLayerDefinition(BeardModel.LAYER_LOCATION, BeardModel::createBodyLayer);
    }

    static void onAddEntityRendererLayers(EntityRenderersEvent.AddLayers event) {
        EntityModelSet models = event.getEntityModels();

        for (PlayerSkin.Model skin : event.getSkins()) {
            PlayerRenderer player = event.getSkin(skin);

            if (player != null) {
                player.addLayer(new FaceMaskLayer<>(player, models));
                player.addLayer(new BetaTesterLayer<>(player, models));
                player.addLayer(new LegacyDevTeamLayer<>(player, models));
            }
        }
    }

    static void onRegisterItemColorHandlers(RegisterColorHandlersEvent.Item event) {
        event.register((stack, index) -> {
            Holder<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
            if (miraculous != null) {
                return FastColor.ARGB32.opaque(miraculous.value().color().getValue());
            }
            return -1;
        }, MineraculousArmors.MIRACULOUS.getAllAsItems().toArray(new Item[0]));
    }

    static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        // Clears old rendering data on reload
        event.registerReloadListener((ResourceManagerReloadListener) resourceManager -> {
            MineraculousClientUtils.syncSpecialPlayerChoices();
            MineraculousArmorUtils.clearAnimationData();
            MiraculousItemRenderer.clearModels();
            MiraculousArmorItemRenderer.clearModels();
            KamikotizationArmorItemRenderer.clearModels();
            MineraculousClientUtils.refreshCataclysmPixels();
        });
    }

    static void onRegisterRenderBuffers(RegisterRenderBuffersEvent event) {
        event.registerRenderBuffer(MineraculousRenderTypes.itemLuckyCharm());
        event.registerRenderBuffer(MineraculousRenderTypes.armorLuckyCharm());
        event.registerRenderBuffer(MineraculousRenderTypes.entityLuckyCharm());
        event.registerRenderBuffer(MineraculousRenderTypes.shieldLuckyCharm());
    }

    static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        // Miraculous Tools
        event.registerItem(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new LadybugYoyoRenderer();
                return renderer;
            }

            @Override
            public ResourceLocation getScopeOverlayTexture(ItemStack stack) {
                return LadybugYoyoRenderer.SPYGLASS_SCOPE_LOCATION;
            }
        }, MineraculousItems.LADYBUG_YOYO);
    }

    // GUI
    static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Mineraculous.modLoc("stealing_progress_bar"), MineraculousGuis::renderStealingProgressBar);
        event.registerAboveAll(Mineraculous.modLoc("revoke_button"), MineraculousGuis::renderRevokeButton);
        event.registerAboveAll(Mineraculous.modLoc("kamiko_hotbar"), MineraculousGuis.getKamikoGui()::renderHotbar);
        event.registerAboveAll(Mineraculous.modLoc("kamiko_tooltip"), MineraculousGuis.getKamikoGui()::renderTooltip);
    }

    // Tick
    static void onClientTick(ClientTickEvent.Post event) {
        if (MineraculousClientUtils.getCameraEntity() != null && MineraculousClientUtils.getCameraEntity().isRemoved())
            MineraculousClientUtils.setCameraEntity(null);

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            checkYoyoInput(player);
        }
    }

    private static void checkYoyoInput(LocalPlayer player) {
        ThrownLadybugYoyoData data = player.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO);
        ThrownLadybugYoyo thrownYoyo = data.getThrownYoyo(player.level());

        MineraculousClientUtils.InputState input = MineraculousClientUtils.captureInput();
        if (thrownYoyo != null && input.hasInput()) {
            int packedInput = input.packInputs();
            TommyLibServices.NETWORK.sendToServer(new ServerboundUpdateYoyoInputPayload(packedInput));
        }
    }

    // Input
    static void onKeyInput(InputEvent.Key event) {
        if (MineraculousClientUtils.isInKamikoView()) {
            for (int i = 0; i < 9; i++) {
                if (Minecraft.getInstance().options.keyHotbarSlots[i].consumeClick()) {
                    MineraculousGuis.getKamikoGui().onHotbarSelected(i);
                }
            }
        }

        if (MineraculousGuis.checkRevokeButtonActive()) {
            Button revokeButton = MineraculousGuis.getRevokeButton();
            int key = event.getKey();
            if ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) && MineraculousClientUtils.hasNoScreenOpen() && revokeButton.active) {
                revokeButton.onPress();
            }
        }
    }

    static void onMouseScrollingInput(InputEvent.MouseScrollingEvent event) {
        if (MineraculousClientUtils.isInKamikoView()) {
            int i = (int) (event.getScrollDeltaY() == 0 ? -event.getScrollDeltaX() : event.getScrollDeltaY());
            MineraculousGuis.getKamikoGui().onMouseScrolled(i);
        }
    }

    static void onPostMouseButtonInput(InputEvent.MouseButton.Post event) {
        if (MineraculousClientUtils.isInKamikoView() && event.getButton() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            MineraculousGuis.getKamikoGui().onMouseMiddleClick();
        }
        Button revokeButton = MineraculousGuis.getRevokeButton();
        if (revokeButton.active) {
            int mouseX = (int) (Minecraft.getInstance().mouseHandler.xpos()
                    * (double) Minecraft.getInstance().getWindow().getGuiScaledWidth()
                    / (double) Minecraft.getInstance().getWindow().getScreenWidth());
            int mouseY = (int) (Minecraft.getInstance().mouseHandler.ypos()
                    * (double) Minecraft.getInstance().getWindow().getGuiScaledHeight()
                    / (double) Minecraft.getInstance().getWindow().getScreenHeight());
            revokeButton.mouseClicked(mouseX, mouseY, event.getButton());
        }
    }

    static void onInteractionKeyMappingTriggered(InputEvent.InteractionKeyMappingTriggered event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (event.isAttack() && event.getHand() == InteractionHand.MAIN_HAND && player != null && player.getOffhandItem().is(MineraculousItems.LADYBUG_YOYO)) {
            TommyLibServices.NETWORK.sendToServer(ServerboundSwingOffhandPayload.INSTANCE);
            if (player.getOffhandItem().onEntitySwing(player, InteractionHand.OFF_HAND)) {
                event.setCanceled(true);
            }
        }
    }

    // Rendering
    static void onRenderHand(RenderHandEvent event) {
        if (MineraculousClientUtils.getCameraEntity() != Minecraft.getInstance().player) {
            event.setCanceled(true);
        }
    }

    public static void onPlayerRendererPost(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        int light = event.getPackedLight();
        float partialTick = event.getPartialTick();

        player.noCulling = true;
        CatStaffRenderer.renderPerch(player, poseStack, bufferSource, light, partialTick);
        CatStaffRenderer.renderTravel(player, poseStack, bufferSource, light, partialTick);
    }

    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        AbstractClientPlayer player = Minecraft.getInstance().player;
        RenderLevelStageEvent.Stage stage = event.getStage();
        EntityRenderDispatcher renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        float partialTick = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPartialTickTime();
        int light = renderDispatcher.getPackedLightCoords(player, partialTick);

        if (stage == RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS && renderDispatcher.options.getCameraType().isFirstPerson()) {
            poseStack.translate(0, -1.6d, 0);
            CatStaffRenderer.renderPerch(player, poseStack, bufferSource, light, partialTick);
            CatStaffRenderer.renderTravel(player, poseStack, bufferSource, light, partialTick);
        }
    }

    // Special Player Handling
    static void onClientPlayerLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        MineraculousClientUtils.syncSpecialPlayerChoices();
    }

    static void onConfigChanged(ModConfigEvent event) {
        MineraculousClientUtils.syncSpecialPlayerChoices();
    }

    // Misc
    static void onPlayerHeartType(PlayerHeartTypeEvent event) {
        if (event.getEntity().hasEffect(MineraculousMobEffects.CATACLYSM))
            event.setType(MineraculousHeartTypes.CATACLYSMED.getValue());
    }

    static void onClientChatReceived(ClientChatReceivedEvent.Player event) {
        ClientLevel level = Minecraft.getInstance().level;
        Player receiver = Minecraft.getInstance().player;
        UUID senderId = event.getSender();
        if (level != null && receiver != null) {
            Player sender = level.getPlayerByUUID(event.getSender());
            if (receiver != sender &&
                    (receiver.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).privateChat().map(chatter -> !chatter.equals(senderId)).orElse(false) ||
                            (sender != null && sender.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).privateChat().map(chatter -> !chatter.equals(receiver.getUUID())).orElse(false)))) {
                event.setCanceled(true);
            }
        }
    }
}
