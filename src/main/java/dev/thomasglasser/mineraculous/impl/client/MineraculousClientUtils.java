package dev.thomasglasser.mineraculous.impl.client;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.Axis;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.gui.screens.RadialMenuScreen;
import dev.thomasglasser.mineraculous.api.client.gui.screens.RegistryElementSelectionScreen;
import dev.thomasglasser.mineraculous.api.client.gui.screens.inventory.ExternalCuriosInventoryScreen;
import dev.thomasglasser.mineraculous.api.client.gui.screens.inventory.InventorySyncListener;
import dev.thomasglasser.mineraculous.api.client.gui.screens.look.LookCustomizationScreen;
import dev.thomasglasser.mineraculous.api.client.renderer.MineraculousRenderTypes;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContextSets;
import dev.thomasglasser.mineraculous.api.core.look.metadata.LookMetadataTypes;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosData;
import dev.thomasglasser.mineraculous.api.world.item.RadialMenuProvider;
import dev.thomasglasser.mineraculous.api.world.kamikotization.KamikotizationData;
import dev.thomasglasser.mineraculous.api.world.level.storage.abilityeffects.AbilityEffectUtils;
import dev.thomasglasser.mineraculous.impl.client.gui.MineraculousGuis;
import dev.thomasglasser.mineraculous.impl.client.gui.screens.MiraculousTransferScreen;
import dev.thomasglasser.mineraculous.impl.client.gui.screens.kamikotization.AbstractKamikotizationChatScreen;
import dev.thomasglasser.mineraculous.impl.client.gui.screens.kamikotization.KamikotizationItemSelectionScreen;
import dev.thomasglasser.mineraculous.impl.client.gui.screens.kamikotization.PerformerKamikotizationChatScreen;
import dev.thomasglasser.mineraculous.impl.client.gui.screens.kamikotization.ReceiverKamikotizationChatScreen;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers.BetaTesterCosmeticOptions;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers.SpecialPlayerData;
import dev.thomasglasser.mineraculous.impl.client.renderer.item.CatStaffRenderer;
import dev.thomasglasser.mineraculous.impl.network.ServerboundRevertConvertedEntityPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundSetInventoryTrackedPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundSetMiraculousLookDataPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundSetSpectationInterruptedPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundStartKamikotizationDetransformationPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundStealCurioPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundStealItemPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundUpdateSpecialPlayerDataPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundUpdateYoyoInputPayload;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.impl.world.entity.KamikotizedMinion;
import dev.thomasglasser.mineraculous.impl.world.item.ability.CatStaffPerchGroundWorker;
import dev.thomasglasser.mineraculous.impl.world.item.component.KamikoData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.SlotInfo;
import dev.thomasglasser.mineraculous.impl.world.level.storage.newPerchingCatStaffData;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.entity.player.SpecialPlayerUtils;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import top.theillusivec4.curios.common.inventory.CurioSlot;

public class MineraculousClientUtils {
    public static final Component GUI_CHOOSE = Component.translatable("gui.choose");
    public static final Component GUI_NAME = Component.translatable("gui.name");
    public static final Component STEALING_WARNING = Component.translatable("mineraculous.stealing_warning");
    public static final Component MIRACULOUS_LOOKS_BUTTON_TOOLTIP = Component.translatable("gui.mineraculous.miraculous_looks.tooltip");

    private static final WidgetSprites MIRACULOUS_LOOKS_BUTTON_SPRITES = new WidgetSprites(
            MineraculousConstants.modLoc("miraculous_looks/button"),
            MineraculousConstants.modLoc("miraculous_looks/button_highlighted"));

    private static final Map<UUID, SpecialPlayerData> SPECIAL_PLAYER_DATA = new Object2ReferenceOpenHashMap<>();
    private static final IntList CATACLYSM_PIXELS = new IntArrayList();
    private static final ResourceLocation KWAMI_GLOW_SHADER = MineraculousConstants.modLoc("shaders/post/kwami_glow.json");
    private static final String KWAMI_GLOW_SHADER_TARGET = "kwami";
    private static final String KWAMI_GLOW_SHADER_STRENGTH_UNIFORM = "BlurSigma";

    private static boolean wasJumping = false;

    private static PostChain kwamiEffect;
    private static RenderTarget kwamiTarget;

    public static PostChain getKwamiEffect() {
        return kwamiEffect;
    }

    public static void setKwamiEffect(PostChain postChain) {
        kwamiEffect = postChain;
    }

