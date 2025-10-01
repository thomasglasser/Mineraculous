package dev.thomasglasser.mineraculous.impl.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.api.client.renderer.layer.ConditionalAutoGlowingGeoLayer;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemDisplayContexts;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class MiraculousItemRenderer extends GeoItemRenderer<MiraculousItem> {
    public static final int MAX_FRAMES = 5;

    private static final Map<Holder<Miraculous>, GeoModel<MiraculousItem>> DEFAULT_MODELS = new Reference2ReferenceOpenHashMap<>();
    private static final Map<ResourceLocation, Int2ObjectMap<ResourceLocation>> POWERED_TEXTURES = new Object2ReferenceOpenHashMap<>();

    public MiraculousItemRenderer() {
        super(null);
        addRenderLayer(new ConditionalAutoGlowingGeoLayer<>(this));
    }

    public static void clearAssets() {
        DEFAULT_MODELS.clear();
        POWERED_TEXTURES.clear();
    }

    @Override
    public void preRender(PoseStack poseStack, MiraculousItem animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        if (!isReRender) {
            ItemStack stack = getCurrentItemStack();
            if (stack != null) {
                Holder<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
                Level level = ClientUtils.getLevel();
                if (miraculous == null && level != null) {
                    miraculous = level.registryAccess().registryOrThrow(MineraculousRegistries.MIRACULOUS).getAny().orElse(null);
                }
                if (miraculous != null) {
                    BakedModel miraculousModel = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(miraculous.getKey().location().getNamespace(), "item/miraculous/" + miraculous.getKey().location().getPath())));
                    if (miraculousModel != Minecraft.getInstance().getModelManager().getMissingModel()) {
                        miraculousModel.applyTransform(renderPerspective, poseStack, false);
                    }
                }
            }
        }
        // Special case for earrings
        model.getBone("right_earring").ifPresent(bone -> bone.setHidden(renderPerspective == MineraculousItemDisplayContexts.CURIOS_EARRINGS.getValue()));
    }

    protected ResourceLocation getPoweredFrameTexture(MiraculousItem animatable, int frame) {
        ResourceLocation base = super.getTextureLocation(animatable);
        return POWERED_TEXTURES.computeIfAbsent(base, loc -> new Int2ObjectOpenHashMap<>()).computeIfAbsent(frame, i -> base.withPath(path -> path.replace("hidden", "powered_" + i)));
    }

    protected ResourceLocation getPoweredTexture(MiraculousItem animatable) {
        ResourceLocation base = super.getTextureLocation(animatable);
        return POWERED_TEXTURES.computeIfAbsent(base, loc -> new Int2ObjectOpenHashMap<>()).computeIfAbsent(-1, i -> base.withPath(path -> path.replace("hidden", "powered")));
    }

    @Override
    public ResourceLocation getTextureLocation(MiraculousItem animatable) {
        ItemStack stack = getCurrentItemStack();
        if (stack != null && stack.has(MineraculousDataComponents.POWERED)) {
            Integer remainingTicks = stack.get(MineraculousDataComponents.REMAINING_TICKS);
            if (remainingTicks != null) {
                int seconds = remainingTicks / SharedConstants.TICKS_PER_SECOND;
                int maxSeconds = MineraculousServerConfig.get().miraculousTimerDuration.get();
                int threshold = Math.max(maxSeconds / MAX_FRAMES, 1);
                int frame = seconds / threshold + 1;
                if (seconds % 2 == 0) {
                    return getPoweredFrameTexture(animatable, frame - 1);
                } else if (frame < MAX_FRAMES) {
                    return getPoweredFrameTexture(animatable, frame);
                } else {
                    // Blinks every other second
                    return getPoweredTexture(animatable);
                }
            } else {
                return getPoweredTexture(animatable);
            }
        }
        return super.getTextureLocation(animatable);
    }

    @Override
    public GeoModel<MiraculousItem> getGeoModel() {
        Holder<Miraculous> miraculous = getCurrentItemStack().get(MineraculousDataComponents.MIRACULOUS);
        Level level = ClientUtils.getLevel();
        if (miraculous == null && level != null) {
            miraculous = level.registryAccess().registryOrThrow(MineraculousRegistries.MIRACULOUS).getAny().orElse(null);
        }
        if (miraculous != null) {
            if (!DEFAULT_MODELS.containsKey(miraculous))
                DEFAULT_MODELS.put(miraculous, createDefaultGeoModel(miraculous));
            return DEFAULT_MODELS.get(miraculous);
        }
        throw new IllegalStateException("Tried to render a Miraculous Item without any registered miraculous");
    }

    private GeoModel<MiraculousItem> createDefaultGeoModel(Holder<Miraculous> miraculous) {
        return new DefaultedItemGeoModel<>(miraculous.getKey().location().withPrefix("miraculous/")) {
            private final ResourceLocation texture = miraculous.getKey().location().withPrefix("textures/item/miraculous/").withSuffix("/hidden.png");

            @Override
            public ResourceLocation getTextureResource(MiraculousItem animatable) {
                return texture;
            }
        };
    }
}
