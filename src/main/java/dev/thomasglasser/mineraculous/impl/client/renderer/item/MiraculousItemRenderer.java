package dev.thomasglasser.mineraculous.impl.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.api.client.renderer.layer.ConditionalAutoGlowingGeoLayer;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemDisplayContexts;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
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
    private static final Map<ResourceKey<Miraculous>, ModelResourceLocation> MODEL_LOCATIONS = new Reference2ReferenceOpenHashMap<>();
    private static final Map<ResourceLocation, EnumMap<MiraculousItem.TextureState, ResourceLocation>> POWERED_FRAME_TEXTURES = new Object2ReferenceOpenHashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> POWERED_TEXTURES = new Object2ReferenceOpenHashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> HIDDEN_TEXTURES = new Object2ReferenceOpenHashMap<>();

    public MiraculousItemRenderer() {
        super((GeoModel<MiraculousItem>) null);
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

    @Override
    public ResourceLocation getTextureLocation(MiraculousItem animatable) {
        MiraculousItem.TextureState state = getCurrentItemStack().get(MineraculousDataComponents.TEXTURE_STATE);
        ResourceLocation base = super.getTextureLocation(animatable);
        if (state == null || state == MiraculousItem.TextureState.POWERED) {
            return POWERED_TEXTURES.computeIfAbsent(base, loc -> loc.withPath(path -> path.replace("active", "powered")));
        }
        if (state == MiraculousItem.TextureState.ACTIVE) {
            return base;
        }
        if (state == MiraculousItem.TextureState.HIDDEN) {
            return HIDDEN_TEXTURES.computeIfAbsent(base, loc -> loc.withPath(path -> path.replace("active", "hidden")));
        }
        return POWERED_FRAME_TEXTURES.computeIfAbsent(base, loc -> new EnumMap<>(MiraculousItem.TextureState.class)).computeIfAbsent(state, i -> base.withPath(path -> path.replace("active", "powered_" + i.frame())));
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
