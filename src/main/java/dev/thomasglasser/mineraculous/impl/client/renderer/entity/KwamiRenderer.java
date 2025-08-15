package dev.thomasglasser.mineraculous.impl.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers.MiniHolidayHatGeoLayer;
import dev.thomasglasser.mineraculous.impl.world.entity.Kwami;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import software.bernie.geckolib.renderer.specialty.DynamicGeoEntityRenderer;

public class KwamiRenderer<T extends Kwami> extends DynamicGeoEntityRenderer<T> {
    private static final String HEAD = "head";
    private static final String LEFT_HAND = "left_hand";
    private static final String RIGHT_HAND = "right_hand";

    private final Map<ResourceKey<Miraculous>, GeoModel<T>> models = new Reference2ReferenceOpenHashMap<>();

    public KwamiRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, null);
        withScale(0.5f);
        addRenderLayer(new BlockAndItemGeoLayer<>(this, (bone, entity) -> switch (bone.getName()) {
            case LEFT_HAND -> animatable.isLeftHanded() ? animatable.getMainHandItem() : animatable.getOffhandItem();
            case RIGHT_HAND -> animatable.isLeftHanded() ? animatable.getOffhandItem() : animatable.getMainHandItem();
            default -> null;
        }, (bone, character) -> null) {
            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, T animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
                poseStack.translate(-0.1, 0, 0);
                poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
                poseStack.mulPose(Axis.ZN.rotationDegrees(30));
                super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
            }

            @Override
            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, T animatable) {
                return switch (bone.getName()) {
                    case LEFT_HAND -> ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
                    case RIGHT_HAND -> ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
                    case HEAD -> ItemDisplayContext.HEAD;
                    default -> ItemDisplayContext.NONE;
                };
            }
        });
        addRenderLayer(new MiniHolidayHatGeoLayer<>(this, HEAD));
    }

    @Override
    public GeoModel<T> getGeoModel() {
        T animatable = getAnimatable();
        if (animatable != null) {
            ResourceKey<Miraculous> miraculous = animatable.getMiraculous();
            if (miraculous != null) {
                if (!models.containsKey(miraculous))
                    models.put(miraculous, createGeoModel(miraculous));
                return models.get(miraculous);
            }
        }
        return super.getGeoModel();
    }

    private GeoModel<T> createGeoModel(ResourceKey<Miraculous> miraculous) {
        return new DefaultedEntityGeoModel<>(ResourceLocation.fromNamespaceAndPath(miraculous.location().getNamespace(), "miraculous/" + miraculous.location().getPath())) {
            private ResourceLocation hungryTexture;

            @Override
            public ResourceLocation getTextureResource(T animatable) {
                if (hungryTexture == null) {
                    hungryTexture = super.getTextureResource(animatable).withPath(path -> path.replace(".png", "_hungry.png"));
                }
                if (!animatable.isCharged())
                    return hungryTexture;
                return super.getTextureResource(animatable);
            }
        };
    }
}
