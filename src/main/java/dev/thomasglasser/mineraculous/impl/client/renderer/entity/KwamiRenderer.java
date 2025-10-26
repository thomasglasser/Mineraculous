package dev.thomasglasser.mineraculous.impl.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers.MiniHolidayHatGeoLayer;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.world.entity.Kwami;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import software.bernie.geckolib.renderer.specialty.DynamicGeoEntityRenderer;
import software.bernie.geckolib.util.Color;

public class KwamiRenderer<T extends Kwami> extends DynamicGeoEntityRenderer<T> {
    private static final String HEAD = "head";
    private static final String LEFT_HAND = "left_hand";
    private static final String RIGHT_HAND = "right_hand";

    private static final Int2ObjectOpenHashMap<Color> COLORS = new Int2ObjectOpenHashMap<>();

    private final Map<Holder<Miraculous>, GeoModel<T>> models = new Reference2ReferenceOpenHashMap<>();

    public KwamiRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DefaultedEntityGeoModel<>(MineraculousConstants.modLoc("summoning_cube")) {
            @Override
            public @Nullable Animation getAnimation(T animatable, String name) {
                return null;
            }
        });
        withScale(0.5f);
        addRenderLayer(new BlockAndItemGeoLayer<>(this, (bone, entity) -> switch (bone.getName()) {
            case LEFT_HAND -> animatable.isLeftHanded() ? animatable.getMainHandItem() : animatable.getOffhandItem();
            case RIGHT_HAND -> animatable.isLeftHanded() ? animatable.getOffhandItem() : animatable.getMainHandItem();
            default -> null;
        }, (bone, character) -> null) {
            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, T animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
                poseStack.translate(0, 0, -0.05);
                poseStack.mulPose(Axis.ZN.rotationDegrees(180));
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
    public void actuallyRender(PoseStack poseStack, T animatable, BakedGeoModel model, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        if (isInCubeForm(animatable)) {
            int summonTicks = animatable.getSummonTicks();
            float progress = summonTicks > 0 ? (float) summonTicks / (SharedConstants.TICKS_PER_SECOND * MineraculousServerConfig.get().kwamiSummonTime.getAsInt()) : 0.5F;
            int color = animatable.getMiraculous().value().color().getValue();
            renderRays(poseStack, progress, bufferSource.getBuffer(RenderType.lightning()), color);
            renderRays(poseStack, progress, bufferSource.getBuffer(RenderType.dragonRays()), color);
        }
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, LightTexture.FULL_BRIGHT, packedOverlay, colour);
    }

    private static void renderRays(PoseStack poseStack, float progress, VertexConsumer buffer, int color) {
        poseStack.pushPose();
        poseStack.scale(0.1f, 0.1f, 0.1f);
        poseStack.translate(0.0f, 5, 0.0f);
        float fade = Math.min(progress > 0.8F ? (progress - 0.8F) / 0.2F : 0.0F, 1.0F);
        int centerColor = FastColor.ARGB32.colorFromFloat(1.0F - fade, 1.0F, 1.0F, 1.0F);
        RandomSource random = RandomSource.create(432L);
        Vector3f origin = new Vector3f();
        Vector3f left = new Vector3f();
        Vector3f right = new Vector3f();
        Vector3f front = new Vector3f();
        Quaternionf rotation = new Quaternionf();
        int rayCount = Mth.floor((progress + progress * progress) / 2.0F * 60.0F);

        for (int i = 0; i < rayCount; i++) {
            rotation.rotationXYZ(
                    random.nextFloat() * (float) (Math.PI * 2),
                    random.nextFloat() * (float) (Math.PI * 2),
                    random.nextFloat() * (float) (Math.PI * 2))
                    .rotateXYZ(
                            random.nextFloat() * (float) (Math.PI * 2),
                            random.nextFloat() * (float) (Math.PI * 2),
                            random.nextFloat() * (float) (Math.PI * 2) + progress * (float) (Math.PI / 2));
            poseStack.mulPose(rotation);
            float rayLength = random.nextFloat() * 20.0F + 5.0F + fade * 10.0F;
            float rayRadius = random.nextFloat() * 2.0F + 1.0F + fade * 2.0F;
            left.set(-EnderDragonRenderer.HALF_SQRT_3 * rayRadius, rayLength, -0.5F * rayRadius);
            right.set(EnderDragonRenderer.HALF_SQRT_3 * rayRadius, rayLength, -0.5F * rayRadius);
            front.set(0.0F, rayLength, rayRadius);
            PoseStack.Pose pose = poseStack.last();
            buffer.addVertex(pose, origin).setColor(centerColor);
            buffer.addVertex(pose, left).setColor(color);
            buffer.addVertex(pose, right).setColor(color);
            buffer.addVertex(pose, origin).setColor(centerColor);
            buffer.addVertex(pose, right).setColor(color);
            buffer.addVertex(pose, front).setColor(color);
            buffer.addVertex(pose, origin).setColor(centerColor);
            buffer.addVertex(pose, front).setColor(color);
            buffer.addVertex(pose, left).setColor(color);
        }

        poseStack.popPose();
    }

    @Override
    public GeoModel<T> getGeoModel() {
        T animatable = getAnimatable();
        if (animatable != null && !isInCubeForm(animatable)) {
            Holder<Miraculous> miraculous = animatable.getMiraculous();
            if (miraculous != null) {
                if (!models.containsKey(miraculous))
                    models.put(miraculous, createGeoModel(miraculous, Kwami::isCharged));
                return models.get(miraculous);
            }
        }
        return super.getGeoModel();
    }

    public static <T extends GeoAnimatable> GeoModel<T> createGeoModel(Holder<Miraculous> miraculous, Predicate<T> chargedPredicate) {
        return new DefaultedEntityGeoModel<>(miraculous.getKey().location().withPrefix("miraculous/")) {
            private ResourceLocation hungryTexture;

            @Override
            public ResourceLocation getTextureResource(T animatable) {
                if (hungryTexture == null) {
                    hungryTexture = super.getTextureResource(animatable).withPath(path -> path.replace(".png", "_hungry.png"));
                }
                if (!chargedPredicate.test(animatable))
                    return hungryTexture;
                return super.getTextureResource(animatable);
            }
        };
    }

    @Override
    public Color getRenderColor(T animatable, float partialTick, int packedLight) {
        if (isInCubeForm(animatable)) {
            return COLORS.computeIfAbsent(animatable.getMiraculous().value().color().getValue(), Color::new);
        }
        return super.getRenderColor(animatable, partialTick, packedLight);
    }

    private boolean isInCubeForm(T animatable) {
        return animatable.getSummonTicks() > 0 || animatable.isTransforming();
    }
}
