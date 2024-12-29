package dev.thomasglasser.mineraculous.client;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.gui.MineraculousHeartTypes;
import dev.thomasglasser.mineraculous.client.gui.components.kamiko.KamikoGui;
import dev.thomasglasser.mineraculous.client.gui.screens.RadialMenuOption;
import dev.thomasglasser.mineraculous.client.gui.screens.RadialMenuScreen;
import dev.thomasglasser.mineraculous.client.model.KamikoMaskModel;
import dev.thomasglasser.mineraculous.client.particle.HoveringOrbParticle;
import dev.thomasglasser.mineraculous.client.particle.KamikotizationParticle;
import dev.thomasglasser.mineraculous.client.renderer.entity.KamikoRenderer;
import dev.thomasglasser.mineraculous.client.renderer.entity.KwamiRenderer;
import dev.thomasglasser.mineraculous.client.renderer.entity.ThrownCatStaffRenderer;
import dev.thomasglasser.mineraculous.client.renderer.entity.layers.KamikoMaskLayer;
import dev.thomasglasser.mineraculous.client.renderer.item.MineraculousItemProperties;
import dev.thomasglasser.mineraculous.client.renderer.item.curio.ContextDependentCurioRenderer;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.network.ServerboundKamikotizationTransformPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSetToggleTagPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.world.item.curio.ContextDependentCurio;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterEntitySpectatorShadersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerHeartTypeEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

public class MineraculousClientEvents {
    public static final String REVOKE = "gui.mineraculous.revoke";

    private static KamikoGui kamikoGui;

