package dev.thomasglasser.mineraculous.impl.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nyfaria.awcapi.ClientClimberHelper;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.entity.animal.ButterflyVariant;
import dev.thomasglasser.mineraculous.impl.world.entity.animal.Caterpillar;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CaterpillarRenderer<T extends Caterpillar> extends GeoEntityRenderer<T> {
    public CaterpillarRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(MineraculousConstants.modLoc("caterpillar")));
        withScale(0.2F);
    }

    @Override
    public void actuallyRender(PoseStack poseStack, T animatable, BakedGeoModel model, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        ClientClimberHelper.preRenderClimber(animatable, partialTick, poseStack);
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        ClientClimberHelper.postRenderClimber(animatable, partialTick, poseStack, bufferSource);
    }

    @Override
    public float getMotionAnimThreshold(T animatable) {
        return 0.005F;
    }

    @Override
    public ResourceLocation getTextureLocation(T animatable) {
        Holder<ButterflyVariant> variant = animatable.getVariant();
        return variant.value().caterpillarTexture().orElseGet(() -> {
            MineraculousConstants.LOGGER.warn("Tried to render caterpillar for invalid variant {}", variant.getKey());
            return TextureManager.INTENTIONAL_MISSING_TEXTURE;
        });
    }
}
