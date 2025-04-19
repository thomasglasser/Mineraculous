package dev.thomasglasser.mineraculous.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.gui.MineraculousHeartTypes;
import dev.thomasglasser.mineraculous.client.gui.components.kamiko.KamikoGui;
import dev.thomasglasser.mineraculous.client.gui.screens.RadialMenuOption;
import dev.thomasglasser.mineraculous.client.gui.screens.RadialMenuScreen;
import dev.thomasglasser.mineraculous.client.model.KamikoMaskModel;
import dev.thomasglasser.mineraculous.client.particle.HoveringOrbParticle;
import dev.thomasglasser.mineraculous.client.particle.KamikotizationParticle;
import dev.thomasglasser.mineraculous.client.particle.RiseAndSpreadParticle;
import dev.thomasglasser.mineraculous.client.renderer.MineraculousRenderTypes;
import dev.thomasglasser.mineraculous.client.renderer.entity.KamikoRenderer;
import dev.thomasglasser.mineraculous.client.renderer.entity.KwamiRenderer;
import dev.thomasglasser.mineraculous.client.renderer.entity.LuckyCharmItemSpawnerRenderer;
import dev.thomasglasser.mineraculous.client.renderer.entity.ThrownButterflyCaneRenderer;
import dev.thomasglasser.mineraculous.client.renderer.entity.ThrownCatStaffRenderer;
import dev.thomasglasser.mineraculous.client.renderer.entity.ThrownLadybugYoyoRenderer;
import dev.thomasglasser.mineraculous.client.renderer.entity.layers.KamikoMaskLayer;
import dev.thomasglasser.mineraculous.client.renderer.entity.layers.KwamiOnShoulderLayer;
import dev.thomasglasser.mineraculous.client.renderer.item.MineraculousItemProperties;
import dev.thomasglasser.mineraculous.client.renderer.item.curio.ContextDependentCurioRenderer;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.network.ServerboundKamikotizationTransformPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSendOffhandSwingPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSetOwnerPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSetToggleTagPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.armor.KamikotizationArmorItem;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.world.item.armor.MiraculousArmorItem;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedKamikotizationLookData;
import dev.thomasglasser.mineraculous.world.level.storage.PerchCatStaffData;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlyStraightTowardsParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.config.ModConfig;
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
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerHeartTypeEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

public class MineraculousClientEvents {
    public static final String PLAYER_ANIMATIONS_PATH = "animations/player";
    public static final String REVOKE = "gui.mineraculous.revoke";
    public static final String REVOKE_WITH_SPACE = "gui.mineraculous.revoke_with_space";

    private static KamikoGui kamikoGui;