    public static RenderTarget getKwamiTarget() {
        return kwamiTarget;
    }

    public static void setKwamiTarget(RenderTarget renderTarget) {
        kwamiTarget = renderTarget;
    }

    public static void initKwami() {
        if (getKwamiEffect() != null) {
            getKwamiEffect().close();
        }
        try {
            setKwamiEffect(new PostChain(
                    Minecraft.getInstance().getTextureManager(),
                    Minecraft.getInstance().getResourceManager(),
                    Minecraft.getInstance().getMainRenderTarget(),
                    KWAMI_GLOW_SHADER));
            getKwamiEffect().resize(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight());
            setKwamiTarget(getKwamiEffect().getTempTarget(KWAMI_GLOW_SHADER_TARGET));
        } catch (IOException | JsonSyntaxException e) {
            MineraculousConstants.LOGGER.warn("Failed to load or parse shader: {}", KWAMI_GLOW_SHADER, e);
            setKwamiEffect(null);
            setKwamiTarget(null);
        }
    }

    public static void updateKwamiGlowUniforms(FloatArrayList values) {
        float kwamiGlowPower = 0.0f;
        for (Float value : values) {
            kwamiGlowPower = Math.max(kwamiGlowPower, value);
        }
        if (getKwamiEffect() != null) {
            getKwamiEffect().setUniform(KWAMI_GLOW_SHADER_STRENGTH_UNIFORM, kwamiGlowPower);
        }
    }

    public static boolean shouldShowKwamiGlow() {
        return !Minecraft.getInstance().gameRenderer.isPanoramicMode() && getKwamiTarget() != null && getKwamiEffect() != null && Minecraft.getInstance().player != null;
    }

