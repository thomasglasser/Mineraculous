package dev.thomasglasser.mineraculous.impl.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.renderer.item.BlockingDefaultedGeoItemRenderer;
import dev.thomasglasser.mineraculous.api.client.renderer.layer.ConditionalAutoGlowingGeoLayer;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.mineraculous.impl.world.item.LadybugYoyoItem;
import dev.thomasglasser.mineraculous.impl.world.level.storage.ThrownLadybugYoyoData;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class LadybugYoyoRenderer extends BlockingDefaultedGeoItemRenderer<LadybugYoyoItem> {
    public static final ResourceLocation SPYGLASS_SCOPE_LOCATION = MineraculousConstants.modLoc("textures/misc/ladybug_yoyo_spyglass_scope.png");
    public static final ResourceLocation SPYGLASS_LOCATION = makeTextureLocation(MineraculousConstants.modLoc("ladybug_yoyo_spyglass"));
    public static final ResourceLocation PHONE_LOCATION = makeTextureLocation(MineraculousConstants.modLoc("ladybug_yoyo_phone"));

    // Hand Rendering Constants
    private static final Quaternionf HAND_XP_ROT = Axis.XP.rotationDegrees(-40);
    private static final Quaternionf RIGHT_HAND_YP_ROT = Axis.YP.rotationDegrees(110);
    private static final Quaternionf LEFT_HAND_YP_ROT = Axis.YP.rotationDegrees(-110);
    private static final Quaternionf RIGHT_HAND_ZP_ROT = Axis.ZP.rotationDegrees(-10);
    private static final Quaternionf LEFT_HAND_ZP_ROT = Axis.ZP.rotationDegrees(10);

    public LadybugYoyoRenderer() {
        super(MineraculousItems.LADYBUG_YOYO.getId());
        addRenderLayer(new ConditionalAutoGlowingGeoLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(LadybugYoyoItem animatable) {
        LadybugYoyoItem.Mode mode = getCurrentItemStack().get(MineraculousDataComponents.LADYBUG_YOYO_MODE);
        if (mode == LadybugYoyoItem.Mode.PHONE) {
            return PHONE_LOCATION;
        } else if (mode == LadybugYoyoItem.Mode.SPYGLASS) {
            return SPYGLASS_LOCATION;
        }
        return super.getTextureLocation(animatable);
    }

    @Override
    public void defaultRender(PoseStack poseStack, LadybugYoyoItem animatable, MultiBufferSource bufferSource, @Nullable RenderType renderType, @Nullable VertexConsumer buffer, float yaw, float partialTick, int packedLight) {
        Integer carrierId = getCurrentItemStack().get(MineraculousDataComponents.CARRIER);
        Level level = ClientUtils.getLevel();
        if (carrierId != null && level != null) {
            Entity carrier = level.getEntity(carrierId);
            if (carrier != null) {
                ThrownLadybugYoyoData data = carrier.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO);
                ThrownLadybugYoyo yoyo = data.getThrownYoyo(carrier.level());
                if (yoyo != null) {
                    if (MineraculousClientUtils.isFirstPerson() && MineraculousClientUtils.getCameraEntity() == carrier && carrier instanceof AbstractClientPlayer player) {
                        InteractionHand initialHand = yoyo.getHand();
                        InteractionHand currentHand = switch (player.getMainArm()) {
                            case LEFT -> this.renderPerspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                            case RIGHT -> this.renderPerspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                        };
                        if (initialHand == currentHand) {
                            renderHand(player, this.renderPerspective, poseStack, bufferSource, packedLight);
                        }
                    }
                    return;
                } else if (carrier.getData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO).isPresent()) {
                    if (MineraculousClientUtils.isFirstPerson() && MineraculousClientUtils.getCameraEntity() == carrier && carrier instanceof AbstractClientPlayer player) {
                        renderHand(player, this.renderPerspective, poseStack, bufferSource, packedLight);
                    }
                    return;
                }
            }
        }
        super.defaultRender(poseStack, animatable, bufferSource, renderType, buffer, yaw, partialTick, packedLight);
    }

    private static void renderHand(AbstractClientPlayer player, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        PlayerRenderer playerRenderer = (PlayerRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
        poseStack.scale(2, 2, 2);
        if (context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
            poseStack.translate(0.5, -0.5, -0.2);
            poseStack.mulPose(HAND_XP_ROT);
            poseStack.mulPose(RIGHT_HAND_YP_ROT);
            poseStack.mulPose(RIGHT_HAND_ZP_ROT);
            playerRenderer.renderRightHand(poseStack, multiBufferSource, packedLight, player);
        } else if (context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            poseStack.translate(0, -0.5, -0.2);
            poseStack.mulPose(HAND_XP_ROT);
            poseStack.mulPose(LEFT_HAND_YP_ROT);
            poseStack.mulPose(LEFT_HAND_ZP_ROT);
            playerRenderer.renderLeftHand(poseStack, multiBufferSource, packedLight, player);
        }
    }
}
