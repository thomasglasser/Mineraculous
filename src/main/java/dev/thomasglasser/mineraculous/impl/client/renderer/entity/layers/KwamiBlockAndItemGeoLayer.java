package dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.BiFunction;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class KwamiBlockAndItemGeoLayer<T extends GeoAnimatable> extends BlockAndItemGeoLayer<T> {
    private final String headBone;
    private final String leftHandBone;
    private final String rightHandBone;

    public KwamiBlockAndItemGeoLayer(GeoRenderer<T> renderer, String headBone, String leftHandBone, String rightHandBone, BiFunction<GeoBone, T, ItemStack> stackForBone) {
        super(renderer, stackForBone, (bone, character) -> null);
        this.headBone = headBone;
        this.leftHandBone = leftHandBone;
        this.rightHandBone = rightHandBone;
    }

    @Override
    protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, T animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
        poseStack.translate(0, 0, -0.06);
        poseStack.mulPose(Axis.ZN.rotationDegrees(110));
        super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
    }

    @Override
    protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, T animatable) {
        if (bone.getName().equals(headBone)) return ItemDisplayContext.HEAD;
        if (bone.getName().equals(leftHandBone)) return ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        if (bone.getName().equals(rightHandBone)) return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        return ItemDisplayContext.NONE;
    }
}