    public static void onFMLClientSetup(FMLClientSetupEvent event) {
        CuriosRendererRegistry.register(MineraculousItems.MIRACULOUS.get(), ContextDependentCurioRenderer::new);
        CuriosRendererRegistry.register(MineraculousItems.CAT_STAFF.get(), ContextDependentCurioRenderer::new);
        CuriosRendererRegistry.register(MineraculousItems.LADYBUG_YOYO.get(), ContextDependentCurioRenderer::new);

        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                ResourceLocation.fromNamespaceAndPath(Mineraculous.MOD_ID, "animation"),
                42,
                MineraculousClientEvents::registerPlayerAnimation);
        MineraculousItemProperties.init();
    }

    private static IAnimation registerPlayerAnimation(AbstractClientPlayer player) {
        //This will be invoked for every new player
        return new ModifierLayer<>();
    }

    public static void openToolWheel(int color, ItemStack stack, Consumer<RadialMenuOption> onSelected, RadialMenuOption... options) {
        if (ClientUtils.getMinecraft().screen == null) {
            ClientUtils.setScreen(new RadialMenuScreen(Arrays.asList(options), stack, onSelected, MineraculousKeyMappings.OPEN_TOOL_WHEEL.get().getKey().getValue(), color));
        }
    }

    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        Map<ResourceLocation, Resource> miraculous = manager.listResources("models/item/miraculous", (location -> location.getPath().endsWith(".json")));
        for (ResourceLocation rl : miraculous.keySet()) {
            ResourceLocation stripped = ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), rl.getPath().substring("models/".length(), rl.getPath().indexOf(".json")));
            event.register(ModelResourceLocation.standalone(stripped));
        }

        event.register(ModelResourceLocation.standalone(Mineraculous.modLoc("item/kamikotization/armor")));
    }

    public static void onRegisterRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MineraculousEntityTypes.KWAMI.get(), KwamiRenderer::new);
        event.registerEntityRenderer(MineraculousEntityTypes.KAMIKO.get(), KamikoRenderer::new);
        event.registerEntityRenderer(MineraculousEntityTypes.LUCKY_CHARM_ITEM_SPAWNER.get(), LuckyCharmItemSpawnerRenderer::new);
        event.registerEntityRenderer(MineraculousEntityTypes.THROWN_CAT_STAFF.get(), ThrownCatStaffRenderer::new);
        event.registerEntityRenderer(MineraculousEntityTypes.THROWN_BUTTERFLY_CANE.get(), ThrownButterflyCaneRenderer::new);
        event.registerEntityRenderer(MineraculousEntityTypes.THROWN_LADYBUG_YOYO.get(), ThrownLadybugYoyoRenderer::new);
    }

    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(MineraculousParticleTypes.BLACK_ORB.get(), HoveringOrbParticle.Provider::new);
        event.registerSpriteSet(MineraculousParticleTypes.KAMIKOTIZATION.get(), KamikotizationParticle.Provider::new);
        event.registerSpriteSet(MineraculousParticleTypes.SUMMONING_LADYBUG.get(), sprites -> new ParticleProvider<>() {
            @Override
            public @Nullable Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
                FlyStraightTowardsParticle flystraighttowardsparticle = new FlyStraightTowardsParticle(
                        level, x, y, z, xSpeed, ySpeed, zSpeed, -1, -1);
                flystraighttowardsparticle.pickSprite(sprites);
                return flystraighttowardsparticle;
            }
        });
        event.registerSpriteSet(MineraculousParticleTypes.SPREADING_LADYBUG.get(), RiseAndSpreadParticle.Provider::new);
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
        int width = data.getInt(MineraculousEntityEvents.TAG_TAKE_TICKS);
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
            revokeButton = new Button(Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - 100, Minecraft.getInstance().getWindow().getGuiScaledHeight() - 35, 200, 20, Component.translatable(REVOKE), button -> {
                Entity cameraEntity = MineraculousClientUtils.getCameraEntity();
                if (cameraEntity instanceof Player target) {
                    TommyLibServices.NETWORK.sendToServer(new ServerboundKamikotizationTransformPayload(Optional.of(target.getUUID()), target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).orElseThrow(), false, false, false, target.position().add(0, 1, 0)));
                    TommyLibServices.NETWORK.sendToServer(new ServerboundSetToggleTagPayload(MineraculousEntityEvents.TAG_SHOW_KAMIKO_MASK, false));
                } else if (cameraEntity instanceof Kamiko kamiko) {
                    TommyLibServices.NETWORK.sendToServer(new ServerboundSetOwnerPayload(kamiko.getId(), Optional.empty()));
                }
                MineraculousClientUtils.setCameraEntity(ClientUtils.getMainClientPlayer());
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

        if (!ClientUtils.getMainClientPlayer().isSpectator() && ClientUtils.getMainClientPlayer().getData(MineraculousAttachmentTypes.MIRACULOUS).isTransformed() && (MineraculousClientUtils.getCameraEntity() instanceof Kamiko kamiko && ClientUtils.getMainClientPlayer().getUUID().equals(kamiko.getOwnerUUID())) || (MineraculousClientUtils.getCameraEntity() instanceof Player player && player != ClientUtils.getMainClientPlayer() && player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent())) {
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

    public static void onPlayerRendererPost(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        int light = event.getPackedLight();

        ItemStack leftH = player.getOffhandItem();
        ItemStack rightH = player.getMainHandItem();

        boolean lHCatStaff = leftH.is(MineraculousItems.CAT_STAFF) && leftH.has(MineraculousDataComponents.ACTIVE) && leftH.get(MineraculousDataComponents.CAT_STAFF_ABILITY) == CatStaffItem.Ability.PERCH;
        boolean rHCatStaff = rightH.is(MineraculousItems.CAT_STAFF) && rightH.has(MineraculousDataComponents.ACTIVE) && rightH.get(MineraculousDataComponents.CAT_STAFF_ABILITY) == CatStaffItem.Ability.PERCH;
        if (lHCatStaff || rHCatStaff) {
            player.noCulling = true;
            renderCatStaffPerch(player, poseStack, bufferSource, light, event.getPartialTick());
        }
    }

    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        AbstractClientPlayer player = Minecraft.getInstance().player;
        RenderLevelStageEvent.Stage stage = event.getStage();
        EntityRenderDispatcher renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        float partialTicks = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPartialTickTime();

        ItemStack leftH = player.getOffhandItem();
        ItemStack rightH = player.getMainHandItem();

        boolean lHCatStaff = leftH.is(MineraculousItems.CAT_STAFF) && leftH.has(MineraculousDataComponents.ACTIVE) && leftH.get(MineraculousDataComponents.CAT_STAFF_ABILITY) == CatStaffItem.Ability.PERCH;
        boolean rHCatStaff = rightH.is(MineraculousItems.CAT_STAFF) && rightH.has(MineraculousDataComponents.ACTIVE) && rightH.get(MineraculousDataComponents.CAT_STAFF_ABILITY) == CatStaffItem.Ability.PERCH;
        if (stage == RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS && renderDispatcher.options.getCameraType().isFirstPerson() && (lHCatStaff || rHCatStaff)) {
            int light = renderDispatcher.getPackedLightCoords(player, partialTicks);
            poseStack.translate(0, -1.6d, 0);
            renderCatStaffPerch(player, poseStack, bufferSource, light, partialTicks);
        }
    }

    public static void renderCatStaffPerch(Player player, PoseStack poseStack, MultiBufferSource bufferSource, int light, float partialTicks) {
        float PIXEL = 1 / 16f;
        PerchCatStaffData perchData = player.getData(MineraculousAttachmentTypes.PERCH_CAT_STAFF);
        float length = perchData.length();
        boolean catStaffPerchRender = perchData.canRender();
        poseStack.pushPose();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(Mineraculous.modLoc("textures/misc/cat_staff_perching.png")));
        PoseStack.Pose pose = poseStack.last();

        //STAFF - PLAYER NEW
        float bodyAngle = player.getData(MineraculousAttachmentTypes.PERCH_CAT_STAFF).initPos().y;
        Vector3f bodyDirectionF = player.getData(MineraculousAttachmentTypes.PERCH_CAT_STAFF).initPos();
        double d0, d1;
        d0 = Mth.lerp(partialTicks, player.xo, player.getX());
        d1 = Mth.lerp(partialTicks, player.zo, player.getZ());
        bodyDirectionF = new Vector3f((float) (bodyDirectionF.x - d0), 0f, (float) (bodyDirectionF.z - d1));

        int direction = -1;
        if (bodyAngle <= 45 || bodyAngle > 270 + 45) //south
            direction = 1; //+z
        else if (bodyAngle > 45 && bodyAngle <= 90 + 45)//east
            direction = 4; //-x
        else if (bodyAngle > 90 + 45 && bodyAngle <= 180 + 45)//north
            direction = 3; //-z
        else if (bodyAngle > 180 + 45 && bodyAngle <= 270 + 45)//west
            direction = 2; //+x
        if (catStaffPerchRender) {
            //SIDES:
            int s = (int) (player.getBbHeight() + 0.5);
            int d;
            for (d = s - 1; d >= (int) length; d--) {
                vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), s, +PIXEL + bodyDirectionF.z(), 0f, 0f, light);
                vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), d, +PIXEL + bodyDirectionF.z(), 0f, 1f, light);
                vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), d, -PIXEL + bodyDirectionF.z(), PIXEL * 2, 1f, light);
                vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), s, -PIXEL + bodyDirectionF.z(), PIXEL * 2, 0f, light);

                vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), s, -PIXEL + bodyDirectionF.z(), PIXEL * 2, 0f, light);
                vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), d, -PIXEL + bodyDirectionF.z(), PIXEL * 2, 1f, light);
                vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), d, -PIXEL + bodyDirectionF.z(), PIXEL * 4, 1f, light);
                vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), s, -PIXEL + bodyDirectionF.z(), PIXEL * 4, 0f, light);

                vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), s, -PIXEL + bodyDirectionF.z(), PIXEL * 4, 0f, light);
                vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), d, -PIXEL + bodyDirectionF.z(), PIXEL * 4, 1f, light);
                vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), d, +PIXEL + bodyDirectionF.z(), PIXEL * 6, 1f, light);
                vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), s, +PIXEL + bodyDirectionF.z(), PIXEL * 6, 0f, light);

                vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), s, +PIXEL + bodyDirectionF.z(), PIXEL * 6, 0f, light);
                vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), d, +PIXEL + bodyDirectionF.z(), PIXEL * 6, 1f, light);
                vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), d, +PIXEL + bodyDirectionF.z(), PIXEL * 8, 1f, light);
                vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), s, +PIXEL + bodyDirectionF.z(), PIXEL * 8, 0f, light);

                s = d;
            }
            float x = length - (int) (length);
            x = Math.abs(x);
            if (x != 0) {
                vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), (int) (length), +PIXEL + bodyDirectionF.z(), 0f, 0f, light);
                vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), length, +PIXEL + bodyDirectionF.z(), 0f, x, light);
                vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), length, -PIXEL + bodyDirectionF.z(), PIXEL * 2, x, light);
                vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), (int) (length), -PIXEL + bodyDirectionF.z(), PIXEL * 2, 0f, light);

                vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), (int) (length), -PIXEL + bodyDirectionF.z(), PIXEL * 2, 0f, light);
                vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), length, -PIXEL + bodyDirectionF.z(), PIXEL * 2, x, light);
                vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), length, -PIXEL + bodyDirectionF.z(), PIXEL * 4, x, light);
                vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), (int) (length), -PIXEL + bodyDirectionF.z(), PIXEL * 4, 0f, light);

                vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), (int) (length), -PIXEL + bodyDirectionF.z(), PIXEL * 4, 0f, light);
                vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), length, -PIXEL + bodyDirectionF.z(), PIXEL * 4, x, light);
                vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), length, +PIXEL + bodyDirectionF.z(), PIXEL * 6, x, light);
                vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), (int) (length), +PIXEL + bodyDirectionF.z(), PIXEL * 6, 0f, light);

                vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), (int) (length), +PIXEL + bodyDirectionF.z(), PIXEL * 6, 0f, light);
                vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), length, +PIXEL + bodyDirectionF.z(), PIXEL * 6, x, light);
                vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), length, +PIXEL + bodyDirectionF.z(), PIXEL * 8, x, light);
                vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), (int) (length), +PIXEL + bodyDirectionF.z(), PIXEL * 8, 0f, light);
            }
            //UP&DOWN:
            vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), 0, -PIXEL + bodyDirectionF.z(), PIXEL * 14, 0f, light);
            vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), 0, -PIXEL + bodyDirectionF.z(), PIXEL * 14, PIXEL * 2, light);
            vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), 0, +PIXEL + bodyDirectionF.z(), 1f, PIXEL * 2, light);
            vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), 0, +PIXEL + bodyDirectionF.z(), 1f, 0f, light);
            vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), length, -PIXEL + bodyDirectionF.z(), PIXEL * 14, PIXEL * 2, light);
            vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), length, -PIXEL + bodyDirectionF.z(), PIXEL * 14, PIXEL * 4, light);
            vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), length, +PIXEL + bodyDirectionF.z(), 1f, PIXEL * 4, light);
            vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), length, +PIXEL + bodyDirectionF.z(), 1f, PIXEL * 2, light);
            //PAW:
            int a1 = 1, a2 = 1, a3 = 1, a4 = 1;
            switch (direction) {
                case 1:
                    a1 = -1;
                    a3 = -1;
                    a4 = -1;
                    break;
                case 2:
                    a1 = -1;
                    a2 = -1;
                    a4 = -1;
                    break;
                case 3:
                    a1 = -1;
                    break;
                case 4:
                    a4 = -1;
                    break;
            }
            vertex(vertexConsumer, pose, a1 * (PIXEL + 0.001f) + bodyDirectionF.x(), player.getEyeHeight(), a3 * (PIXEL + 0.001f) + bodyDirectionF.z(), PIXEL * 9, PIXEL * 8, 15728880);
            vertex(vertexConsumer, pose, a1 * (PIXEL + 0.001f) + bodyDirectionF.x(), -PIXEL * 2f + player.getEyeHeight(), a3 * (PIXEL + 0.001f) + bodyDirectionF.z(), PIXEL * 9, PIXEL * 15, 15728880);
            vertex(vertexConsumer, pose, a2 * (PIXEL + 0.001f) + bodyDirectionF.x(), -PIXEL * 2f + player.getEyeHeight(), a4 * (PIXEL + 0.001f) + bodyDirectionF.z(), PIXEL * 16, PIXEL * 15, 15728880);
            vertex(vertexConsumer, pose, a2 * (PIXEL + 0.001f) + bodyDirectionF.x(), player.getEyeHeight(), a4 * (PIXEL + 0.001f) + bodyDirectionF.z(), PIXEL * 16, PIXEL * 8, 15728880);

            //LINES:
            vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), -PIXEL * 3f + player.getEyeHeight(), -PIXEL - 0.0001f + bodyDirectionF.z(), PIXEL * 8, PIXEL * 4, 15728880);
            vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), -PIXEL * 3.5f + player.getEyeHeight(), -PIXEL - 0.0001f + bodyDirectionF.z(), PIXEL * 8, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), -PIXEL * 3.5f + player.getEyeHeight(), -PIXEL - 0.0001f + bodyDirectionF.z(), PIXEL * 12, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), -PIXEL * 3f + player.getEyeHeight(), -PIXEL - 0.0001f + bodyDirectionF.z(), PIXEL * 12, PIXEL * 4, 15728880);

            vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), -PIXEL * 3f + player.getEyeHeight(), +PIXEL + 0.0001f + bodyDirectionF.z(), PIXEL * 8, PIXEL * 4, 15728880);
            vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), -PIXEL * 3.5f + player.getEyeHeight(), +PIXEL + 0.0001f + bodyDirectionF.z(), PIXEL * 8, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), -PIXEL * 3.5f + player.getEyeHeight(), +PIXEL + 0.0001f + bodyDirectionF.z(), PIXEL * 12, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), -PIXEL * 3f + player.getEyeHeight(), +PIXEL + 0.0001f + bodyDirectionF.z(), PIXEL * 12, PIXEL * 4, 15728880);

            vertex(vertexConsumer, pose, +PIXEL + 0.0001f + bodyDirectionF.x(), -PIXEL * 3f + player.getEyeHeight(), +PIXEL + bodyDirectionF.z(), PIXEL * 8, PIXEL * 4, 15728880);
            vertex(vertexConsumer, pose, +PIXEL + 0.0001f + bodyDirectionF.x(), -PIXEL * 3.5f + player.getEyeHeight(), +PIXEL + bodyDirectionF.z(), PIXEL * 8, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, +PIXEL + 0.0001f + bodyDirectionF.x(), -PIXEL * 3.5f + player.getEyeHeight(), -PIXEL + bodyDirectionF.z(), PIXEL * 12, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, +PIXEL + 0.0001f + bodyDirectionF.x(), -PIXEL * 3f + player.getEyeHeight(), -PIXEL + bodyDirectionF.z(), PIXEL * 12, PIXEL * 4, 15728880);

            vertex(vertexConsumer, pose, -PIXEL - 0.0001f + bodyDirectionF.x(), -PIXEL * 3f + player.getEyeHeight(), -PIXEL + bodyDirectionF.z(), PIXEL * 8, PIXEL * 4, 15728880);
            vertex(vertexConsumer, pose, -PIXEL - 0.0001f + bodyDirectionF.x(), -PIXEL * 3.5f + player.getEyeHeight(), -PIXEL + bodyDirectionF.z(), PIXEL * 8, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, -PIXEL - 0.0001f + bodyDirectionF.x(), -PIXEL * 3.5f + player.getEyeHeight(), +PIXEL + bodyDirectionF.z(), PIXEL * 12, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, -PIXEL - 0.0001f + bodyDirectionF.x(), -PIXEL * 3f + player.getEyeHeight(), +PIXEL + bodyDirectionF.z(), PIXEL * 12, PIXEL * 4, 15728880);

            vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), PIXEL + player.getEyeHeight(), -PIXEL - 0.0001f + bodyDirectionF.z(), PIXEL * 8, PIXEL * 4, 15728880);
            vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), PIXEL * 1.5f + player.getEyeHeight(), -PIXEL - 0.0001f + bodyDirectionF.z(), PIXEL * 8, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), PIXEL * 1.5f + player.getEyeHeight(), -PIXEL - 0.0001f + bodyDirectionF.z(), PIXEL * 12, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), PIXEL + player.getEyeHeight(), -PIXEL - 0.0001f + bodyDirectionF.z(), PIXEL * 12, PIXEL * 4, 15728880);

            vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), PIXEL + player.getEyeHeight(), +PIXEL + 0.0001f + bodyDirectionF.z(), PIXEL * 8, PIXEL * 4, 15728880);
            vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), PIXEL * 1.5f + player.getEyeHeight(), +PIXEL + 0.0001f + bodyDirectionF.z(), PIXEL * 8, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), PIXEL * 1.5f + player.getEyeHeight(), +PIXEL + 0.0001f + bodyDirectionF.z(), PIXEL * 12, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), PIXEL + player.getEyeHeight(), +PIXEL + 0.0001f + bodyDirectionF.z(), PIXEL * 12, PIXEL * 4, 15728880);

            vertex(vertexConsumer, pose, +PIXEL + 0.0001f + bodyDirectionF.x(), PIXEL + player.getEyeHeight(), +PIXEL + bodyDirectionF.z(), PIXEL * 8, PIXEL * 4, 15728880);
            vertex(vertexConsumer, pose, +PIXEL + 0.0001f + bodyDirectionF.x(), PIXEL * 1.5f + player.getEyeHeight(), +PIXEL + bodyDirectionF.z(), PIXEL * 8, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, +PIXEL + 0.0001f + bodyDirectionF.x(), PIXEL * 1.5f + player.getEyeHeight(), -PIXEL + bodyDirectionF.z(), PIXEL * 12, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, +PIXEL + 0.0001f + bodyDirectionF.x(), PIXEL + player.getEyeHeight(), -PIXEL + bodyDirectionF.z(), PIXEL * 12, PIXEL * 4, 15728880);

            vertex(vertexConsumer, pose, -PIXEL - 0.0001f + bodyDirectionF.x(), PIXEL + player.getEyeHeight(), -PIXEL + bodyDirectionF.z(), PIXEL * 8, PIXEL * 4, 15728880);
            vertex(vertexConsumer, pose, -PIXEL - 0.0001f + bodyDirectionF.x(), PIXEL * 1.5f + player.getEyeHeight(), -PIXEL + bodyDirectionF.z(), PIXEL * 8, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, -PIXEL - 0.0001f + bodyDirectionF.x(), PIXEL * 1.5f + player.getEyeHeight(), +PIXEL + bodyDirectionF.z(), PIXEL * 12, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, -PIXEL - 0.0001f + bodyDirectionF.x(), PIXEL + player.getEyeHeight(), +PIXEL + bodyDirectionF.z(), PIXEL * 12, PIXEL * 4, 15728880);

            vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), PIXEL * 4.5f + player.getEyeHeight(), -PIXEL - 0.0001f + bodyDirectionF.z(), PIXEL * 8, PIXEL * 4, 15728880);
            vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), PIXEL * 5f + player.getEyeHeight(), -PIXEL - 0.0001f + bodyDirectionF.z(), PIXEL * 8, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), PIXEL * 5f + player.getEyeHeight(), -PIXEL - 0.0001f + bodyDirectionF.z(), PIXEL * 12, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), PIXEL * 4.5f + player.getEyeHeight(), -PIXEL - 0.0001f + bodyDirectionF.z(), PIXEL * 12, PIXEL * 4, 15728880);

            vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), PIXEL * 4.5f + player.getEyeHeight(), +PIXEL + 0.0001f + bodyDirectionF.z(), PIXEL * 8, PIXEL * 4, 15728880);
            vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), PIXEL * 5f + player.getEyeHeight(), +PIXEL + 0.0001f + bodyDirectionF.z(), PIXEL * 8, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), PIXEL * 5f + player.getEyeHeight(), +PIXEL + 0.0001f + bodyDirectionF.z(), PIXEL * 12, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), PIXEL * 4.5f + player.getEyeHeight(), +PIXEL + 0.0001f + bodyDirectionF.z(), PIXEL * 12, PIXEL * 4, 15728880);

            vertex(vertexConsumer, pose, +PIXEL + 0.0001f + bodyDirectionF.x(), PIXEL * 4.5f + player.getEyeHeight(), +PIXEL + bodyDirectionF.z(), PIXEL * 8, PIXEL * 4, 15728880);
            vertex(vertexConsumer, pose, +PIXEL + 0.0001f + bodyDirectionF.x(), PIXEL * 5 + player.getEyeHeight(), +PIXEL + bodyDirectionF.z(), PIXEL * 8, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, +PIXEL + 0.0001f + bodyDirectionF.x(), PIXEL * 5 + player.getEyeHeight(), -PIXEL + bodyDirectionF.z(), PIXEL * 12, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, +PIXEL + 0.0001f + bodyDirectionF.x(), PIXEL * 4.5f + player.getEyeHeight(), -PIXEL + bodyDirectionF.z(), PIXEL * 12, PIXEL * 4, 15728880);

            vertex(vertexConsumer, pose, -PIXEL - 0.0001f + bodyDirectionF.x(), PIXEL * 4.5f + player.getEyeHeight(), -PIXEL + bodyDirectionF.z(), PIXEL * 8, PIXEL * 4, 15728880);
            vertex(vertexConsumer, pose, -PIXEL - 0.0001f + bodyDirectionF.x(), PIXEL * 5f + player.getEyeHeight(), -PIXEL + bodyDirectionF.z(), PIXEL * 8, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, -PIXEL - 0.0001f + bodyDirectionF.x(), PIXEL * 5f + player.getEyeHeight(), +PIXEL + bodyDirectionF.z(), PIXEL * 12, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, -PIXEL - 0.0001f + bodyDirectionF.x(), PIXEL * 4.5f + player.getEyeHeight(), +PIXEL + bodyDirectionF.z(), PIXEL * 12, PIXEL * 4, 15728880);

            vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), PIXEL * 0.5f + length, -PIXEL - 0.0001f + bodyDirectionF.z(), PIXEL * 8, PIXEL * 4, 15728880);
            vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), PIXEL + length, -PIXEL - 0.0001f + bodyDirectionF.z(), PIXEL * 8, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), PIXEL + length, -PIXEL - 0.0001f + bodyDirectionF.z(), PIXEL * 12, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), PIXEL * 0.5f + length, -PIXEL - 0.0001f + bodyDirectionF.z(), PIXEL * 12, PIXEL * 4, 15728880);

            vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), PIXEL * 0.5f + length, +PIXEL + 0.0001f + bodyDirectionF.z(), PIXEL * 8, PIXEL * 4, 15728880);
            vertex(vertexConsumer, pose, +PIXEL + bodyDirectionF.x(), PIXEL + length, +PIXEL + 0.0001f + bodyDirectionF.z(), PIXEL * 8, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), PIXEL + length, +PIXEL + 0.0001f + bodyDirectionF.z(), PIXEL * 12, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, -PIXEL + bodyDirectionF.x(), PIXEL * 0.5f + length, +PIXEL + 0.0001f + bodyDirectionF.z(), PIXEL * 12, PIXEL * 4, 15728880);

            vertex(vertexConsumer, pose, +PIXEL + 0.0001f + bodyDirectionF.x(), PIXEL * 0.5f + length, +PIXEL + bodyDirectionF.z(), PIXEL * 8, PIXEL * 4, 15728880);
            vertex(vertexConsumer, pose, +PIXEL + 0.0001f + bodyDirectionF.x(), PIXEL + length, +PIXEL + bodyDirectionF.z(), PIXEL * 8, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, +PIXEL + 0.0001f + bodyDirectionF.x(), PIXEL + length, -PIXEL + bodyDirectionF.z(), PIXEL * 12, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, +PIXEL + 0.0001f + bodyDirectionF.x(), PIXEL * 0.5f + length, -PIXEL + bodyDirectionF.z(), PIXEL * 12, PIXEL * 4, 15728880);

            vertex(vertexConsumer, pose, -PIXEL - 0.0001f + bodyDirectionF.x(), PIXEL * 0.5f + length, -PIXEL + bodyDirectionF.z(), PIXEL * 8, PIXEL * 4, 15728880);
            vertex(vertexConsumer, pose, -PIXEL - 0.0001f + bodyDirectionF.x(), PIXEL + length, -PIXEL + bodyDirectionF.z(), PIXEL * 8, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, -PIXEL - 0.0001f + bodyDirectionF.x(), PIXEL + length, +PIXEL + bodyDirectionF.z(), PIXEL * 12, PIXEL * 5, 15728880);
            vertex(vertexConsumer, pose, -PIXEL - 0.0001f + bodyDirectionF.x(), PIXEL * 0.5f + length, +PIXEL + bodyDirectionF.z(), PIXEL * 12, PIXEL * 4, 15728880);
        }
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, float x, float y, float z, float i, float j, int light) {
        vertexConsumer.addVertex(pose, x, y, z).setColor(-1).setUv(i, j).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0F, 1.0F, 0.0F);
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
                player.addLayer(new KwamiOnShoulderLayer<>(player));
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
            if (event.getKey() == GLFW.GLFW_KEY_SPACE && MineraculousClientUtils.hasNoScreenOpen() && revokeButton != null && revokeButton.active) {
                revokeButton.onPress();
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

    public static void onPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
        MineraculousClientUtils.refreshVip();
    }

    public static void onClientConfigChanged(ModConfigEvent event) {
        if (event.getConfig().getType() == ModConfig.Type.CLIENT && Minecraft.getInstance().player != null) {
            MineraculousClientUtils.refreshVip();
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
            event.insertAfter(MineraculousBlocks.CAMEMBERT_BLOCKS.get(CheeseBlock.Age.TIME_HONORED).toStack(), MineraculousItems.WAXED_CHEESE_WEDGES.get(CheeseBlock.Age.FRESH).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.WAXED_CHEESE_WEDGES.get(CheeseBlock.Age.FRESH).toStack(), MineraculousBlocks.WAXED_CHEESE_BLOCKS.get(CheeseBlock.Age.FRESH).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.WAXED_CHEESE_BLOCKS.get(CheeseBlock.Age.FRESH).toStack(), MineraculousItems.WAXED_CAMEMBERT_WEDGES.get(CheeseBlock.Age.FRESH).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.WAXED_CAMEMBERT_WEDGES.get(CheeseBlock.Age.FRESH).toStack(), MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS.get(CheeseBlock.Age.FRESH).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS.get(CheeseBlock.Age.FRESH).toStack(), MineraculousItems.WAXED_CHEESE_WEDGES.get(CheeseBlock.Age.AGED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.WAXED_CHEESE_WEDGES.get(CheeseBlock.Age.AGED).toStack(), MineraculousBlocks.WAXED_CHEESE_BLOCKS.get(CheeseBlock.Age.AGED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.WAXED_CHEESE_BLOCKS.get(CheeseBlock.Age.AGED).toStack(), MineraculousItems.WAXED_CAMEMBERT_WEDGES.get(CheeseBlock.Age.AGED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.WAXED_CAMEMBERT_WEDGES.get(CheeseBlock.Age.AGED).toStack(), MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS.get(CheeseBlock.Age.AGED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS.get(CheeseBlock.Age.AGED).toStack(), MineraculousItems.WAXED_CHEESE_WEDGES.get(CheeseBlock.Age.RIPENED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.WAXED_CHEESE_WEDGES.get(CheeseBlock.Age.RIPENED).toStack(), MineraculousBlocks.WAXED_CHEESE_BLOCKS.get(CheeseBlock.Age.RIPENED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.WAXED_CHEESE_BLOCKS.get(CheeseBlock.Age.RIPENED).toStack(), MineraculousItems.WAXED_CAMEMBERT_WEDGES.get(CheeseBlock.Age.RIPENED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.WAXED_CAMEMBERT_WEDGES.get(CheeseBlock.Age.RIPENED).toStack(), MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS.get(CheeseBlock.Age.RIPENED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS.get(CheeseBlock.Age.RIPENED).toStack(), MineraculousItems.WAXED_CHEESE_WEDGES.get(CheeseBlock.Age.EXQUISITE).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.WAXED_CHEESE_WEDGES.get(CheeseBlock.Age.EXQUISITE).toStack(), MineraculousBlocks.WAXED_CHEESE_BLOCKS.get(CheeseBlock.Age.EXQUISITE).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.WAXED_CHEESE_BLOCKS.get(CheeseBlock.Age.EXQUISITE).toStack(), MineraculousItems.WAXED_CAMEMBERT_WEDGES.get(CheeseBlock.Age.EXQUISITE).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.WAXED_CAMEMBERT_WEDGES.get(CheeseBlock.Age.EXQUISITE).toStack(), MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS.get(CheeseBlock.Age.EXQUISITE).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS.get(CheeseBlock.Age.EXQUISITE).toStack(), MineraculousItems.WAXED_CHEESE_WEDGES.get(CheeseBlock.Age.TIME_HONORED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.WAXED_CHEESE_WEDGES.get(CheeseBlock.Age.TIME_HONORED).toStack(), MineraculousBlocks.WAXED_CHEESE_BLOCKS.get(CheeseBlock.Age.TIME_HONORED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousBlocks.WAXED_CHEESE_BLOCKS.get(CheeseBlock.Age.TIME_HONORED).toStack(), MineraculousItems.WAXED_CAMEMBERT_WEDGES.get(CheeseBlock.Age.TIME_HONORED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(MineraculousItems.WAXED_CAMEMBERT_WEDGES.get(CheeseBlock.Age.TIME_HONORED).toStack(), MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS.get(CheeseBlock.Age.TIME_HONORED).toStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
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

    public static FlattenedKamikotizationLookData flattenKamikotizationLook(ResourceKey<Kamikotization> kamikotization) {
        Path folder = Minecraft.getInstance().gameDirectory.toPath().resolve("miraculouslooks").resolve("kamikotizations");
        if (!Files.exists(folder)) {
            return null;
        }
        String namespace = kamikotization.location().getNamespace();
        Path nameFolder = folder.resolve(namespace);
        if (!Files.exists(nameFolder)) {
            return null;
        }
        String type = kamikotization.location().getPath();
        Path texture = nameFolder.resolve(type + ".png");
        if (Files.exists(texture)) {
            try {
                Path model = texture.resolveSibling(type + ".geo.json");
                String convertedModel = null;
                if (Files.exists(model)) {
                    convertedModel = Files.readString(model);
                }
                byte[] convertedImage = NativeImage.read(texture.toUri().toURL().openStream()).asByteArray();
                Path glowmask = texture.resolveSibling(type + "_glowmask.png");
                byte[] convertedGlowmask = null;
                if (Files.exists(glowmask)) {
                    convertedGlowmask = NativeImage.read(glowmask.toUri().toURL().openStream()).asByteArray();
                }
                Path animations = texture.resolveSibling(type + ".animation.json");
                String convertedAnimations = null;
                if (Files.exists(animations)) {
                    convertedAnimations = Files.readString(animations);
                }
                return new FlattenedKamikotizationLookData(kamikotization, Optional.ofNullable(convertedModel), convertedImage, Optional.ofNullable(convertedGlowmask), Optional.ofNullable(convertedAnimations));
            } catch (Exception exception) {
                Mineraculous.LOGGER.error("Failed to handle clientbound request sync kamikotization look payload", exception);
            }
        }
        return null;
    }

    public static void onInteractionKeyMappingTriggered(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack() && event.getHand() == InteractionHand.MAIN_HAND && ClientUtils.getMainClientPlayer().getOffhandItem().is(MineraculousItems.LADYBUG_YOYO)) {
            TommyLibServices.NETWORK.sendToServer(ServerboundSendOffhandSwingPayload.INSTANCE);
            if (ClientUtils.getMainClientPlayer().getOffhandItem().onEntitySwing(ClientUtils.getMainClientPlayer(), InteractionHand.OFF_HAND)) {
                event.setCanceled(true);
            }
        }
    }

    public static void onRegisterRenderBuffers(RegisterRenderBuffersEvent event) {
        event.registerRenderBuffer(MineraculousRenderTypes.luckyCharm());
        event.registerRenderBuffer(MineraculousRenderTypes.armorLuckyCharm());
        event.registerRenderBuffer(MineraculousRenderTypes.entityLuckyCharm());
        event.registerRenderBuffer(MineraculousRenderTypes.shieldLuckyCharm());
    }

    public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) resourceManager -> {
            MineraculousClientUtils.refreshVip();
            MiraculousArmorItem.clearAnimationData();
            KamikotizationArmorItem.clearAnimationData();
        });
    }
}
