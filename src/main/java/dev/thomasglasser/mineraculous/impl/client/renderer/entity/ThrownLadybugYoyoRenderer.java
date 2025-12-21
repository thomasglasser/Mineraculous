package dev.thomasglasser.mineraculous.impl.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.thomasglasser.mineraculous.api.client.look.renderer.ThrownMiraculousToolLookRenderer;
import dev.thomasglasser.mineraculous.api.client.model.LookGeoModel;
import dev.thomasglasser.mineraculous.api.client.renderer.layer.ConditionalAutoGlowingGeoLayer;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownLadybugYoyo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ThrownLadybugYoyoRenderer<T extends ThrownLadybugYoyo> extends GeoEntityRenderer<T> implements ThrownMiraculousToolLookRenderer<T> {
    private final GeoModel<T> model;

    public ThrownLadybugYoyoRenderer(EntityRendererProvider.Context context) {
        super(context, (GeoModel<T>) null);
        addRenderLayer(new ConditionalAutoGlowingGeoLayer<>(this));

        this.model = new LookGeoModel<>(this);
    }

    @Override
    public GeoModel<T> getGeoModel() {
        return model;
    }

    @Override
    public void defaultRender(PoseStack poseStack, T animatable, MultiBufferSource bufferSource, @Nullable RenderType renderType, @Nullable VertexConsumer buffer, float yaw, float partialTick, int packedLight) {
        if (animatable.getOwner() instanceof Player) {
            poseStack.pushPose();

            Player projectilePlayer = animatable.getPlayerOwner();
            if (projectilePlayer == null) return;
            double maxLength;
            Vec3 playerHandPos;
            boolean offHand = !(animatable.getHand() == InteractionHand.MAIN_HAND);
            if (projectilePlayer == Minecraft.getInstance().player && Minecraft.getInstance().getEntityRenderDispatcher().options.getCameraType().isFirstPerson()) {
                playerHandPos = MineraculousClientUtils.getFirstPersonHandPosition(offHand, partialTick);
            } else {
                playerHandPos = MineraculousClientUtils.getHumanoidEntityHandPos(projectilePlayer, offHand, partialTick, 0.15f, -0.75, 0.35f);
            }
            maxLength = animatable.getMaxRopeLength();
            Vec3 projectilePos = animatable.getPosition(partialTick);

            YoyoRopeRenderer.renderRope(playerHandPos, projectilePos, maxLength, poseStack, bufferSource);

            if (animatable.getInitialDirection() == Direction.SOUTH || animatable.getInitialDirection() == Direction.NORTH) {
                poseStack.mulPose(Axis.ZN.rotationDegrees(90));
                poseStack.translate(-0.15, 0, 0);
            } else {
                poseStack.mulPose(Axis.XN.rotationDegrees(90));
                poseStack.translate(0, 0, 0.15);
            }
            poseStack.translate(0, -0.1, 0);

            super.defaultRender(poseStack, animatable, bufferSource, renderType, buffer, yaw, partialTick, packedLight);
            poseStack.popPose();
        }
    }
}
