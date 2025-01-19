package dev.thomasglasser.mineraculous.client;

import dev.thomasglasser.mineraculous.client.gui.screens.KamikotizationChatScreen;
import dev.thomasglasser.mineraculous.client.gui.screens.KamikotizationSelectionScreen;
import dev.thomasglasser.mineraculous.client.renderer.entity.layers.SnapshotTesterCosmeticOptions;
import dev.thomasglasser.mineraculous.client.renderer.entity.layers.VipData;
import dev.thomasglasser.mineraculous.network.ServerboundChangeVipDataPayload;
import dev.thomasglasser.mineraculous.world.item.component.KamikoData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MineraculousClientUtils {
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

    public static void renderParticlesFollowingEntity(LivingEntity entity, ParticleOptions type, double distanceFromSkin, double forwardShift, double rightShift, double upShift, float scale, boolean firstPerson) {
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

    public static void init() {}
}
