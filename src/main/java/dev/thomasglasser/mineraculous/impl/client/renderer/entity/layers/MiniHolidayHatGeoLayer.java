package dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class MiniHolidayHatGeoLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {
    private final GeoObjectRenderer<T> hatRenderer;
    private final String targetBone;

    public MiniHolidayHatGeoLayer(GeoRenderer<T> entityRendererIn, String targetBone) {
        super(entityRendererIn);

        this.targetBone = targetBone;
        this.hatRenderer = new GeoObjectRenderer<>(new DefaultedEntityGeoModel<>(Mineraculous.modLoc("mini_holiday_hat")) {
            @Override
            public @Nullable Animation getAnimation(T animatable, String name) {
                return null;
            }
        });
    }

    @Override
    public void renderForBone(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (ClientUtils.isHoliday() && bone.getName().equals(this.targetBone)) {
            poseStack.pushPose();
            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(hatRenderer.getTextureLocation(animatable)));
            poseStack.translate(-0.5, 0.05, -0.5);
            hatRenderer.render(poseStack, animatable, bufferSource, renderType, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }
}