    public static void onFMLClientSetup(FMLClientSetupEvent event) {
        CuriosRendererRegistry.register(MineraculousItems.MIRACULOUS.get(), ContextDependentCurioRenderer::new);
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof ContextDependentCurio)
                CuriosRendererRegistry.register(item, ContextDependentCurioRenderer::new);
        }

        MineraculousItemProperties.init();
    }

    public static void openToolWheel(ResourceKey<Miraculous> miraculousType, ItemStack stack, Consumer<RadialMenuOption> onSelected, RadialMenuOption... options) {
        if (ClientUtils.getMinecraft().screen == null) {
            ClientUtils.setScreen(new RadialMenuScreen(Arrays.asList(options), stack, onSelected, MineraculousKeyMappings.OPEN_TOOL_WHEEL.get().getKey().getValue(), ClientUtils.getLevel().holderOrThrow(miraculousType).value().color().getValue()));
        }
    }

    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        Map<ResourceLocation, Resource> map = manager.listResources("models/item/miraculous", (location -> location.getPath().endsWith(".json")));
        for (ResourceLocation rl : map.keySet()) {
            ResourceLocation stripped = ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), rl.getPath().substring("models/".length(), rl.getPath().indexOf(".json")));
            event.register(ModelResourceLocation.standalone(stripped));
        }

        event.register(ModelResourceLocation.standalone(Mineraculous.modLoc("item/kamikotization/armor")));

        event.register(ModelResourceLocation.standalone(Mineraculous.modLoc("item/cat_staff_extended")));
    }

    public static void onRegisterRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MineraculousEntityTypes.KWAMI.get(), KwamiRenderer::new);
        event.registerEntityRenderer(MineraculousEntityTypes.KAMIKO.get(), KamikoRenderer::new);
        event.registerEntityRenderer(MineraculousEntityTypes.THROWN_CAT_STAFF.get(), ThrownCatStaffRenderer::new);
    }

    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(MineraculousParticleTypes.BLACK_ORB.get(), HoveringOrbParticle.Provider::new);
        event.registerSpriteSet(MineraculousParticleTypes.KAMIKOTIZATION.get(), KamikotizationParticle.Provider::new);
    }

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Mineraculous.modLoc("stealing_progress_bar"), MineraculousClientEvents::renderStealingProgressBar);
        kamikoGui = new KamikoGui(Minecraft.getInstance());
        event.registerAboveAll(Mineraculous.modLoc("kamiko_hotbar"), ((guiGraphics, deltaTracker) -> kamikoGui.renderHotbar(guiGraphics)));
        event.registerAboveAll(Mineraculous.modLoc("kamiko_tooltip"), ((guiGraphics, deltaTracker) -> kamikoGui.renderTooltip(guiGraphics)));
        event.registerAboveAll(Mineraculous.modLoc("revoke_button"), MineraculousClientEvents::renderRevokeButton);
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

    private static Button revokeButton;

    private static void renderRevokeButton(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (revokeButton == null) {
            revokeButton = Button.builder(Component.translatable(REVOKE), button -> {
                if (MineraculousClientUtils.getCameraEntity() instanceof Player target) {
                    TommyLibServices.NETWORK.sendToServer(new ServerboundKamikotizationTransformPayload(Optional.of(target.getUUID()), target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION), false, false, false, target.position().add(0, 1, 0)));
                    TommyLibServices.NETWORK.sendToServer(new ServerboundSetToggleTagPayload(MineraculousEntityEvents.TAG_SHOW_KAMIKO_MASK, false));
                    MineraculousClientUtils.setCameraEntity(ClientUtils.getMainClientPlayer());
                }
            })
                    .bounds(Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - 100, Minecraft.getInstance().getWindow().getGuiScaledHeight() - 40, 200, 20)
                    .build();
        }

        if (MineraculousClientUtils.getCameraEntity() instanceof Player player && player != ClientUtils.getMainClientPlayer() && !ClientUtils.getMainClientPlayer().isSpectator()) {
            int mouseX = (int) (Minecraft.getInstance().mouseHandler.xpos()
                    * (double) Minecraft.getInstance().getWindow().getGuiScaledWidth()
                    / (double) Minecraft.getInstance().getWindow().getScreenWidth());
            int mouseY = (int) (Minecraft.getInstance().mouseHandler.ypos()
                    * (double) Minecraft.getInstance().getWindow().getGuiScaledHeight()
                    / (double) Minecraft.getInstance().getWindow().getScreenHeight());
            revokeButton.render(guiGraphics, mouseX, mouseY, 0);
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
        if (revokeButton != null && revokeButton.visible) {
            int mouseX = (int) (Minecraft.getInstance().mouseHandler.xpos()
                    * (double) Minecraft.getInstance().getWindow().getGuiScaledWidth()
                    / (double) Minecraft.getInstance().getWindow().getScreenWidth());
            int mouseY = (int) (Minecraft.getInstance().mouseHandler.ypos()
                    * (double) Minecraft.getInstance().getWindow().getGuiScaledHeight()
                    / (double) Minecraft.getInstance().getWindow().getScreenHeight());
            revokeButton.mouseClicked(mouseX, mouseY, event.getButton());
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
            boolean onlyButterflyChat = TommyLibServices.ENTITY.getPersistentData(Minecraft.getInstance().player).getBoolean(MineraculousEntityEvents.TAG_SHOW_KAMIKO_MASK);
            if (event.isSystem()) {
                if (onlyButterflyChat && !(event instanceof ClientChatReceivedEvent.System system && system.isOverlay())) {
                    event.setCanceled(true);
                }
            } else {
                boolean senderHasKamikoMask = TommyLibServices.ENTITY.getPersistentData(Minecraft.getInstance().level.getPlayerByUUID(event.getSender())).getBoolean(MineraculousEntityEvents.TAG_SHOW_KAMIKO_MASK);
                if ((onlyButterflyChat && !senderHasKamikoMask) || (senderHasKamikoMask && !onlyButterflyChat)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    public static void onRegisterItemColorHandlers(RegisterColorHandlersEvent.Item event) {
        event.register((stack, index) -> {
            ResourceKey<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
            if (miraculous != null) {
                return FastColor.ARGB32.opaque(Minecraft.getInstance().level.registryAccess().holderOrThrow(miraculous).value().color().getValue());
            }
            return -1;
        }, MineraculousArmors.MIRACULOUS.getAllAsItems().toArray(new Item[0]));
    }

    public static void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {

        } else if (event.getTabKey() == CreativeModeTabs.COLORED_BLOCKS) {

        } else if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.insertAfter(Items.PINK_PETALS.getDefaultInstance(), MineraculousBlocks.HIBISCUS_BUSH.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(Items.COBWEB.getDefaultInstance(), MineraculousBlocks.CATACLYSM_BLOCK.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        } else if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.insertAfter(Items.LOOM.getDefaultInstance(), MineraculousBlocks.CHEESE_POT.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        } else if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {

        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {

        } else if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.insertAfter(Items.SHIELD.getDefaultInstance(), MineraculousItems.LADYBUG_YOYO.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.LADYBUG_YOYO.toStack(), MineraculousItems.CAT_STAFF.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.CAT_STAFF.toStack(), MineraculousItems.BUTTERFLY_CANE.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        } else if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.insertAfter(Items.PUMPKIN_PIE.getDefaultInstance(), MineraculousItems.CHEESE_WEDGES.get(CheeseBlock.Age.FRESH).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.CHEESE_WEDGES.get(CheeseBlock.Age.FRESH).toStack(), MineraculousBlocks.CHEESE_BLOCKS.get(CheeseBlock.Age.FRESH).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.CHEESE_BLOCKS.get(CheeseBlock.Age.FRESH).toStack(), MineraculousItems.CAMEMBERT_WEDGES.get(CheeseBlock.Age.FRESH).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.CAMEMBERT_WEDGES.get(CheeseBlock.Age.FRESH).toStack(), MineraculousBlocks.CAMEMBERT_BLOCKS.get(CheeseBlock.Age.FRESH).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.CAMEMBERT_BLOCKS.get(CheeseBlock.Age.FRESH).toStack(), MineraculousItems.CHEESE_WEDGES.get(CheeseBlock.Age.AGED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.CHEESE_WEDGES.get(CheeseBlock.Age.AGED).toStack(), MineraculousBlocks.CHEESE_BLOCKS.get(CheeseBlock.Age.AGED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.CHEESE_BLOCKS.get(CheeseBlock.Age.AGED).toStack(), MineraculousItems.CAMEMBERT_WEDGES.get(CheeseBlock.Age.AGED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.CAMEMBERT_WEDGES.get(CheeseBlock.Age.AGED).toStack(), MineraculousBlocks.CAMEMBERT_BLOCKS.get(CheeseBlock.Age.AGED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.CAMEMBERT_BLOCKS.get(CheeseBlock.Age.AGED).toStack(), MineraculousItems.CHEESE_WEDGES.get(CheeseBlock.Age.RIPENED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.CHEESE_WEDGES.get(CheeseBlock.Age.RIPENED).toStack(), MineraculousBlocks.CHEESE_BLOCKS.get(CheeseBlock.Age.RIPENED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.CHEESE_BLOCKS.get(CheeseBlock.Age.RIPENED).toStack(), MineraculousItems.CAMEMBERT_WEDGES.get(CheeseBlock.Age.RIPENED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.CAMEMBERT_WEDGES.get(CheeseBlock.Age.RIPENED).toStack(), MineraculousBlocks.CAMEMBERT_BLOCKS.get(CheeseBlock.Age.RIPENED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.CAMEMBERT_BLOCKS.get(CheeseBlock.Age.RIPENED).toStack(), MineraculousItems.CHEESE_WEDGES.get(CheeseBlock.Age.EXQUISITE).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.CHEESE_WEDGES.get(CheeseBlock.Age.EXQUISITE).toStack(), MineraculousBlocks.CHEESE_BLOCKS.get(CheeseBlock.Age.EXQUISITE).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.CHEESE_BLOCKS.get(CheeseBlock.Age.EXQUISITE).toStack(), MineraculousItems.CAMEMBERT_WEDGES.get(CheeseBlock.Age.EXQUISITE).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.CAMEMBERT_WEDGES.get(CheeseBlock.Age.EXQUISITE).toStack(), MineraculousBlocks.CAMEMBERT_BLOCKS.get(CheeseBlock.Age.EXQUISITE).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.CAMEMBERT_BLOCKS.get(CheeseBlock.Age.EXQUISITE).toStack(), MineraculousItems.CHEESE_WEDGES.get(CheeseBlock.Age.TIME_HONORED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.CHEESE_WEDGES.get(CheeseBlock.Age.TIME_HONORED).toStack(), MineraculousBlocks.CHEESE_BLOCKS.get(CheeseBlock.Age.TIME_HONORED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.CHEESE_BLOCKS.get(CheeseBlock.Age.TIME_HONORED).toStack(), MineraculousItems.CAMEMBERT_WEDGES.get(CheeseBlock.Age.TIME_HONORED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.CAMEMBERT_WEDGES.get(CheeseBlock.Age.TIME_HONORED).toStack(), MineraculousBlocks.CAMEMBERT_BLOCKS.get(CheeseBlock.Age.TIME_HONORED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.CAMEMBERT_BLOCKS.get(CheeseBlock.Age.TIME_HONORED).toStack(), MineraculousBlocks.WAXED_CHEESE_BLOCKS.get(CheeseBlock.Age.FRESH).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.WAXED_CHEESE_BLOCKS.get(CheeseBlock.Age.FRESH).toStack(), MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS.get(CheeseBlock.Age.FRESH).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS.get(CheeseBlock.Age.FRESH).toStack(), MineraculousBlocks.WAXED_CHEESE_BLOCKS.get(CheeseBlock.Age.AGED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.WAXED_CHEESE_BLOCKS.get(CheeseBlock.Age.AGED).toStack(), MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS.get(CheeseBlock.Age.AGED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS.get(CheeseBlock.Age.AGED).toStack(), MineraculousBlocks.WAXED_CHEESE_BLOCKS.get(CheeseBlock.Age.RIPENED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.WAXED_CHEESE_BLOCKS.get(CheeseBlock.Age.RIPENED).toStack(), MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS.get(CheeseBlock.Age.RIPENED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS.get(CheeseBlock.Age.RIPENED).toStack(), MineraculousBlocks.WAXED_CHEESE_BLOCKS.get(CheeseBlock.Age.EXQUISITE).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.WAXED_CHEESE_BLOCKS.get(CheeseBlock.Age.EXQUISITE).toStack(), MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS.get(CheeseBlock.Age.EXQUISITE).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS.get(CheeseBlock.Age.EXQUISITE).toStack(), MineraculousBlocks.WAXED_CHEESE_BLOCKS.get(CheeseBlock.Age.TIME_HONORED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.WAXED_CHEESE_BLOCKS.get(CheeseBlock.Age.TIME_HONORED).toStack(), MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS.get(CheeseBlock.Age.TIME_HONORED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
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
}
