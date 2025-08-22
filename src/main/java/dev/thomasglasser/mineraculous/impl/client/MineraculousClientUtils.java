package dev.thomasglasser.mineraculous.impl.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.datafixers.util.Either;
import dev.thomasglasser.mineraculous.api.client.gui.screens.RadialMenuScreen;
import dev.thomasglasser.mineraculous.api.client.gui.screens.inventory.ExternalCuriosInventoryScreen;
import dev.thomasglasser.mineraculous.api.client.renderer.MineraculousRenderTypes;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosData;
import dev.thomasglasser.mineraculous.api.world.item.RadialMenuProvider;
import dev.thomasglasser.mineraculous.api.world.kamikotization.KamikotizationData;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.client.gui.MineraculousGuis;
import dev.thomasglasser.mineraculous.impl.client.gui.screens.MiraculousTransferScreen;
import dev.thomasglasser.mineraculous.impl.client.gui.screens.kamikotization.AbstractKamikotizationChatScreen;
import dev.thomasglasser.mineraculous.impl.client.gui.screens.kamikotization.KamikotizationSelectionScreen;
import dev.thomasglasser.mineraculous.impl.client.gui.screens.kamikotization.PerformerKamikotizationChatScreen;
import dev.thomasglasser.mineraculous.impl.client.gui.screens.kamikotization.ReceiverKamikotizationChatScreen;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers.BetaTesterCosmeticOptions;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers.SpecialPlayerData;
import dev.thomasglasser.mineraculous.impl.network.ServerboundRequestInventorySyncPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundStealCurioPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundStealItemPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundUpdateSpecialPlayerDataPayload;
import dev.thomasglasser.mineraculous.impl.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.impl.world.item.component.KamikoData;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.entity.player.SpecialPlayerUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import top.theillusivec4.curios.common.inventory.CurioSlot;

public class MineraculousClientUtils {
    public static final Component GUI_CHOOSE = Component.translatable("gui.choose");
    public static final Component GUI_NAME = Component.translatable("gui.name");

    private static final Map<Player, SpecialPlayerData> SPECIAL_PLAYER_DATA = new Reference2ReferenceOpenHashMap<>();
    private static final IntList CATACLYSM_PIXELS = new IntArrayList();

    // Special Player Handling
    public static void setSpecialPlayerData(Player player, SpecialPlayerData data) {
        SPECIAL_PLAYER_DATA.put(player, data);
    }

    public static boolean renderBetaTesterLayer(AbstractClientPlayer player) {
        return SPECIAL_PLAYER_DATA.get(player) != null && SPECIAL_PLAYER_DATA.get(player).displayBeta() && SpecialPlayerUtils.renderCosmeticLayerInSlot(player, betaChoice(player).slot());
    }

    public static BetaTesterCosmeticOptions betaChoice(AbstractClientPlayer player) {
        return SPECIAL_PLAYER_DATA.get(player) != null ? SPECIAL_PLAYER_DATA.get(player).choice() : BetaTesterCosmeticOptions.DERBY_HAT;
    }

    public static boolean renderDevLayer(AbstractClientPlayer player) {
        return SPECIAL_PLAYER_DATA.get(player) != null && SPECIAL_PLAYER_DATA.get(player).displayDev() && SpecialPlayerUtils.renderCosmeticLayerInSlot(player, EquipmentSlot.HEAD);
    }

    public static boolean renderLegacyDevLayer(AbstractClientPlayer player) {
        return SPECIAL_PLAYER_DATA.get(player) != null && SPECIAL_PLAYER_DATA.get(player).displayLegacyDev() && SpecialPlayerUtils.renderCosmeticLayerInSlot(player, EquipmentSlot.HEAD);
    }

