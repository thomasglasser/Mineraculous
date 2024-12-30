package dev.thomasglasser.mineraculous.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.renderer.item.ButterflyCaneRenderer;
import dev.thomasglasser.mineraculous.world.entity.projectile.ThrownButterflyCane;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ThrownButterflyCaneRenderer extends GeoEntityRenderer<ThrownButterflyCane> {
    public ThrownButterflyCaneRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedItemGeoModel<>(Mineraculous.modLoc("butterfly_cane")));
    }

    @Override
    public void actuallyRender(PoseStack poseStack, ThrownButterflyCane animatable, BakedGeoModel model, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        Boolean isCovered = animatable.isCovered;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, animatable.yRotO, animatable.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot()) + 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));

        poseStack.translate(0.0, -1.0, 0.0);
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownButterflyCane animatable) {
        return ButterflyCaneRenderer.TEXTURE;
    }
}
