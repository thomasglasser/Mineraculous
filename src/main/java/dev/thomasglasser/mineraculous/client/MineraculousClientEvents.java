package dev.thomasglasser.mineraculous.client;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.gui.MineraculousHeartTypes;
import dev.thomasglasser.mineraculous.client.gui.components.kamiko.KamikoGui;
import dev.thomasglasser.mineraculous.client.model.DerbyHatModel;
import dev.thomasglasser.mineraculous.client.model.FaceMaskModel;
import dev.thomasglasser.mineraculous.client.particle.HoveringOrbParticle;
import dev.thomasglasser.mineraculous.client.particle.KamikotizationParticle;
import dev.thomasglasser.mineraculous.client.particle.RiseAndSpreadParticle;
import dev.thomasglasser.mineraculous.client.renderer.MineraculousRenderTypes;
import dev.thomasglasser.mineraculous.client.renderer.armor.KamikotizationArmorItemRenderer;
import dev.thomasglasser.mineraculous.client.renderer.armor.MiraculousArmorItemRenderer;
import dev.thomasglasser.mineraculous.client.renderer.entity.KwamiRenderer;
import dev.thomasglasser.mineraculous.client.renderer.entity.LuckyCharmItemSpawnerRenderer;
import dev.thomasglasser.mineraculous.client.renderer.entity.ThrownButterflyCaneRenderer;
import dev.thomasglasser.mineraculous.client.renderer.entity.ThrownCatStaffRenderer;
import dev.thomasglasser.mineraculous.client.renderer.entity.ThrownLadybugYoyoRenderer;
import dev.thomasglasser.mineraculous.client.renderer.entity.layers.BetaTesterLayer;
import dev.thomasglasser.mineraculous.client.renderer.entity.layers.FaceMaskLayer;
import dev.thomasglasser.mineraculous.client.renderer.entity.layers.KwamiOnShoulderLayer;
import dev.thomasglasser.mineraculous.client.renderer.item.MineraculousItemProperties;
import dev.thomasglasser.mineraculous.client.renderer.item.MiraculousItemRenderer;
import dev.thomasglasser.mineraculous.client.renderer.item.curio.ContextDependentCurioRenderer;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.network.ServerboundJumpMidSwingingPayload;
import dev.thomasglasser.mineraculous.network.ServerboundRevertConvertedEntityPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSetSpectationInterruptedPayload;
import dev.thomasglasser.mineraculous.network.ServerboundStartKamikotizationDetransformationPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSwingOffhandPayload;
import dev.thomasglasser.mineraculous.network.ServerboundUpdateYoyoInputPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.effect.MineraculousMobEffects;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmorUtils;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.world.level.block.AgeingCheese;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityEffectData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.mineraculous.world.level.storage.ThrownLadybugYoyoData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlyStraightTowardsParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
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
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerHeartTypeEvent;
import org.lwjgl.glfw.GLFW;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

public class MineraculousClientEvents {
    public static final String REVOKE = "gui.mineraculous.revoke";
    public static final String REVOKE_WITH_SPACE = "gui.mineraculous.revoke_with_space";

    private static KamikoGui kamikoGui;
    private static Button revokeButton;

    // Setup
    public static void onFMLClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            CuriosRendererRegistry.register(MineraculousItems.MIRACULOUS.get(), ContextDependentCurioRenderer::new);
            CuriosRendererRegistry.register(MineraculousItems.CAT_STAFF.get(), ContextDependentCurioRenderer::new);
            CuriosRendererRegistry.register(MineraculousItems.LADYBUG_YOYO.get(), ContextDependentCurioRenderer::new);

