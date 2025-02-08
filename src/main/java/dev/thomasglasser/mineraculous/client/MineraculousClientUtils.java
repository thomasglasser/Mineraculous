package dev.thomasglasser.mineraculous.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import dev.thomasglasser.mineraculous.client.gui.screens.KamikotizationChatScreen;
import dev.thomasglasser.mineraculous.client.gui.screens.KamikotizationSelectionScreen;
import dev.thomasglasser.mineraculous.client.gui.screens.LookCustomizationScreen;
import dev.thomasglasser.mineraculous.client.gui.screens.MiraculousTransferScreen;
import dev.thomasglasser.mineraculous.client.gui.screens.inventory.ExternalCuriosInventoryScreen;
import dev.thomasglasser.mineraculous.client.renderer.MineraculousRenderTypes;
import dev.thomasglasser.mineraculous.client.renderer.entity.layers.SnapshotTesterCosmeticOptions;
import dev.thomasglasser.mineraculous.client.renderer.entity.layers.VipData;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.network.ServerboundChangeVipDataPayload;
import dev.thomasglasser.mineraculous.network.ServerboundRequestInventorySyncPayload;
import dev.thomasglasser.mineraculous.network.ServerboundStealCuriosPayload;
import dev.thomasglasser.mineraculous.network.ServerboundStealItemPayload;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.component.KamikoData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedMiraculousLookData;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedSuitLookData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import top.theillusivec4.curios.common.inventory.CurioSlot;

public class MineraculousClientUtils {
    public static final String CHOOSE = "gui.choose";
    public static final String NAME = "gui.name";

    private static final String GIST = "thomasglasser/aa8eb933847685b93d8f99a59f07b62e";
    private static final HashMap<Player, VipData> vipData = new HashMap<>();

    public static boolean renderCosmeticLayerInSlot(AbstractClientPlayer player, EquipmentSlot slot) {
        return slot == null || !player.hasItemInSlot(slot);
    }

    public static boolean renderSnapshotTesterLayer(AbstractClientPlayer player) {
        return vipData.get(player) != null && snapshotChoice(player) != null && renderCosmeticLayerInSlot(player, snapshotChoice(player).slot()) && vipData.get(player).displaySnapshot();
    }

    @Nullable
    public static SnapshotTesterCosmeticOptions snapshotChoice(AbstractClientPlayer player) {
//        return vipData.get(player) != null && vipData.get(player).choice() != null ? vipData.get(player).choice() : null;
        return null;
    }

    public static boolean renderDevLayer(AbstractClientPlayer player) {
        return vipData.get(player) != null && renderCosmeticLayerInSlot(player, EquipmentSlot.HEAD/*TODO:Figure out slot*/) && vipData.get(player).displayDev();
    }

    public static boolean renderLegacyDevLayer(AbstractClientPlayer player) {
        return vipData.get(player) != null && renderCosmeticLayerInSlot(player, EquipmentSlot.HEAD/*TODO:Figure out slot*/) && vipData.get(player).displayLegacyDev();
    }

    public static void refreshVip() {
        if (Minecraft.getInstance().player != null) {
            UUID uuid = Minecraft.getInstance().player.getUUID();

            boolean displaySnapshot;
            boolean displayDev;
            boolean displayLegacyDev;

            displaySnapshot = MineraculousClientConfig.get().displaySnapshotTesterCosmetic.get() && ClientUtils.checkSnapshotTester(GIST, uuid);
            displayDev = MineraculousClientConfig.get().displayDevTeamCosmetic.get() && ClientUtils.checkDevTeam(GIST, uuid);
            displayLegacyDev = MineraculousClientConfig.get().displayLegacyDevTeamCosmetic.get() && ClientUtils.checkLegacyDevTeam(GIST, uuid);

            TommyLibServices.NETWORK.sendToServer(new ServerboundChangeVipDataPayload(uuid, new VipData(/*MineraculousClientConfig.get().snapshotTesterCosmeticChoice.get(),*/ displaySnapshot, /*displayDev*/false, displayLegacyDev)));
        }
    }

    public static boolean verifySnapshotTester(UUID uuid) {
        return ClientUtils.checkSnapshotTester(GIST, uuid);
    }

    public static void setVipData(Player player, VipData data) {
        vipData.put(player, data);
    }

    public static void setShader(@Nullable ResourceLocation location) {
        PostChain current = Minecraft.getInstance().gameRenderer.postEffect;
        if (location != null)
            Minecraft.getInstance().gameRenderer.loadEffect(location);
        else if (current != null)
            Minecraft.getInstance().gameRenderer.postEffect = null;
    }

    public static boolean isFirstPerson() {
        return Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }

