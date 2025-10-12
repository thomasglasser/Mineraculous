package dev.thomasglasser.mineraculous.impl.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.api.client.renderer.layer.ConditionalAutoGlowingGeoLayer;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosUtils;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
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
    private static final Map<ResourceKey<Miraculous>, ModelResourceLocation> MODEL_LOCATIONS = new Reference2ReferenceOpenHashMap<>();
    private static final Map<ResourceLocation, Int2ObjectMap<ResourceLocation>> POWERED_FRAME_TEXTURES = new Object2ReferenceOpenHashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> POWERED_TEXTURES = new Object2ReferenceOpenHashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> HIDDEN_TEXTURES = new Object2ReferenceOpenHashMap<>();

    public MiraculousItemRenderer() {
        super(null);
        addRenderLayer(new ConditionalAutoGlowingGeoLayer<>(this));
    }

    public static Holder<Miraculous> getMiraculousOrDefault(ItemStack stack) {
        Holder<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
        Level level = ClientUtils.getLevel();
        if (miraculous == null && level != null) {
            miraculous = level.registryAccess().registryOrThrow(MineraculousRegistries.MIRACULOUS).getAny().orElse(null);
        }
        if (miraculous == null) {
            throw new IllegalStateException("Tried to render a Miraculous related item without any registered miraculous");
        }
        return miraculous;
    }

    public static void clearAssets() {
        DEFAULT_MODELS.clear();
        MODEL_LOCATIONS.clear();
        POWERED_FRAME_TEXTURES.clear();
        POWERED_TEXTURES.clear();
        HIDDEN_TEXTURES.clear();
    }

    @Override
    public void preRender(PoseStack poseStack, MiraculousItem animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        if (!isReRender) {
            BakedModel miraculousModel = Minecraft.getInstance().getModelManager().getModel(MODEL_LOCATIONS.computeIfAbsent(getMiraculousOrDefault(getCurrentItemStack()).getKey(), key -> ModelResourceLocation.standalone(key.location().withPrefix("item/miraculous/"))));
            if (miraculousModel != Minecraft.getInstance().getModelManager().getMissingModel()) {
                miraculousModel.applyTransform(renderPerspective, poseStack, false);
            }
        }
        // Special case for earrings
        model.getBone("right_earring").ifPresent(bone -> bone.setHidden(renderPerspective == MineraculousItemDisplayContexts.CURIOS_EARRINGS.getValue()));
    }

    protected ResourceLocation getPoweredFrameTexture(MiraculousItem animatable, int frame) {
        if (frame == -1) {
            return getPoweredTexture(animatable);
        }
        ResourceLocation base = super.getTextureLocation(animatable);
        if (frame == 0) {
            return base;
        }
        return POWERED_FRAME_TEXTURES.computeIfAbsent(base, loc -> new Int2ObjectOpenHashMap<>()).computeIfAbsent(frame, i -> base.withPath(path -> path.replace("active", "powered_" + i)));
    }

    protected ResourceLocation getPoweredTexture(MiraculousItem animatable) {
        return POWERED_TEXTURES.computeIfAbsent(super.getTextureLocation(animatable), loc -> loc.withPath(path -> path.replace("active", "powered")));
    }

    protected ResourceLocation getHiddenTexture(MiraculousItem animatable) {
        return HIDDEN_TEXTURES.computeIfAbsent(super.getTextureLocation(animatable), loc -> loc.withPath(path -> path.replace("active", "hidden")));
    }

    @Override
    public ResourceLocation getTextureLocation(MiraculousItem animatable) {
        ItemStack stack = getCurrentItemStack();
        if (stack.has(MineraculousDataComponents.POWERED)) {
            Integer remainingTicks = stack.get(MineraculousDataComponents.REMAINING_TICKS);
            if (remainingTicks != null) {
                int seconds = remainingTicks / SharedConstants.TICKS_PER_SECOND;
                int maxSeconds = MineraculousServerConfig.get().miraculousTimerDuration.get();
                int threshold = Math.max(maxSeconds / MAX_FRAMES, 1);
                int frame = seconds / threshold + 1;
                // Blinks every other second
                if (seconds % 2 == 0) {
                    return getPoweredFrameTexture(animatable, frame - 1);
                } else if (frame < MAX_FRAMES) {
                    return getPoweredFrameTexture(animatable, frame);
                } else {
                    return getPoweredTexture(animatable);
                }
            } else {
                return getPoweredTexture(animatable);
            }
        } else {
            // Use hidden texture only when worn and not transformed
            Integer carrierId = stack.get(MineraculousDataComponents.CARRIER);
            Level level = ClientUtils.getLevel();
            if (carrierId != null && level != null && level.getEntity(carrierId) instanceof LivingEntity entity && !entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed() && CuriosUtils.isEquipped(entity, stack)) {
                return getHiddenTexture(animatable);
            }
        }
        return super.getTextureLocation(animatable);
    }

    @Override
    public GeoModel<MiraculousItem> getGeoModel() {
        return DEFAULT_MODELS.computeIfAbsent(getMiraculousOrDefault(getCurrentItemStack()), this::createDefaultGeoModel);
    }

    private GeoModel<MiraculousItem> createDefaultGeoModel(Holder<Miraculous> miraculous) {
        return new DefaultedItemGeoModel<>(miraculous.getKey().location().withPrefix("miraculous/")) {
            private final ResourceLocation texture = miraculous.getKey().location().withPath(path -> "textures/item/miraculous/" + path + "/active.png");

            @Override
            public ResourceLocation getTextureResource(MiraculousItem animatable) {
                return texture;
            }
        };
    }
}