            MineraculousItemProperties.init();
        });
    }

    public static void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
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
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        Map<ResourceLocation, Resource> miraculous = manager.listResources("models/item/miraculous", (location -> location.getPath().endsWith(".json")));
        for (ResourceLocation rl : miraculous.keySet()) {
            ResourceLocation stripped = ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), rl.getPath().substring("models/".length(), rl.getPath().indexOf(".json")));
            event.register(ModelResourceLocation.standalone(stripped));
        }
    }

    public static void onRegisterRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MineraculousEntityTypes.KWAMI.get(), KwamiRenderer::new);
        event.registerEntityRenderer(MineraculousEntityTypes.KAMIKO.get(), context -> new GeoEntityRenderer<>(context, new DefaultedEntityGeoModel<>(Mineraculous.modLoc("kamiko"))));
        event.registerEntityRenderer(MineraculousEntityTypes.LUCKY_CHARM_ITEM_SPAWNER.get(), LuckyCharmItemSpawnerRenderer::new);
        event.registerEntityRenderer(MineraculousEntityTypes.THROWN_CAT_STAFF.get(), ThrownCatStaffRenderer::new);
        event.registerEntityRenderer(MineraculousEntityTypes.THROWN_BUTTERFLY_CANE.get(), ThrownButterflyCaneRenderer::new);
        event.registerEntityRenderer(MineraculousEntityTypes.THROWN_LADYBUG_YOYO.get(), ThrownLadybugYoyoRenderer::new);
    }

    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(MineraculousParticleTypes.BLACK_ORB.get(), HoveringOrbParticle.Provider::new);
        event.registerSpriteSet(MineraculousParticleTypes.KAMIKOTIZATION.get(), KamikotizationParticle.Provider::new);
        event.registerSpriteSet(MineraculousParticleTypes.SUMMONING_LADYBUG.get(), sprites -> (type, level, x, y, z, xSpeed, ySpeed, zSpeed) -> {
            FlyStraightTowardsParticle flystraighttowardsparticle = new FlyStraightTowardsParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, -1, -1);
            flystraighttowardsparticle.pickSprite(sprites);
            return flystraighttowardsparticle;
        });
        event.registerSpriteSet(MineraculousParticleTypes.SPREADING_LADYBUG.get(), RiseAndSpreadParticle.Provider::new);
    }

    public static void onRegisterEntitySpectatorShaders(RegisterEntitySpectatorShadersEvent event) {
        event.register(MineraculousEntityTypes.KAMIKO.get(), Kamiko.SPECTATOR_SHADER);
    }

    public static void onRegisterEntityRendererLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FaceMaskModel.LAYER_LOCATION, FaceMaskModel::createBodyLayer);
        event.registerLayerDefinition(DerbyHatModel.LAYER_LOCATION, DerbyHatModel::createBodyLayer);
    }

    public static void onAddEntityRendererLayers(EntityRenderersEvent.AddLayers event) {
        EntityModelSet models = event.getEntityModels();

        for (PlayerSkin.Model skin : event.getSkins()) {
            PlayerRenderer player = event.getSkin(skin);

            if (player != null) {
                player.addLayer(new FaceMaskLayer<>(player, models));
                player.addLayer(new KwamiOnShoulderLayer<>(player));
                player.addLayer(new BetaTesterLayer<>(player, models));
            }
        }
    }

    public static void onRegisterItemColorHandlers(RegisterColorHandlersEvent.Item event) {
        event.register((stack, index) -> {
            Holder<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
            if (miraculous != null) {
                return FastColor.ARGB32.opaque(miraculous.value().color().getValue());
            }
            return -1;
        }, MineraculousArmors.MIRACULOUS.getAllAsItems().toArray(new Item[0]));
    }

    public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        // Clears old rendering data on reload
        event.registerReloadListener((ResourceManagerReloadListener) resourceManager -> {
            MineraculousClientUtils.syncSpecialPlayerChoices();
            MineraculousArmorUtils.clearAnimationData();
            MiraculousItemRenderer.clearModels();
            MiraculousArmorItemRenderer.clearModels();
            KamikotizationArmorItemRenderer.clearModels();
        });
    }

    public static void onRegisterRenderBuffers(RegisterRenderBuffersEvent event) {
        event.registerRenderBuffer(MineraculousRenderTypes.itemLuckyCharm());
        event.registerRenderBuffer(MineraculousRenderTypes.armorLuckyCharm());
        event.registerRenderBuffer(MineraculousRenderTypes.entityLuckyCharm());
        event.registerRenderBuffer(MineraculousRenderTypes.shieldLuckyCharm());
    }

    // GUI
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Mineraculous.modLoc("stealing_progress_bar"), MineraculousClientEvents::renderStealingProgressBar);
        event.registerAboveAll(Mineraculous.modLoc("revoke_button"), MineraculousClientEvents::renderRevokeButton);
        kamikoGui = new KamikoGui(Minecraft.getInstance());
        event.registerAboveAll(Mineraculous.modLoc("kamiko_hotbar"), ((guiGraphics, deltaTracker) -> kamikoGui.renderHotbar(guiGraphics)));
        event.registerAboveAll(Mineraculous.modLoc("kamiko_tooltip"), ((guiGraphics, deltaTracker) -> kamikoGui.renderTooltip(guiGraphics)));
    }

    private static void renderStealingProgressBar(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        LocalPlayer player = Minecraft.getInstance().player;
        int width = MineraculousKeyMappings.getTakeTicks();
        if (player != null && width > 0) {
            int x = (guiGraphics.guiWidth() - 18) / 2;
            int y = (guiGraphics.guiHeight() + 12) / 2;
            guiGraphics.fill(RenderType.guiOverlay(), x, y, x + 20, y + 5, -16777216);
            guiGraphics.fill(RenderType.guiOverlay(), x, y, (int) (x + (width / 5.0)), y + 5, 0xFFFFFFF | -16777216);
        }
    }

    private static void renderRevokeButton(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (revokeButton == null) {
            revokeButton = new Button(Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - 100, Minecraft.getInstance().getWindow().getGuiScaledHeight() - 35, 200, 20, Component.translatable(REVOKE), button -> {
                Entity cameraEntity = MineraculousClientUtils.getCameraEntity();
                if (cameraEntity instanceof Player target) {
                    KamikotizationData kamikotizationData = target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).orElseThrow();
                    TommyLibServices.NETWORK.sendToServer(new ServerboundStartKamikotizationDetransformationPayload(Optional.of(target.getUUID()), kamikotizationData, false));
                    AbilityEffectData.removeFaceMaskTexture(target, kamikotizationData.kamikoData().faceMaskTexture());
                } else if (cameraEntity instanceof Kamiko kamiko) {
                    TommyLibServices.NETWORK.sendToServer(new ServerboundRevertConvertedEntityPayload(kamiko.getOwner().getId(), kamiko.getId()));
                }
                TommyLibServices.NETWORK.sendToServer(new ServerboundSetSpectationInterruptedPayload(Optional.empty()));
            }, Button.DEFAULT_NARRATION) {
                @Override
                public Component getMessage() {
                    if (MineraculousClientUtils.hasNoScreenOpen())
                        return Component.translatable(REVOKE_WITH_SPACE);
                    else
                        return Component.translatable(REVOKE);
                }

                @Override
                public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                    if (active) {
                        if (keyCode == GLFW.GLFW_KEY_SPACE) {
                            revokeButton.onPress();
                            return true;
                        }
                    }
                    return super.keyPressed(keyCode, scanCode, modifiers);
                }
            };
        }

        if (isInKamikoView() && !kamikoGui.isMenuActive()) {
            if (MineraculousClientUtils.hasNoScreenOpen()) {
                revokeButton.setPosition(revokeButton.getX(), Minecraft.getInstance().getWindow().getGuiScaledHeight() - 60);
                revokeButton.active = true;
            } else if (Minecraft.getInstance().screen instanceof ChatScreen) {
                revokeButton.setPosition(revokeButton.getX(), Minecraft.getInstance().getWindow().getGuiScaledHeight() - 35);
                revokeButton.active = true;
            } else
                revokeButton.active = false;
        } else
            revokeButton.active = false;

        if (revokeButton.active) {
            int mouseX = (int) (Minecraft.getInstance().mouseHandler.xpos()
                    * (double) Minecraft.getInstance().getWindow().getGuiScaledWidth()
                    / (double) Minecraft.getInstance().getWindow().getScreenWidth());
            int mouseY = (int) (Minecraft.getInstance().mouseHandler.ypos()
                    * (double) Minecraft.getInstance().getWindow().getGuiScaledHeight()
                    / (double) Minecraft.getInstance().getWindow().getScreenHeight());
            revokeButton.render(guiGraphics, mouseX, mouseY, 0);
        }
    }

    private static boolean isInKamikoView() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && !player.isSpectator() && player.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed() && MineraculousClientUtils.getCameraEntity() instanceof Kamiko kamiko && kamiko.isOwnedBy(player) && kamikoGui != null;
    }

    // Tick
    public static void onClientTick(ClientTickEvent.Post event) {
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
        if (thrownYoyo != null) {
            if (player.input.jumping) {
                TommyLibServices.NETWORK.sendToServer(ServerboundJumpMidSwingingPayload.INSTANCE);
            } else {
                boolean front = player.input.up;
                boolean back = player.input.down;
                boolean left = player.input.left;
                boolean right = player.input.right;
                if (front || back || left || right) {
                    TommyLibServices.NETWORK.sendToServer(new ServerboundUpdateYoyoInputPayload(front, back, left, right));
                }
            }
        }
    }

    // Input
    public static void onKeyInput(InputEvent.Key event) {
        if (isInKamikoView()) {
            for (int i = 0; i < 9; i++) {
                if (Minecraft.getInstance().options.keyHotbarSlots[i].consumeClick()) {
                    kamikoGui.onHotbarSelected(i);
                }
            }
            if (event.getKey() == GLFW.GLFW_KEY_SPACE && MineraculousClientUtils.hasNoScreenOpen() && revokeButton != null && revokeButton.active) {
                revokeButton.onPress();
            }
        }
    }

    public static void onMouseScrollingInput(InputEvent.MouseScrollingEvent event) {
        if (isInKamikoView()) {
            int i = (int) (event.getScrollDeltaY() == 0 ? -event.getScrollDeltaX() : event.getScrollDeltaY());
            kamikoGui.onMouseScrolled(i);
        }
    }

    public static void onPostMouseButtonInput(InputEvent.MouseButton.Post event) {
        if (isInKamikoView() && event.getButton() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            kamikoGui.onMouseMiddleClick();
        }
        if (revokeButton != null && revokeButton.active) {
            int mouseX = (int) (Minecraft.getInstance().mouseHandler.xpos()
                    * (double) Minecraft.getInstance().getWindow().getGuiScaledWidth()
                    / (double) Minecraft.getInstance().getWindow().getScreenWidth());
            int mouseY = (int) (Minecraft.getInstance().mouseHandler.ypos()
                    * (double) Minecraft.getInstance().getWindow().getGuiScaledHeight()
                    / (double) Minecraft.getInstance().getWindow().getScreenHeight());
            revokeButton.mouseClicked(mouseX, mouseY, event.getButton());
        }
    }

    public static void onInteractionKeyMappingTriggered(InputEvent.InteractionKeyMappingTriggered event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (event.isAttack() && event.getHand() == InteractionHand.MAIN_HAND && player != null && player.getOffhandItem().is(MineraculousItems.LADYBUG_YOYO)) {
            TommyLibServices.NETWORK.sendToServer(ServerboundSwingOffhandPayload.INSTANCE);
            if (player.getOffhandItem().onEntitySwing(player, InteractionHand.OFF_HAND)) {
                event.setCanceled(true);
            }
        }
    }

    // Rendering
    public static void onRenderHand(RenderHandEvent event) {
        if (MineraculousClientUtils.getCameraEntity() != Minecraft.getInstance().player) {
            event.setCanceled(true);
        }
    }

    // Special Player Handling
    public static void onClientPlayerLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        MineraculousClientUtils.syncSpecialPlayerChoices();
    }

    public static void onConfigChanged(ModConfigEvent event) {
        MineraculousClientUtils.syncSpecialPlayerChoices();
    }

    // Misc
    public static void onPlayerHeartType(PlayerHeartTypeEvent event) {
        if (event.getEntity().hasEffect(MineraculousMobEffects.CATACLYSM))
            event.setType(MineraculousHeartTypes.CATACLYSMED.getValue());
    }

    public static void onClientChatReceived(ClientChatReceivedEvent event) {
        ClientLevel level = Minecraft.getInstance().level;
        LocalPlayer player = Minecraft.getInstance().player;
        if (level != null && player != null) {
            player.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).privateChat().ifPresent(chatter -> {
                if (!(event.getSender().equals(chatter) || event.getSender().equals(player.getUUID()) || (event instanceof ClientChatReceivedEvent.System system && system.isOverlay()))) {
                    event.setCanceled(true);
                }
            });
        }
    }
}