    public static void renderParticlesFollowingEntity(LivingEntity entity, ParticleOptions type, double distanceFromSkin, double forwardShift, double rightShift, double upShift, boolean firstPerson) {
        Vec3 angle = firstPerson ? entity.getLookAngle() : new Vec3(0, 0, 0);
        Vec3 worldUp = new Vec3(0, 1, 0);
        Vec3 localForward = Vec3.directionFromRotation(new Vec2(0, firstPerson ? entity.getYRot() : entity.yBodyRot));
        Vec3 right = localForward.cross(worldUp);
        Vec3 up = localForward.cross(right);

        double x = entity.getX() + (rightShift * right.x()) - (upShift * up.x());
        double y = entity.getY() + (rightShift * right.y()) - (upShift * up.y());
        double z = entity.getZ() + (rightShift * right.z()) - (upShift * up.z());

        if (firstPerson) {
            x += (distanceFromSkin * angle.x());
            y += entity.getEyeHeight() + (distanceFromSkin * angle.y());
            z += (distanceFromSkin * angle.z());
        } else {
            Vec3 forward = localForward.scale(forwardShift);
            x += forward.x();
            y += forward.y();
            z += forward.z();
        }

        entity.level().addParticle(type, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    public static boolean hasNoScreenOpen() {
        return Minecraft.getInstance().screen == null;
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

    public static boolean isCameraEntityOther() {
        return Minecraft.getInstance().cameraEntity != Minecraft.getInstance().player && Minecraft.getInstance().cameraEntity != null;
    }

    public static void openKamikotizationSelectionScreen(Player target, KamikoData kamikoData) {
        ClientUtils.setScreen(new KamikotizationSelectionScreen(Component.translatable(KamikotizationSelectionScreen.TITLE), target, kamikoData));
    }

    public static void openKamikotizationChatScreen(Player other, KamikotizationData kamikotizationData) {
        ClientUtils.setScreen(new KamikotizationChatScreen(other, kamikotizationData));
    }

    public static void openKamikotizationChatScreen(String targetName, String performerName, Player target) {
        ClientUtils.setScreen(new KamikotizationChatScreen(targetName, performerName, target));
    }

    public static void closeKamikotizationChatScreen(boolean cancel) {
        if (Minecraft.getInstance().screen instanceof KamikotizationChatScreen screen)
            screen.onClose(cancel, false);
    }

    public static void openMiraculousTransferScreen(int kwamiId) {
        ClientUtils.setScreen(new MiraculousTransferScreen(kwamiId));
    }

    public static VertexConsumer checkLuckyCharm(VertexConsumer buffer, MultiBufferSource bufferSource, ItemStack itemStack) {
        return itemStack.has(MineraculousDataComponents.LUCKY_CHARM) ? VertexMultiConsumer.create(bufferSource.getBuffer(MineraculousRenderTypes.luckyCharm()), buffer) : buffer;
    }

    public static void registerDynamicTexture(ResourceLocation texture, byte[] pixels) throws IOException {
        Minecraft.getInstance().getTextureManager().register(texture, new DynamicTexture(NativeImage.read(pixels)));
    }

    public static void openExternalCuriosInventoryScreen(Player target, Player player) {
        ClientUtils.setScreen(new ExternalCuriosInventoryScreen(target, true, ((slot, target1, menu) -> {
            if (slot instanceof CurioSlot curioSlot)
                TommyLibServices.NETWORK.sendToServer(new ServerboundStealCuriosPayload(target1.getUUID(), new CuriosData(curioSlot.getSlotIndex(), curioSlot.getIdentifier())));
            else
                TommyLibServices.NETWORK.sendToServer(new ServerboundStealItemPayload(target1.getUUID(), menu.slots.indexOf(slot)));
        }), exit -> {
            TommyLibServices.NETWORK.sendToServer(new ServerboundRequestInventorySyncPayload(target.getUUID()));
            TommyLibServices.NETWORK.sendToServer(new ServerboundRequestInventorySyncPayload(player.getUUID()));
        }));
    }

    public static void openLookCustomizationScreen(ResourceKey<Miraculous> miraculous, Map<String, FlattenedSuitLookData> serverSuits, Map<String, FlattenedMiraculousLookData> serverMiraculous) {
        ClientUtils.setScreen(new LookCustomizationScreen(miraculous, serverSuits, serverMiraculous));
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
        Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf quaternionf1 = new Quaternionf().rotateX(90 * 20.0F * (float) (Math.PI / 180.0));
        quaternionf.mul(quaternionf1);
        float f4 = entity.yBodyRot;
        float f5 = entity.getYRot();
        float f6 = entity.getXRot();
        float f7 = entity.yHeadRotO;
        float f8 = entity.yHeadRot;
        entity.yBodyRot = 180.0F + rotation * 2;
        entity.setYRot(180.0F + rotation * 2);
        entity.setXRot(-90 * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        float f9 = entity.getScale();
        Vector3f vector3f = new Vector3f(0.0F, entity.getBbHeight() / 2.0F + 0.0625F * f9, 0.0F);
        float f10 = (float) scale / f9;
        InventoryScreen.renderEntityInInventory(guiGraphics, x, y, f10, vector3f, quaternionf, quaternionf1, entity);
        entity.yBodyRot = f4;
        entity.setYRot(f5);
        entity.setXRot(f6);
        entity.yHeadRotO = f7;
        entity.yHeadRot = f8;
        guiGraphics.disableScissor();
    }

    public static void init() {}
}