    public static void blitKwamiGlow() {
        if (shouldShowKwamiGlow()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ZERO,
                    GlStateManager.DestFactor.ONE);
            getKwamiTarget().blitToScreen(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), false);
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }
    }

    // Special Player Handling
    public static void setSpecialPlayerData(UUID id, SpecialPlayerData data) {
        SPECIAL_PLAYER_DATA.put(id, data);
    }

    public static boolean renderBetaTesterLayer(AbstractClientPlayer player) {
        if (player != ClientUtils.getLocalPlayer() && !MineraculousClientConfig.get().displayOthersBetaTesterCosmetic.getAsBoolean())
            return false;
        SpecialPlayerData data = SPECIAL_PLAYER_DATA.get(player.getUUID());
        return data != null && data.displayBeta() && SpecialPlayerUtils.renderCosmeticLayerInSlot(player, betaChoice(player.getUUID()).slot());
    }

    public static BetaTesterCosmeticOptions betaChoice(UUID id) {
        SpecialPlayerData data = SPECIAL_PLAYER_DATA.get(id);
        return data != null ? data.choice() : BetaTesterCosmeticOptions.DERBY_HAT;
    }

    public static boolean renderDevLayer(AbstractClientPlayer player) {
        if (player != ClientUtils.getLocalPlayer() && !MineraculousClientConfig.get().displayOthersDevTeamCosmetic.getAsBoolean())
            return false;
        SpecialPlayerData data = SPECIAL_PLAYER_DATA.get(player.getUUID());
        return data != null && data.displayDev() && SpecialPlayerUtils.renderCosmeticLayerInSlot(player, EquipmentSlot.HEAD);
    }

    public static boolean renderLegacyDevLayer(AbstractClientPlayer player) {
        if (player != ClientUtils.getLocalPlayer() && !MineraculousClientConfig.get().displayOthersLegacyDevTeamCosmetic.getAsBoolean())
            return false;
        SpecialPlayerData data = SPECIAL_PLAYER_DATA.get(player.getUUID());
        return data != null && data.displayLegacyDev() && SpecialPlayerUtils.renderCosmeticLayerInSlot(player, EquipmentSlot.HEAD);
    }

    public static void syncSpecialPlayerChoices() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            TommyLibServices.NETWORK.sendToServer(new ServerboundUpdateSpecialPlayerDataPayload(player.getUUID(), new SpecialPlayerData(
                    MineraculousClientConfig.get().betaTesterCosmeticChoice.get(),
                    MineraculousClientConfig.get().displaySelfBetaTesterCosmetic.get(),
                    MineraculousClientConfig.get().displaySelfDevTeamCosmetic.get(),
                    MineraculousClientConfig.get().displaySelfLegacyDevTeamCosmetic.get())));
        }
    }

    // Screens
    public static boolean hasNoScreenOpen() {
        return Minecraft.getInstance().screen == null;
    }

    public static void renderEntityInInventory(
            GuiGraphics guiGraphics,
            int xStart,
            int yStart,
            int xEnd,
            int yEnd,
            int scale,
            float horizontalRotation,
            float verticalRotation,
            LivingEntity entity) {
        guiGraphics.enableScissor(xStart, yStart, xEnd, yEnd);
        EntityInInventoryRotations originalRotation = new EntityInInventoryRotations(
                entity.yBodyRot,
                entity.getYRot(),
                entity.getXRot(),
                entity.yHeadRotO,
                entity.yHeadRot);
        EntityInInventoryRotations newRotation = new EntityInInventoryRotations(
                180.0F + horizontalRotation * 2,
                180.0F + horizontalRotation * 2,
                0,
                180.0F + horizontalRotation * 2,
                180.0F + horizontalRotation * 2);

        float x = (float) (xStart + xEnd) / 2.0F;
        float y = (float) (yStart + yEnd) / 2.0F;
        float renderScale = (float) scale / entity.getScale();
        Vec3 translation = translatedEntityInInventory(entity);
        EntityInInventoryQuaternions rotation = getRotations(verticalRotation);
        setEntityInInventoryRotation(newRotation, entity);
        renderEntityInInventory(guiGraphics, x, y, renderScale, translation.toVector3f(), translation.scale(-1).toVector3f(), rotation.horizontal(), rotation.vertical(), entity);
        setEntityInInventoryRotation(originalRotation, entity);
        guiGraphics.disableScissor();
    }

    /**
     * Translates to the half of the height.
     */
    public static Vec3 translatedEntityInInventory(LivingEntity entity) {
        float entityScale = entity.getScale();
        float halfHeight = -(entity.getBbHeight() / 2.0F + 0.0625F * entityScale);
        return new Vec3(0, halfHeight, 0);
    }

    private static EntityInInventoryQuaternions getRotations(float verticalRotation) {
        Quaternionf flipRot = new Quaternionf().rotateZ(Mth.PI);
        Quaternionf forwardTilt = new Quaternionf().rotateX(verticalRotation * (Mth.PI / 180f));
        flipRot.mul(forwardTilt);
        return new EntityInInventoryQuaternions(flipRot, forwardTilt);
    }

    private static void setEntityInInventoryRotation(EntityInInventoryRotations rotation, LivingEntity entity) {
        entity.yBodyRot = rotation.yBodyRot;
        entity.setYRot(rotation.yRot);
        entity.setXRot(rotation.xRot);
        entity.yHeadRotO = rotation.yHeadRotO;
        entity.yHeadRot = rotation.yHeadRot;
    }

    private static void renderEntityInInventory(
            GuiGraphics guiGraphics,
            float x,
            float y,
            float scale,
            Vector3f translate,
            Vector3f pivot,
            Quaternionf pose,
            Quaternionf cameraOrientation,
            LivingEntity entity) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 50.0);
        guiGraphics.pose().scale(scale, scale, -scale);
        guiGraphics.pose().translate(translate.x, translate.y, translate.z);
        guiGraphics.pose().rotateAround(pose, pivot.x, pivot.y, pivot.z);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (cameraOrientation != null) {
            entityrenderdispatcher.overrideCameraOrientation(cameraOrientation.conjugate(new Quaternionf()).rotateY((float) Math.PI));
        }

        entityrenderdispatcher.setRenderShadow(false);
        RenderSystem.runAsFancy(() -> entityrenderdispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, guiGraphics.pose(), guiGraphics.bufferSource(), 15728880));
        guiGraphics.flush();
        entityrenderdispatcher.setRenderShadow(true);
        guiGraphics.pose().popPose();
        Lighting.setupFor3DItems();
    }

    public static boolean tryOpenRadialMenuScreenFromProvider(InteractionHand hand, ItemStack stack, RadialMenuProvider<?> provider) {
        if (provider.canOpenMenu(stack, hand, ClientUtils.getLocalPlayer())) {
            Minecraft.getInstance().setScreen(new RadialMenuScreen<>(hand, MineraculousKeyMappings.OPEN_ITEM_RADIAL_MENU.getKey().getValue(), stack, provider));
            return true;
        }
        return false;
    }

    public static void openExternalCuriosInventoryScreenForStealing(Player target) {
        TommyLibServices.NETWORK.sendToServer(new ServerboundSetInventoryTrackedPayload(target.getUUID(), true));
        Minecraft.getInstance().setScreen(new ExternalCuriosInventoryScreen(target, true) {
            @Override
            public void pickUp(Slot slot, Player target, AbstractContainerMenu menu) {
                if (slot instanceof CurioSlot curioSlot)
                    TommyLibServices.NETWORK.sendToServer(new ServerboundStealCurioPayload(target.getUUID(), new CuriosData(curioSlot.getSlotContext())));
                else
                    TommyLibServices.NETWORK.sendToServer(new ServerboundStealItemPayload(target.getUUID(), menu.slots.indexOf(slot)));
            }

            @Override
            public void onClose(boolean cancel) {
                TommyLibServices.NETWORK.sendToServer(new ServerboundSetInventoryTrackedPayload(target.getUUID(), false));
            }
        });
    }

    public static void remoteCloseKamikotizationChatScreen(boolean cancel) {
        if (Minecraft.getInstance().screen instanceof AbstractKamikotizationChatScreen screen)
            screen.onClose(cancel, false);
    }

    public static void beginKamikotizationSelection(Player target, KamikoData kamikoData) {
        Minecraft.getInstance().setScreen(new KamikotizationItemSelectionScreen(target, kamikoData));
    }

    public static void openReceiverKamikotizationChatScreen(UUID other, KamikotizationData kamikotizationData, SlotInfo slotInfo) {
        Minecraft.getInstance().setScreen(new ReceiverKamikotizationChatScreen(other, kamikotizationData, slotInfo));
    }

    public static void openPerformerKamikotizationChatScreen(String performerName, String targetName, Optional<ResourceLocation> faceMaskTexture, Player target) {
        Minecraft.getInstance().setScreen(new PerformerKamikotizationChatScreen(performerName, targetName, faceMaskTexture, target));
    }

    public static void openMiraculousTransferScreen(int kwamiId) {
        Minecraft.getInstance().setScreen(new MiraculousTransferScreen(kwamiId));
    }

    public static void triggerInventorySyncListener(Player player) {
        if (Minecraft.getInstance().screen instanceof InventorySyncListener tracker) {
            tracker.onInventorySynced(player);
        }
    }

    public static ImageButton createMiraculousLooksButton(Screen parent, GridLayout gridLayout) {
        ImageButton button = new ImageButton(gridLayout.getX() - 15, gridLayout.getY() + (gridLayout.getHeight() / 2) + 15, 14, 14, MIRACULOUS_LOOKS_BUTTON_SPRITES, b -> parent.getMinecraft().setScreen(new RegistryElementSelectionScreen<>(parent, MineraculousRegistries.MIRACULOUS, selected -> parent.getMinecraft().setScreen(new LookCustomizationScreen<>(
                LookContextSets.MIRACULOUS,
                LookMetadataTypes.VALID_MIRACULOUSES,
                selected,
                player -> player.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(selected).lookData(),
                (player, lookData) -> player.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(selected).withLookData(lookData).save(selected, player),
                (player, lookData) -> TommyLibServices.NETWORK.sendToServer(new ServerboundSetMiraculousLookDataPayload(selected, lookData)))))));
        button.setTooltip(Tooltip.create(MIRACULOUS_LOOKS_BUTTON_TOOLTIP));
        return button;
    }

    public static void revokeCameraEntity() {
        Entity cameraEntity = MineraculousClientUtils.getCameraEntity();
        Player player = ClientUtils.getLocalPlayer();
        if (cameraEntity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent()) {
            KamikotizationData kamikotizationData = cameraEntity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).get();
            TommyLibServices.NETWORK.sendToServer(new ServerboundStartKamikotizationDetransformationPayload(Optional.of(cameraEntity.getUUID()), true, false));
            AbilityEffectUtils.removeFaceMaskTexture(cameraEntity, kamikotizationData.kamikoData().faceMaskTexture());
        } else if (player != null) {
            TommyLibServices.NETWORK.sendToServer(new ServerboundRevertConvertedEntityPayload(cameraEntity.getUUID()));
        }
        TommyLibServices.NETWORK.sendToServer(new ServerboundSetSpectationInterruptedPayload(Optional.empty()));
    }

    // Camera
    public static boolean isFirstPerson() {
        return Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }

    @Nullable
    public static Entity getLookEntity() {
        return Minecraft.getInstance().crosshairPickEntity;
    }

    public static void setCameraEntity(@Nullable Entity entity) {
        Minecraft.getInstance().setCameraEntity(entity);
    }

    public static Entity getCameraEntity() {
        return Minecraft.getInstance().cameraEntity;
    }

    public static boolean isInKamikoView() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && !player.isSpectator() && player.getData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS).spectatingId().isPresent() && MineraculousClientUtils.getCameraEntity() instanceof Kamiko && MineraculousGuis.getKamikoGui() != null;
    }

    // Rendering
    public static VertexConsumer checkItemShaders(VertexConsumer buffer, MultiBufferSource bufferSource, ItemStack stack, boolean armor, boolean entity) {
        if (stack.has(MineraculousDataComponents.KAMIKOTIZING)) {
            if (entity) {
                return stack.is(Items.SHIELD)
                        ? VertexMultiConsumer.create(bufferSource.getBuffer(MineraculousRenderTypes.shieldKamikotizing()), buffer)
                        : VertexMultiConsumer.create(bufferSource.getBuffer(MineraculousRenderTypes.entityKamikotizing()), buffer);
            }
            return VertexMultiConsumer.create(bufferSource.getBuffer(armor ? MineraculousRenderTypes.armorKamikotizing() : MineraculousRenderTypes.itemKamikotizing()), buffer);
        } else if (stack.has(MineraculousDataComponents.LUCKY_CHARM) && !stack.is(MineraculousItemTags.LUCKY_CHARM_SHADER_IMMUNE)) {
            if (entity) {
                return stack.is(Items.SHIELD)
                        ? VertexMultiConsumer.create(bufferSource.getBuffer(MineraculousRenderTypes.shieldLuckyCharm()), buffer)
                        : VertexMultiConsumer.create(bufferSource.getBuffer(MineraculousRenderTypes.entityLuckyCharm()), buffer);
            }
            return VertexMultiConsumer.create(bufferSource.getBuffer(armor ? MineraculousRenderTypes.armorLuckyCharm() : MineraculousRenderTypes.itemLuckyCharm()), buffer);
        }
        return buffer;
    }

    public static @Nullable RenderType checkArmorShaders(ItemStack stack) {
        if (stack.has(MineraculousDataComponents.KAMIKOTIZING))
            return MineraculousRenderTypes.armorKamikotizing();
        else if (stack.has(MineraculousDataComponents.LUCKY_CHARM) && !stack.is(MineraculousItemTags.LUCKY_CHARM_SHADER_IMMUNE))
            return MineraculousRenderTypes.armorLuckyCharm();
        return null;
    }

    public static boolean shouldNotRenderCape(LivingEntity entity) {
        if (entity instanceof KamikotizedMinion)
            return true;
        return entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed() || entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent();
    }

    public static int getCataclysmPixel(RandomSource random) {
        return CATACLYSM_PIXELS.getInt(random.nextInt(CATACLYSM_PIXELS.size()));
    }

    public static void refreshCataclysmPixels() {
        CATACLYSM_PIXELS.clear();
        try (AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(MineraculousConstants.modLoc("textures/block/cataclysm_block.png"))) {
            NativeImage image;
            if (texture instanceof SimpleTexture simpleTexture) {
                image = simpleTexture.getTextureImage(Minecraft.getInstance().getResourceManager()).getImage();
            } else if (texture instanceof DynamicTexture dynamicTexture) {
                image = dynamicTexture.getPixels();
            } else {
                throw new IllegalStateException("Invalid cataclysm texture");
            }
            if (image != null) {
                int width = image.getWidth();
                int height = image.getHeight();
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        int pixelRGBA = image.getPixelRGBA(x, y);
                        if (pixelRGBA != 0x00000000)
                            CATACLYSM_PIXELS.add(pixelRGBA);
                    }
                }
                if (CATACLYSM_PIXELS.isEmpty()) {
                    throw new IllegalStateException("Cataclysm texture cannot be empty");
                }
            } else {
                throw new IllegalStateException("Invalid cataclysm texture");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load cataclysm texture", e);
        }
    }

    public static @Nullable NativeImage getNativeImage(HttpTexture texture) {
        AtomicReference<NativeImage> image = new AtomicReference<>();
        if (texture.file != null && texture.file.isFile()) {
            MineraculousConstants.LOGGER.debug("Loading http texture from local cache ({})", texture.file);
            try {
                FileInputStream fileinputstream = new FileInputStream(texture.file);
                image.set(texture.load(fileinputstream));
            } catch (FileNotFoundException e) {
                MineraculousConstants.LOGGER.error("Couldn't load http texture from local cache", e);
            }
        }

        if (image.get() == null) {
            HttpURLConnection httpurlconnection = null;
            MineraculousConstants.LOGGER.debug("Downloading http texture from {} to {}", texture.urlString, texture.file);

            try {
                httpurlconnection = (HttpURLConnection) new URI(texture.urlString).toURL().openConnection(Minecraft.getInstance().getProxy());
                httpurlconnection.setDoInput(true);
                httpurlconnection.setDoOutput(false);
                httpurlconnection.connect();
                if (httpurlconnection.getResponseCode() / 100 == 2) {
                    InputStream inputstream;
                    if (texture.file != null) {
                        FileUtils.copyInputStreamToFile(httpurlconnection.getInputStream(), texture.file);
                        inputstream = new FileInputStream(texture.file);
                    } else {
                        inputstream = httpurlconnection.getInputStream();
                    }

                    Minecraft.getInstance().execute(() -> {
                        NativeImage image1 = texture.load(inputstream);
                        if (image1 != null) {
                            image.set(image1);
                        }
                        try {
                            inputstream.close();
                        } catch (IOException e) {
                            MineraculousConstants.LOGGER.error("Couldn't close http texture input stream", e);
                        }
                    });
                }
            } catch (Exception exception) {
                MineraculousConstants.LOGGER.error("Couldn't download http texture", exception);
            } finally {
                if (httpurlconnection != null) {
                    httpurlconnection.disconnect();
                }
            }
        }
        return image.get();
    }

    // Misc
    public static void setShader(@Nullable ResourceLocation location) {
        PostChain current = Minecraft.getInstance().gameRenderer.postEffect;
        if (location != null)
            Minecraft.getInstance().gameRenderer.loadEffect(location);
        else if (current != null)
            Minecraft.getInstance().gameRenderer.postEffect = null;
    }

    @SuppressWarnings("ConstantValue")
    public static Vec3 getFirstPersonHandPositionNearPlane(boolean offHand, boolean swing, float rightScale, float upScale) {
        Camera camera = Minecraft.getInstance().getEntityRenderDispatcher().camera;
        float partialTicks = camera == null ? 0 : camera.getPartialTickTime();
        return getFirstPersonHandPositionNearPlane(offHand, swing, partialTicks, rightScale, upScale);
    }

    @SuppressWarnings("ConstantValue")
    public static Vec3 getFirstPersonHandPositionNearPlane(boolean offHand, boolean swing, float partialTick, float rightScale, float upScale) { //meant to be used only when the local player is in 1st POV
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            float attackAnim = player.getAttackAnim(partialTick);
            float swingAngle = swing ? Mth.sin(Mth.sqrt(attackAnim) * Mth.PI) : 0;
            int armMultiplier = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
            if (offHand) {
                armMultiplier = -armMultiplier;
            }

            EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            if (entityRenderDispatcher.camera != null) {
                double fovScale = 960.0 / (double) entityRenderDispatcher.options.fov().get();
                Vec3 handOffset = entityRenderDispatcher.camera.getNearPlane().getPointOnPlane((float) armMultiplier * rightScale, upScale).scale(fovScale).yRot(swingAngle * 0.5F).xRot(-swingAngle * 0.7F);
                return player.getEyePosition(partialTick).add(handOffset);
            }
            return Vec3.ZERO;
        }
        return Vec3.ZERO;
    }

    @SuppressWarnings("ConstantValue")
    public static Vec3 getFirstPersonHandPosition(boolean offHand) {
        Camera camera = Minecraft.getInstance().getEntityRenderDispatcher().camera;
        float partialTicks = camera == null ? 0 : camera.getPartialTickTime();
        return getFirstPersonHandPosition(offHand, partialTicks);
    }

    @SuppressWarnings("ConstantValue")
    public static Vec3 getFirstPersonHandPosition(boolean offHand, float partialTick) { //meant to be used only when the local player is in 1st POV
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            float attackAnim = player.getAttackAnim(partialTick);
            float swingAngle = Mth.sin(Mth.sqrt(attackAnim) * Mth.PI);
            int armMultiplier = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
            if (offHand) {
                armMultiplier = -armMultiplier;
            }

            EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            if (entityRenderDispatcher.camera != null) {
                double fovScale = entityRenderDispatcher.options.fov().get() / 110.0d;
                Vec3 hand_offset = new Vec3(
                        (double) armMultiplier * -0.45d * fovScale,
                        -0.23d * fovScale,
                        fovScale <= 0.7
                                ? 0.55 - 0.50625 * (fovScale - 0.3) - 0.03125 * Math.pow(fovScale - 0.3, 3)
                                : 0.38 - 0.1375 * (fovScale - 0.7) - 0.05625 * Math.pow(fovScale - 0.7, 3));
                double pitch = -swingAngle * 0.7f;
                double yaw = -swingAngle * 0.5f;

                // apply swing
                hand_offset = MineraculousMathUtils.rotatePitch(hand_offset, pitch);
                hand_offset = MineraculousMathUtils.rotateYaw(hand_offset, yaw);

                // apply looking direction
                hand_offset = MineraculousMathUtils.rotatePitch(hand_offset, -Mth.lerp(partialTick, player.xRotO, player.getXRot()) * (Mth.PI / 180F));
                hand_offset = MineraculousMathUtils.rotateYaw(hand_offset, Mth.lerp(partialTick, player.yRotO, player.getYRot()) * (Mth.PI / 180F));

                return hand_offset.add(entityRenderDispatcher.camera.getPosition());
            }
            return Vec3.ZERO;
        }
        return Vec3.ZERO;
    }

    @SuppressWarnings("ConstantValue")
    public static Vec3 getHumanoidEntityHandPos(Entity entity, boolean offHand, double frontOffset, double heightOffset, double sideOffset) {
        Camera camera = Minecraft.getInstance().getEntityRenderDispatcher().camera;
        float partialTicks = camera == null ? 0 : camera.getPartialTickTime();
        return getHumanoidEntityHandPos(entity, offHand, partialTicks, frontOffset, heightOffset, sideOffset);
    }

    public static Vec3 getHumanoidEntityHandPos(Entity entity, boolean offHand, float partialTick, double frontOffset, double heightOffset, double sideOffset) { //meant only for third person view
        if (entity instanceof LivingEntity livingEntity) {
            int armMultiplier = livingEntity.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
            if (offHand) {
                armMultiplier = -armMultiplier;
            }

            float bodyRotRad = Mth.lerp(partialTick, livingEntity.yBodyRotO, livingEntity.yBodyRot) * (Mth.PI / 180f);
            double sinRot = Mth.sin(bodyRotRad);
            double cosRot = Mth.cos(bodyRotRad);
            float scale = livingEntity.getScale();
            double armOffset = (double) armMultiplier * sideOffset * (double) scale;
            double frontOffsetScaled = frontOffset * (double) scale;
            float crouchOffset = livingEntity.isCrouching() ? -0.1875f : 0;
            return livingEntity.getEyePosition(partialTick).add(-cosRot * armOffset - sinRot * frontOffsetScaled, (double) crouchOffset + heightOffset * (double) scale, -sinRot * armOffset + cosRot * frontOffsetScaled);
        }
        return Vec3.ZERO;
    }

    public record CatStaffTickData(Vec3 prevOrigin, Vec3 currOrigin) {}

    public static Map<Integer, CatStaffTickData> catStaffPastTickExtremitiesEntityMap = new HashMap<>();

    public static void updateCatStaffMap() {
        catStaffPastTickExtremitiesEntityMap.clear();
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            Set<Integer> seenThisTick = new HashSet<>();
            for (Entity entity : level.entitiesForRendering()) {
                newPerchingCatStaffData data = entity.getData(MineraculousAttachmentTypes.newPERCHING_CAT_STAFF);
                if (!data.isModeActive()) continue;
                seenThisTick.add(entity.getId());
                catStaffPastTickExtremitiesEntityMap.compute(entity.getId(), (id, old) -> {
                    if (old == null) {
                        return new CatStaffTickData(
                                data.staffOrigin(),
                                data.staffOrigin());
                    }
                    return new CatStaffTickData(
                            old.currOrigin(),
                            data.staffOrigin());
                });
            }
            catStaffPastTickExtremitiesEntityMap.keySet().removeIf(id -> !seenThisTick.contains(id));
        }
    }

    public static void renderCatStaffsInWorldSpace(PoseStack poseStack, MultiBufferSource bufferSource, int light, float partialTick) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            for (Entity entity : level.entitiesForRendering()) {
                newPerchingCatStaffData perchingData = entity.getData(MineraculousAttachmentTypes.newPERCHING_CAT_STAFF);
                boolean renderPerch = perchingData.isModeActive();
                if (renderPerch) {
                    CatStaffTickData extremities = catStaffPastTickExtremitiesEntityMap.get(entity.getId());
                    if (extremities != null) {
                        Vec3 interpolatedOrigin = extremities.prevOrigin.lerp(extremities.currOrigin, partialTick);
                        double y = CatStaffPerchGroundWorker.expectedStaffTip(entity, partialTick).y;
                        Vec3 interpolatedTip = perchingData.withStaffTipY(y).staffTip();

                        boolean leaning = perchingData.state() == newPerchingCatStaffData.PerchingState.LEAN;
                        if (leaning) {
                            Vec3 userGroundProjected = perchingData.userPositionBeforeLeanOrRelease().multiply(1, 0, 1).add(0, perchingData.staffOrigin().y, 0);
                            Vec3 oldUserPosition = new Vec3(entity.xOld, entity.yOld, entity.zOld);
                            Vec3 userPosition = oldUserPosition.lerp(entity.position(), partialTick);
                            Quaternionf rotation = new Quaternionf().rotationTo(MineraculousMathUtils.UP.toVector3f(), userPosition.subtract(userGroundProjected).normalize().toVector3f());
                            poseStack.pushPose();
                            poseStack.rotateAround(
                                    rotation,
                                    (float) perchingData.staffOrigin().x,
                                    (float) perchingData.staffOrigin().y,
                                    (float) perchingData.staffOrigin().z);
                        }
                        CatStaffRenderer.renderStaffInWorldSpace(poseStack, bufferSource, light, interpolatedOrigin, interpolatedTip, perchingData.pawDirection());
                        if (leaning) {
                            poseStack.popPose();
                        }
                    }
                }
            }
        }
    }

    public record InputState(boolean front, boolean back, boolean left, boolean right, boolean jump) {
        public int packInputs() {
            int bits = 0;
            if (front) bits |= ServerboundUpdateYoyoInputPayload.UP;
            if (back) bits |= ServerboundUpdateYoyoInputPayload.DOWN;
            if (left) bits |= ServerboundUpdateYoyoInputPayload.LEFT;
            if (right) bits |= ServerboundUpdateYoyoInputPayload.RIGHT;
            if (jump) bits |= ServerboundUpdateYoyoInputPayload.JUMP;
            return bits;
        }

        public boolean hasInput() {
            return front || back || left || right || jump;
        }

        public boolean[] getHorizontalMovementInputs() {
            return new boolean[] { front, back, left, right };
        }
    }

    public static InputState captureInput() {
        var input = Minecraft.getInstance().player.input;
        boolean jump = input.jumping && !wasJumping;
        wasJumping = input.jumping;
        return new InputState(input.up, input.down, input.left, input.right, jump);
    }

    public static boolean isValidTexture(ResourceLocation texture) {
        return texture != null && (Minecraft.getInstance().getResourceManager().getResource(texture).isPresent() || Minecraft.getInstance().getTextureManager().getTexture(texture, MissingTextureAtlasSprite.getTexture()) != MissingTextureAtlasSprite.getTexture());
    }

    public static void rotateFacingCamera(PoseStack poseStack, Vec3 pos, double zDegrees) {
        rotateFacingCamera(poseStack, pos.toVector3f(), (float) zDegrees);
    }

    public static void rotateFacingCamera(PoseStack poseStack, Vector3f pos, float zDegrees) {
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        poseStack.pushPose();
        poseStack.rotateAround(Axis.YP.rotationDegrees(-camera.getYRot()), pos.x, pos.y, pos.z);
        poseStack.rotateAround(Axis.XP.rotationDegrees(camera.getXRot()), pos.x, pos.y, pos.z);
        poseStack.rotateAround(Axis.ZP.rotationDegrees(zDegrees), pos.x, pos.y, pos.z);
    }

    public static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int light) {
        vertexConsumer.addVertex(pose, x, y, z)
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0, 1, 0);
    }

    public static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, Vector3f pos, float u, float v, int light) {
        vertex(vertexConsumer, pose, pos.x, pos.y, pos.z, u, v, light);
    }

    public static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, Vec3 position, float u, float v, int light) {
        Vector3f pos = position.toVector3f();
        vertex(vertexConsumer, pose, pos, u, v, light);
    }

    public static void vertex(
            VertexConsumer vc,
            PoseStack.Pose pose,
            float x, float y, float z,
            float u, float v,
            int light,
            float nx, float ny, float nz) {
        vc.addVertex(pose, x, y, z)
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    private record EntityInInventoryQuaternions(Quaternionf horizontal, Quaternionf vertical) {}

    private record EntityInInventoryRotations(float yBodyRot, float yRot, float xRot, float yHeadRotO, float yHeadRot) {}
}