    public static void syncSpecialPlayerChoices() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            TommyLibServices.NETWORK.sendToServer(new ServerboundUpdateSpecialPlayerDataPayload(player.getUUID(), new SpecialPlayerData(
                    MineraculousClientConfig.get().betaTesterCosmeticChoice.get(),
                    MineraculousClientConfig.get().displayBetaTesterCosmetic.get(),
                    MineraculousClientConfig.get().displayDevTeamCosmetic.get(),
                    MineraculousClientConfig.get().displayLegacyDevTeamCosmetic.get())));
        }
    }

    // Screens
    public static boolean hasNoScreenOpen() {
        return Minecraft.getInstance().screen == null;
    }

    public static void renderEntityInInventorySpinning(
            GuiGraphics guiGraphics,
            int xStart,
            int yStart,
            int xEnd,
            int yEnd,
            int scale,
            float rotation,
            LivingEntity entity) {
        float x = (float) (xStart + xEnd) / 2.0F;
        float y = (float) (yStart + yEnd) / 2.0F;
        guiGraphics.enableScissor(xStart, yStart, xEnd, yEnd);
        Quaternionf flipRot = new Quaternionf().rotateZ(Mth.PI);
        Quaternionf forwardTilt = new Quaternionf().rotateX(90 * 20.0F * (Mth.PI / 180f));
        flipRot.mul(forwardTilt);
        float yBodyRot = entity.yBodyRot;
        float yRot = entity.getYRot();
        float xRot = entity.getXRot();
        float yHeadRotO = entity.yHeadRotO;
        float yHeadRot = entity.yHeadRot;
        entity.yBodyRot = 180.0F + rotation * 2;
        entity.setYRot(180.0F + rotation * 2);
        entity.setXRot(-90 * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        float entityScale = entity.getScale();
        Vector3f vector3f = new Vector3f(0.0F, entity.getBbHeight() / 2.0F + 0.0625F * entityScale, 0.0F);
        float renderScale = (float) scale / entityScale;
        InventoryScreen.renderEntityInInventory(guiGraphics, x, y, renderScale, vector3f, flipRot, forwardTilt, entity);
        entity.yBodyRot = yBodyRot;
        entity.setYRot(yRot);
        entity.setXRot(xRot);
        entity.yHeadRotO = yHeadRotO;
        entity.yHeadRot = yHeadRot;
        guiGraphics.disableScissor();
    }

    public static boolean tryOpenRadialMenuScreenFromProvider(InteractionHand hand, ItemStack stack, RadialMenuProvider<?> provider) {
        if (provider.canOpenMenu(stack, hand, ClientUtils.getLocalPlayer())) {
            Minecraft.getInstance().setScreen(new RadialMenuScreen<>(hand, MineraculousKeyMappings.OPEN_ITEM_RADIAL_MENU.getKey().getValue(), stack, provider));
            return true;
        }
        return false;
    }

    public static void openExternalCuriosInventoryScreen(Player target) {
        TommyLibServices.NETWORK.sendToServer(new ServerboundRequestInventorySyncPayload(target.getUUID(), true));
        Minecraft.getInstance().setScreen(new ExternalCuriosInventoryScreen(target, true, ((slot, target1, menu) -> {
            if (slot instanceof CurioSlot curioSlot)
                TommyLibServices.NETWORK.sendToServer(new ServerboundStealCurioPayload(target1.getUUID(), new CuriosData(curioSlot.getSlotContext())));
            else
                TommyLibServices.NETWORK.sendToServer(new ServerboundStealItemPayload(target1.getUUID(), menu.slots.indexOf(slot)));
        }), exit -> {
            TommyLibServices.NETWORK.sendToServer(new ServerboundRequestInventorySyncPayload(target.getUUID(), false));
        }));
    }

    public static void remoteCloseKamikotizationChatScreen(boolean cancel) {
        if (Minecraft.getInstance().screen instanceof AbstractKamikotizationChatScreen screen)
            screen.onClose(cancel, false);
    }

    public static void openKamikotizationSelectionScreen(Player target, KamikoData kamikoData) {
        Minecraft.getInstance().setScreen(new KamikotizationSelectionScreen(target, kamikoData));
    }

    public static void openReceiverKamikotizationChatScreen(UUID other, KamikotizationData kamikotizationData, Either<Integer, CuriosData> slotInfo) {
        Minecraft.getInstance().setScreen(new ReceiverKamikotizationChatScreen(other, kamikotizationData, slotInfo));
    }

    public static void openPerformerKamikotizationChatScreen(String performerName, String targetName, Optional<ResourceLocation> faceMaskTexture, Player target) {
        Minecraft.getInstance().setScreen(new PerformerKamikotizationChatScreen(performerName, targetName, faceMaskTexture, target));
    }

    public static void openMiraculousTransferScreen(int kwamiId) {
        Minecraft.getInstance().setScreen(new MiraculousTransferScreen(kwamiId));
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
        return player != null && !player.isSpectator() && player.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed() && MineraculousClientUtils.getCameraEntity() instanceof Kamiko kamiko && kamiko.isOwnedBy(player) && MineraculousGuis.getKamikoGui() != null;
    }

    // Rendering
    public static VertexConsumer checkLuckyCharm(VertexConsumer buffer, MultiBufferSource bufferSource, ItemStack itemStack, boolean armor, boolean entity) {
        if (itemStack.has(MineraculousDataComponents.LUCKY_CHARM) && !itemStack.is(MineraculousItemTags.LUCKY_CHARM_SHADER_IMMUNE)) {
            if (entity) {
                return itemStack.is(Items.SHIELD)
                        ? VertexMultiConsumer.create(bufferSource.getBuffer(MineraculousRenderTypes.shieldLuckyCharm()), buffer)
                        : VertexMultiConsumer.create(bufferSource.getBuffer(MineraculousRenderTypes.entityLuckyCharm()), buffer);
            }
            return VertexMultiConsumer.create(bufferSource.getBuffer(armor ? MineraculousRenderTypes.armorLuckyCharm() : MineraculousRenderTypes.itemLuckyCharm()), buffer);
        }
        return buffer;
    }

    public static int getCataclysmPixel(RandomSource random) {
        return CATACLYSM_PIXELS.getInt(random.nextInt(CATACLYSM_PIXELS.size()));
    }

    public static void refreshCataclysmPixels() {
        CATACLYSM_PIXELS.clear();
        try (AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(Mineraculous.modLoc("textures/entity/cataclysm.png"))) {
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

    // Misc
    public static void setShader(@Nullable ResourceLocation location) {
        PostChain current = Minecraft.getInstance().gameRenderer.postEffect;
        if (location != null)
            Minecraft.getInstance().gameRenderer.loadEffect(location);
        else if (current != null)
            Minecraft.getInstance().gameRenderer.postEffect = null;
    }

    @SuppressWarnings("ConstantValue")
    public static Vec3 getFirstPersonHandPosition(boolean offHand, boolean swing, float rightScale, float upScale) {
        Camera camera = Minecraft.getInstance().getEntityRenderDispatcher().camera;
        float partialTicks = camera == null ? 0 : camera.getPartialTickTime();
        return getFirstPersonHandPosition(offHand, swing, partialTicks, rightScale, upScale);
    }

    @SuppressWarnings("ConstantValue")
    public static Vec3 getFirstPersonHandPosition(boolean offHand, boolean swing, float partialTick, float rightScale, float upScale) { //meant to be used only when the local player is in 1st POV
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

            float bodyRotRad = Mth.lerp(partialTick, livingEntity.yBodyRotO, livingEntity.yBodyRot) * (Mth.PI / 180F);
            double sinRot = Mth.sin(bodyRotRad);
            double cosRot = Mth.cos(bodyRotRad);
            float scale = livingEntity.getScale();
            double armOffset = (double) armMultiplier * sideOffset * (double) scale;
            double frontOffsetScaled = frontOffset * (double) scale;
            float crouchOffset = livingEntity.isCrouching() ? -0.1875F : 0;
            return livingEntity.getEyePosition(partialTick).add(-cosRot * armOffset - sinRot * frontOffsetScaled, (double) crouchOffset + heightOffset * (double) scale, -sinRot * armOffset + cosRot * frontOffsetScaled);
        }
        return Vec3.ZERO;
    }

    public record InputState(boolean front, boolean back, boolean left, boolean right, boolean jump) {
        int packInputs() {
            int bits = 0;
            if (front) bits |= 1 << 0;
            if (back) bits |= 1 << 1;
            if (left) bits |= 1 << 2;
            if (right) bits |= 1 << 3;
            if (jump) bits |= 1 << 4;
            return bits;
        }

        public boolean hasInput() {
            return front || back || left || right || jump;
        }

        public Boolean[] getMovementBools() {
            return new Boolean[] { front, back, left, right };
        }
    }

    public static InputState captureInput() {
        var input = Minecraft.getInstance().player.input;
        return new InputState(input.up, input.down, input.left, input.right, input.jumping);
    }
}
