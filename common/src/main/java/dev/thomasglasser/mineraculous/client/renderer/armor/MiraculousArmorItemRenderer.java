package dev.thomasglasser.mineraculous.client.renderer.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientConfig;
import dev.thomasglasser.mineraculous.world.item.armor.MiraculousArmorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class MiraculousArmorItemRenderer extends GeoArmorRenderer<MiraculousArmorItem> {

    private final String miraculous;
    private final ResourceLocation defaultTexture;
    private final ResourceLocation defaultModel;

    public MiraculousArmorItemRenderer(String miraculous) {
        super(new DefaultedItemGeoModel<>(Mineraculous.modLoc("armor/miraculous/" + miraculous + "_miraculous_suit")));
        this.miraculous = miraculous;
        defaultTexture = Mineraculous.modLoc("textures/models/armor/miraculous/" + miraculous + "_miraculous_suit_default.png");
        defaultModel = Mineraculous.modLoc("geo/item/armor/miraculous/" + miraculous + "_miraculous_suit_default.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(MiraculousArmorItem animatable)
    {
        if (getCurrentEntity() instanceof Player player && MineraculousClientConfig.enableCustomHiddenVariants)
        {
            ResourceLocation texture = Mineraculous.modLoc("textures/models/armor/miraculous/" + miraculous + "_miraculous_suit_" + player.getGameProfile().getName().toLowerCase() + ".png");
            return Minecraft.getInstance().getTextureManager().getTexture(texture) != MissingTextureAtlasSprite.getTexture() ? texture : defaultTexture;
        }
        return defaultTexture;
    }

    public ResourceLocation getModelLocation(MiraculousArmorItem animatable)
    {
        if (getCurrentEntity() instanceof Player player && MineraculousClientConfig.enableCustomHiddenVariants)
        {
            ResourceLocation model = Mineraculous.modLoc("geo/item/armor/miraculous/" + miraculous + "_miraculous_suit_" + player.getGameProfile().getName().toLowerCase() + ".geo.json");
            return GeckoLibCache.getBakedModels().containsKey(model) ? model : defaultModel;
        }
        return defaultModel;
    }

    @Override
    public void defaultRender(PoseStack poseStack, MiraculousArmorItem animatable, MultiBufferSource bufferSource, @Nullable RenderType renderType, @Nullable VertexConsumer buffer, float yaw, float partialTick, int packedLight)
    {
        poseStack.pushPose();

        Color renderColor = getRenderColor(animatable, partialTick, packedLight);
        float red = renderColor.getRedFloat();
        float green = renderColor.getGreenFloat();
        float blue = renderColor.getBlueFloat();
        float alpha = renderColor.getAlphaFloat();
        int packedOverlay = getPackedOverlay(animatable, 0, partialTick);
        BakedGeoModel model = getGeoModel().getBakedModel(getModelLocation(animatable));

        if (renderType == null)
            renderType = getRenderType(animatable, getTextureLocation(animatable), bufferSource, partialTick);

        if (buffer == null)
            buffer = bufferSource.getBuffer(renderType);

        preRender(poseStack, animatable, model, bufferSource, buffer, false, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        if (firePreRenderEvent(poseStack, model, bufferSource, partialTick, packedLight)) {
            preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, packedLight, packedLight, packedOverlay);
            actuallyRender(poseStack, animatable, model, renderType,
                    bufferSource, buffer, false, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
            applyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
            postRender(poseStack, animatable, model, bufferSource, buffer, false, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
            firePostRenderEvent(poseStack, model, bufferSource, partialTick, packedLight);
        }

        poseStack.popPose();

        renderFinal(poseStack, animatable, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void preRender(PoseStack poseStack, MiraculousArmorItem animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        this.entityRenderTranslations = new Matrix4f(poseStack.last().pose());

        applyBaseModel(this.baseModel);
        grabRelevantBones(getGeoModel().getBakedModel(getModelLocation(this.animatable)));
        applyBaseTransformations(this.baseModel);
        scaleModelForBaby(poseStack, animatable, partialTick, isReRender);
        scaleModelForRender(this.scaleWidth, this.scaleHeight, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);

        if (!(this.currentEntity instanceof GeoAnimatable))
            applyBoneVisibilityBySlot(this.currentSlot);
    }
}
