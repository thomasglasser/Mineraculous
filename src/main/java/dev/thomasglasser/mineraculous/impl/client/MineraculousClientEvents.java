package dev.thomasglasser.mineraculous.impl.client;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.event.ContextDependentCurioRenderEvent;
import dev.thomasglasser.mineraculous.api.client.event.CreatePlayerMenuItemEvent;
import dev.thomasglasser.mineraculous.api.client.event.RenderPlayerLikeEvent;
import dev.thomasglasser.mineraculous.api.client.gui.MineraculousGuiLayers;
import dev.thomasglasser.mineraculous.api.client.gui.screens.ExternalMenuScreen;
import dev.thomasglasser.mineraculous.api.client.gui.screens.inventory.tooltip.ClientLabeledItemTagsTooltip;
import dev.thomasglasser.mineraculous.api.client.particle.FadingParticle;
import dev.thomasglasser.mineraculous.api.client.particle.FlourishingParticle;
import dev.thomasglasser.mineraculous.api.client.particle.HoveringOrbParticle;
import dev.thomasglasser.mineraculous.api.client.particle.StaticParticle;
import dev.thomasglasser.mineraculous.api.client.renderer.MineraculousRenderTypes;
import dev.thomasglasser.mineraculous.api.client.renderer.item.MineraculousItemProperties;
import dev.thomasglasser.mineraculous.api.client.renderer.item.curio.ContextDependentCurioRenderer;
import dev.thomasglasser.mineraculous.api.client.renderer.layer.ConditionalAutoGlowingGeoLayer;
import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.effect.MineraculousMobEffects;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.api.world.inventory.MineraculousMenuTypes;
import dev.thomasglasser.mineraculous.api.world.inventory.tooltip.LabeledItemTagsTooltip;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemDisplayContexts;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.api.world.item.crafting.MineraculousRecipeTypes;
import dev.thomasglasser.mineraculous.api.world.level.block.AgeingCheese;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.api.world.level.storage.abilityeffects.AbilityEffectUtils;
import dev.thomasglasser.mineraculous.api.world.level.storage.abilityeffects.SyncedTransientAbilityEffectData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.client.gui.MineraculousGuis;
import dev.thomasglasser.mineraculous.impl.client.gui.MineraculousHeartTypes;
import dev.thomasglasser.mineraculous.impl.client.gui.screens.inventory.OvenScreen;
import dev.thomasglasser.mineraculous.impl.client.model.BeardModel;
import dev.thomasglasser.mineraculous.impl.client.model.DerbyHatModel;
import dev.thomasglasser.mineraculous.impl.client.model.FaceMaskModel;
import dev.thomasglasser.mineraculous.impl.client.renderer.armor.KamikotizationArmorItemRenderer;
import dev.thomasglasser.mineraculous.impl.client.renderer.armor.MiraculousArmorItemRenderer;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.KwamiRenderer;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.LuckyCharmItemSpawnerRenderer;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.MiraculousLadybugRenderer;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.PlayerLikeRenderer;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.ThrownButterflyCaneRenderer;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.ThrownCatStaffRenderer;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.ThrownLadybugYoyoRenderer;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.YoyoRopeRenderer;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers.BetaTesterLayer;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers.FaceMaskLayer;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers.LegacyDevTeamLayer;
import dev.thomasglasser.mineraculous.impl.client.renderer.item.ButterflyCaneRenderer;
import dev.thomasglasser.mineraculous.impl.client.renderer.item.CatStaffRenderer;
import dev.thomasglasser.mineraculous.impl.client.renderer.item.LadybugYoyoRenderer;
import dev.thomasglasser.mineraculous.impl.client.renderer.item.MiraculousItemRenderer;
import dev.thomasglasser.mineraculous.impl.data.curios.MineraculousCuriosProvider;
import dev.thomasglasser.mineraculous.impl.network.ServerboundRemoteDamagePayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundUpdateYoyoInputPayload;
import dev.thomasglasser.mineraculous.impl.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.impl.world.inventory.MineraculousRecipeBookTypes;
import dev.thomasglasser.mineraculous.impl.world.item.armor.MineraculousArmorUtils;
import dev.thomasglasser.mineraculous.impl.world.level.storage.LeashingLadybugYoyoData;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.particle.FlyStraightTowardsParticle;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
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
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterEntitySpectatorShadersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
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
            CuriosRendererRegistry.register(MineraculousItems.BUTTERFLY_CANE.get(), ContextDependentCurioRenderer::new);

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

            event.insertAfter(Items.BLAST_FURNACE.getDefaultInstance(), MineraculousBlocks.OVEN.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        } else if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {

        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {

        } else if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.insertAfter(Items.SHIELD.getDefaultInstance(), MineraculousItems.LADYBUG_YOYO.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.LADYBUG_YOYO.toStack(), MineraculousItems.CAT_STAFF.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.CAT_STAFF.toStack(), MineraculousItems.BUTTERFLY_CANE.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.BUTTERFLY_CANE.toStack(), MineraculousItems.GREAT_SWORD.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        } else if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.insertAfter(Items.COOKIE.getDefaultInstance(), MineraculousItems.RAW_MACARON.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.RAW_MACARON.toStack(), MineraculousItems.MACARON.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);

            addCheeses(event, Items.PUMPKIN_PIE.getDefaultInstance(), MineraculousItems.CHEESE);
            addCheeses(event, MineraculousItems.CHEESE.get(AgeingCheese.Age.TIME_HONORED).toStack(), MineraculousBlocks.CHEESE);
            addCheeses(event, MineraculousBlocks.CHEESE.get(AgeingCheese.Age.TIME_HONORED).toStack(), MineraculousItems.CAMEMBERT);
            addCheeses(event, MineraculousItems.CAMEMBERT.get(AgeingCheese.Age.TIME_HONORED).toStack(), MineraculousBlocks.CAMEMBERT);
            addCheeses(event, MineraculousBlocks.CAMEMBERT.get(AgeingCheese.Age.TIME_HONORED).toStack(), MineraculousItems.WAXED_CHEESE);
            addCheeses(event, MineraculousItems.WAXED_CHEESE.get(AgeingCheese.Age.TIME_HONORED).toStack(), MineraculousBlocks.WAXED_CHEESE);
            addCheeses(event, MineraculousBlocks.WAXED_CHEESE.get(AgeingCheese.Age.TIME_HONORED).toStack(), MineraculousItems.WAXED_CAMEMBERT);
            addCheeses(event, MineraculousItems.WAXED_CAMEMBERT.get(AgeingCheese.Age.TIME_HONORED).toStack(), MineraculousBlocks.WAXED_CAMEMBERT);
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.insertAfter(Items.BLAZE_POWDER.getDefaultInstance(), MineraculousItems.CATACLYSM_DUST.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);

            // Pottery Sherds
            event.insertAfter(Items.HOWL_POTTERY_SHERD.getDefaultInstance(), MineraculousItems.LADYBUG_POTTERY_SHERD.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);

            // Armor Trims
            event.insertAfter(Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE.getDefaultInstance(), MineraculousItems.LADYBUG_ARMOR_TRIM_SMITHING_TEMPLATE.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.LADYBUG_ARMOR_TRIM_SMITHING_TEMPLATE.toStack(), MineraculousItems.CAT_ARMOR_TRIM_SMITHING_TEMPLATE.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.CAT_ARMOR_TRIM_SMITHING_TEMPLATE.toStack(), MineraculousItems.BUTTERFLY_ARMOR_TRIM_SMITHING_TEMPLATE.toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        } else if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            // Must be in alphabetical order
        } else if (event.getTabKey() == CreativeModeTabs.OP_BLOCKS) {

        }
    }

    private static <T extends ItemLike> void addCheeses(BuildCreativeModeTabContentsEvent event, ItemStack before, Map<AgeingCheese.Age, T> cheeses) {
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
        event.registerEntityRenderer(MineraculousEntityTypes.KAMIKO.get(), context -> new GeoEntityRenderer<>(context, new DefaultedEntityGeoModel<>(MineraculousConstants.modLoc("kamiko"))));
        event.registerEntityRenderer(MineraculousEntityTypes.LUCKY_CHARM_ITEM_SPAWNER.get(), LuckyCharmItemSpawnerRenderer::new);
        event.registerEntityRenderer(MineraculousEntityTypes.THROWN_CAT_STAFF.get(), ThrownCatStaffRenderer::new);
        event.registerEntityRenderer(MineraculousEntityTypes.THROWN_BUTTERFLY_CANE.get(), ThrownButterflyCaneRenderer::new);
        event.registerEntityRenderer(MineraculousEntityTypes.THROWN_LADYBUG_YOYO.get(), ThrownLadybugYoyoRenderer::new);
        event.registerEntityRenderer(MineraculousEntityTypes.MIRACULOUS_LADYBUG.get(), MiraculousLadybugRenderer::new);
    }

    static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(MineraculousParticleTypes.BLACK_ORB.get(), HoveringOrbParticle.Provider::new);
        event.registerSpriteSet(MineraculousParticleTypes.KAMIKOTIZATION.get(), StaticParticle.Provider::new);
        event.registerSpriteSet(MineraculousParticleTypes.REVERTING_LADYBUG.get(), FlourishingParticle.Provider::new);
        event.registerSpriteSet(MineraculousParticleTypes.SPARKLE.get(), FadingParticle.Provider::new);
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

        PlayerLikeRenderer.refreshModels(event.getContext());
    }

    private static final int DEFAULT_MACARON_COLOR = 0xFFf9d7a4;

    static void onRegisterItemColorHandlers(RegisterColorHandlersEvent.Item event) {
        event.register((stack, index) -> index == 0 ? FastColor.ARGB32.opaque(MiraculousItemRenderer.getMiraculousOrDefault(stack).value().color().getValue()) : -1, MineraculousArmors.MIRACULOUS.getAllAsItems().toArray(new Item[0]));
        event.register((stack, index) -> index == 0 ? DyedItemColor.getOrDefault(stack, DEFAULT_MACARON_COLOR) : -1, MineraculousItems.RAW_MACARON, MineraculousItems.MACARON);
    }

    static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        // Clears old rendering data on reload
        event.registerReloadListener((ResourceManagerReloadListener) resourceManager -> {
            MineraculousClientUtils.syncSpecialPlayerChoices();
            MineraculousArmorUtils.clearAnimationData();
            MiraculousItemRenderer.clearAssets();
            MiraculousArmorItemRenderer.clearAssets();
            KamikotizationArmorItemRenderer.clearModels();
            MineraculousClientUtils.refreshCataclysmPixels();
            ConditionalAutoGlowingGeoLayer.clearGlowmasks();
        });
    }

    static void onRegisterRenderBuffers(RegisterRenderBuffersEvent event) {
        event.registerRenderBuffer(MineraculousRenderTypes.itemLuckyCharm());
        event.registerRenderBuffer(MineraculousRenderTypes.armorLuckyCharm());
        event.registerRenderBuffer(MineraculousRenderTypes.entityLuckyCharm());
        event.registerRenderBuffer(MineraculousRenderTypes.shieldLuckyCharm());
        event.registerRenderBuffer(MineraculousRenderTypes.itemKamikotizing());
        event.registerRenderBuffer(MineraculousRenderTypes.armorKamikotizing());
        event.registerRenderBuffer(MineraculousRenderTypes.entityKamikotizing());
        event.registerRenderBuffer(MineraculousRenderTypes.shieldKamikotizing());
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
        event.registerItem(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new CatStaffRenderer();
                return renderer;
            }

            @Override
            public ResourceLocation getScopeOverlayTexture(ItemStack stack) {
                return CatStaffRenderer.SPYGLASS_SCOPE_LOCATION;
            }
        }, MineraculousItems.CAT_STAFF);
        event.registerItem(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new ButterflyCaneRenderer();
                return renderer;
            }

            @Override
            public ResourceLocation getScopeOverlayTexture(ItemStack stack) {
                return ButterflyCaneRenderer.SPYGLASS_SCOPE_LOCATION;
            }
        }, MineraculousItems.BUTTERFLY_CANE);
    }

    static void onRegisterClientTooltipComponentFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(LabeledItemTagsTooltip.class, ClientLabeledItemTagsTooltip::new);
    }

    // GUI
    static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(MineraculousGuiLayers.STEALING_PROGRESS_BAR, MineraculousGuis::renderStealingProgressBar);
        event.registerAboveAll(MineraculousGuiLayers.REVOKE_BUTTON, MineraculousGuis::renderRevokeButton);
        event.registerAboveAll(MineraculousGuiLayers.KAMIKO_HOTBAR, MineraculousGuis.getKamikoGui()::renderHotbar);
        event.registerAboveAll(MineraculousGuiLayers.KAMIKO_TOOLTIP, MineraculousGuis.getKamikoGui()::renderTooltip);
    }

    static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(MineraculousMenuTypes.OVEN.get(), OvenScreen::new);
    }

    static void onRegisterRecipeBookCategories(RegisterRecipeBookCategoriesEvent event) {
        event.registerAggregateCategory(MineraculousRecipeBookCategories.OVEN_SEARCH.getValue(), ImmutableList.of(MineraculousRecipeBookCategories.OVEN_FOOD.getValue()));
        event.registerBookCategories(MineraculousRecipeBookTypes.OVEN.getValue(), ImmutableList.of(MineraculousRecipeBookCategories.OVEN_SEARCH.getValue(), MineraculousRecipeBookCategories.OVEN_FOOD.getValue()));
        event.registerRecipeCategoryFinder(MineraculousRecipeTypes.OVEN_COOKING.get(), recipe -> MineraculousRecipeBookCategories.OVEN_FOOD.getValue());
    }

    static void onCreatePlayerMenuItem(CreatePlayerMenuItemEvent event) {
        Player player = event.getEntity();
        List<Holder<Miraculous>> transformed = player.getData(MineraculousAttachmentTypes.MIRACULOUSES).getTransformed();
        if (!transformed.isEmpty()) {
            event.setDisplay(Miraculous.createMiraculousStack(transformed.getFirst()));
        }
    }

    // Tick
    static void onClientTick(ClientTickEvent.Post event) {
        if (MineraculousClientUtils.getCameraEntity() != null && MineraculousClientUtils.getCameraEntity().isRemoved())
            MineraculousClientUtils.setCameraEntity(null);

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            checkYoyoInput(player);
            if (Minecraft.getInstance().player.level() != null)
                for (Player otherPlayer : Minecraft.getInstance().player.level().players()) {
                    playerPerchRendererMap.computeIfAbsent(otherPlayer.getUUID(), k -> new CatStaffRenderer.PerchRenderer()).tick(otherPlayer);
                }
        }
    }

    private static void checkYoyoInput(LocalPlayer player) {
        MineraculousClientUtils.InputState input = MineraculousClientUtils.captureInput();
        if (player.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO).id().isPresent() && input.hasInput()) {
            int packedInput = input.packInputs();
            TommyLibServices.NETWORK.sendToServer(new ServerboundUpdateYoyoInputPayload(packedInput));
        }
    }

    // Input
    static void onKeyInput(InputEvent.Key event) {
        if (MineraculousClientUtils.isInKamikoView()) {
            for (int i = 0; i < 9; i++) {
                while (Minecraft.getInstance().options.keyHotbarSlots[i].consumeClick()) {
                    MineraculousGuis.getKamikoGui().onHotbarSelected(i);
                }
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
        Player player = ClientUtils.getLocalPlayer();
        if (player != null) {
            SyncedTransientAbilityEffectData abilityEffectData = player.getData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS);
            if (abilityEffectData.spectatingId().isPresent()) {
                event.setCanceled(true);
                if (event.isAttack() && abilityEffectData.allowRemoteDamage()) {
                    TommyLibServices.NETWORK.sendToServer(ServerboundRemoteDamagePayload.INSTANCE);
                } else {
                    event.setSwingHand(false);
                }
            }
        }
    }

    // Rendering
    static void onRenderHand(RenderHandEvent event) {
        if (MineraculousClientUtils.getCameraEntity() != Minecraft.getInstance().player) {
            event.setCanceled(true);
        }
    }

    private static final Map<UUID, CatStaffRenderer.PerchRenderer> playerPerchRendererMap = new Object2ObjectOpenHashMap<>();

    public static void onPlayerRendererPost(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        int light = event.getPackedLight();
        float partialTick = event.getPartialTick();

        player.noCulling = true;
        if (playerPerchRendererMap.containsKey(player.getUUID()))
            playerPerchRendererMap.get(player.getUUID()).renderPerch(player, poseStack, bufferSource, light, partialTick);
        CatStaffRenderer.renderTravel(player, poseStack, bufferSource, light, partialTick);
        player.noCulling = false;
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
            poseStack.pushPose();
            poseStack.translate(0, -1.6d, 0);
            if (playerPerchRendererMap.containsKey(player.getUUID()))
                playerPerchRendererMap.get(player.getUUID()).renderPerch(player, poseStack, bufferSource, light, partialTick);
            CatStaffRenderer.renderTravel(player, poseStack, bufferSource, light, partialTick);
            poseStack.popPose();

            if (MineraculousClientUtils.getCameraEntity() instanceof Leashable leashable && leashable.getLeashHolder() instanceof Player holder) {
                holder.getData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO).map(LeashingLadybugYoyoData::maxRopeLength).ifPresent(maxLength -> {
                    double y = (leashable instanceof LivingEntity livingLeashed && livingLeashed.isCrouching()) ? -1.2d : -1.6d;
                    poseStack.translate(0, y, 0);
                    YoyoRopeRenderer.render((Entity) leashable, holder, maxLength + 1.3d, poseStack, bufferSource, partialTick);
                });
            }
        }
    }

    static void onPreRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        Player player = ClientUtils.getLocalPlayer();
        if (player != null && player.getData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS).spectatingId().isPresent()) {
            if (!MineraculousGuiLayers.isAllowedSpectatingGuiLayer(event.getName())) {
                event.setCanceled(true);
            }
        }
    }

    static void onRenderInventoryMobEffects(ScreenEvent.RenderInventoryMobEffects event) {
        if (Minecraft.getInstance().screen instanceof ExternalMenuScreen) {
            event.setCanceled(true);
        }
    }

    static void onDetermineCurioRenderContext(ContextDependentCurioRenderEvent.DetermineContext<?, ?> event) {
        String slot = event.getSlot();
        if (slot.equals(MineraculousCuriosProvider.SLOT_RING))
            event.setDisplayContext(MineraculousItemDisplayContexts.CURIOS_RIGHT_ARM.getValue());
        else if (slot.equals(MineraculousCuriosProvider.SLOT_EARRINGS))
            event.setDisplayContext(MineraculousItemDisplayContexts.CURIOS_LEFT_EARRING.getValue());
    }

    static void onPostContextDependentCurioRender(ContextDependentCurioRenderEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();
        ItemStack stack = event.getStack();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource renderTypeBuffer = event.getRenderTypeBuffer();
        int light = event.getLight();
        ItemDisplayContext displayContext = event.getDisplayContext();
        ItemInHandRenderer renderer = event.getItemInHandRenderer();
        if (displayContext == MineraculousItemDisplayContexts.CURIOS_LEFT_EARRING.getValue()) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            poseStack.translate(0, 0, 1 / 16f);
            renderer.renderItem(entity, stack, MineraculousItemDisplayContexts.CURIOS_RIGHT_EARRING.getValue(), false, poseStack, renderTypeBuffer, light);
        }
    }

    // Player-Like Rendering
    public static void onRenderPlayerLikeCape(RenderPlayerLikeEvent.RenderCape<?> event) {
        if (event.getPlayerLike() instanceof LivingEntity entity && MineraculousClientUtils.shouldNotRenderCape(entity)) {
            event.setCanceled(true);
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
        Player player = ClientUtils.getLocalPlayer();
        if (player != null && !AbilityEffectUtils.isMessageAllowed(player, event.getSender())) {
            event.setCanceled(true);
        }
    }
}
