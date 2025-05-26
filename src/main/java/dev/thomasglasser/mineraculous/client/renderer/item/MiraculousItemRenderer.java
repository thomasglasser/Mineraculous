package dev.thomasglasser.mineraculous.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.renderer.texture.DynamicAutoGlowingTexture;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.MineraculousItemDisplayContexts;
import dev.thomasglasser.mineraculous.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousLookData;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.io.IOException;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.texture.AutoGlowingTexture;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class MiraculousItemRenderer extends GeoItemRenderer<MiraculousItem> {
    private static final Map<ResourceKey<Miraculous>, GeoModel<MiraculousItem>> DEFAULT_MODELS = new Reference2ReferenceOpenHashMap<>();
    private static final Map<MiraculousLookData, GeoModel<MiraculousItem>> LOOK_MODELS = new Reference2ReferenceOpenHashMap<>();

    public MiraculousItemRenderer() {
        super(null);
        addRenderLayer(new AutoGlowingGeoLayer<>(this) {
            @Override
            protected @Nullable RenderType getRenderType(MiraculousItem animatable, @Nullable MultiBufferSource bufferSource) {
                ResourceLocation texture = getTextureLocation(animatable);
                ResourceLocation glowmaskTexture = AutoGlowingTexture.appendToPath(texture, "_glowmask");
                if (Minecraft.getInstance().getResourceManager().getResource(glowmaskTexture).isPresent() || Minecraft.getInstance().getTextureManager().getTexture(glowmaskTexture, MissingTextureAtlasSprite.getTexture()) != MissingTextureAtlasSprite.getTexture()) {
                    return super.getRenderType(animatable, bufferSource);
                } else {
                    ItemStack stack = getCurrentItemStack();
                    if (stack != null) {
                        MiraculousLookData data = getMiraculousLookData(stack);
                        if (data != null && texture.equals(data.texture())) {
                            data.glowmask().ifPresent(glowmask -> {
                                try {
                                    DynamicAutoGlowingTexture.register(texture, glowmask);
                                } catch (IOException e) {
                                    Mineraculous.LOGGER.error("Failed to register glowmask texture for {}", texture, e);
                                }
                            });
                        }
                    }
                    return null;
                }
            }
        });
    }

    public static void clearModels() {
        DEFAULT_MODELS.clear();
        LOOK_MODELS.clear();
    }

    @Override
    public void preRender(PoseStack poseStack, MiraculousItem animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        if (!isReRender) {
            ItemStack stack = getCurrentItemStack();
            if (stack != null) {
                ResourceKey<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
                MiraculousLookData data = getMiraculousLookData(stack);
                if (data != null && data.transforms().isPresent() && data.transforms().get().hasTransform(renderPerspective) && !stack.has(MineraculousDataComponents.POWERED)) {
                    data.transforms().get().getTransform(renderPerspective).apply(false, poseStack);
                } else {
                    BakedModel miraculousModel = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(miraculous.location().getNamespace(), "item/miraculous/" + miraculous.location().getPath())));
                    if (miraculousModel != Minecraft.getInstance().getModelManager().getMissingModel()) {
                        miraculousModel.applyTransform(renderPerspective, poseStack, false);
                    }
                }
            }
        }
        // Special case for earrings
        model.getBone("right_earring").ifPresent(bone -> bone.setHidden(renderPerspective == MineraculousItemDisplayContexts.CURIOS_EARRINGS.getValue()));
    }

    @Override
    public ResourceLocation getTextureLocation(MiraculousItem animatable) {
        ItemStack stack = getCurrentItemStack();
        if (stack != null) {
            if (stack.has(MineraculousDataComponents.POWERED)) {
                int ticks = stack.getOrDefault(MineraculousDataComponents.REMAINING_TICKS, 0);
                final int second = ticks / SharedConstants.TICKS_PER_SECOND;
                final int minute = ticks / SharedConstants.TICKS_PER_MINUTE + 1;
                ResourceLocation powered = super.getTextureLocation(animatable).withPath(path -> path.replace("hidden", "powered"));
                if (ticks > 0 && ticks < MiraculousItem.FIVE_MINUTES) {
                    // Blinks every other second
                    if (second % 2 == 0)
                        return super.getTextureLocation(animatable).withPath(path -> path.replace("hidden", "powered_" + (minute - 1)));
                    // The first blink level should reference the normal powered model
                    else if (minute == 5) {
                        return powered;
                    } else {
                        return super.getTextureLocation(animatable).withPath(path -> path.replace("hidden", "powered_" + minute));
                    }
                } else {
                    return powered;
                }
            }
        }
        return super.getTextureLocation(animatable);
    }

    @Override
    public GeoModel<MiraculousItem> getGeoModel() {
        ItemStack stack = getCurrentItemStack();
        if (stack != null) {
            ResourceKey<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
            if (miraculous != null) {
                if (!DEFAULT_MODELS.containsKey(miraculous))
                    DEFAULT_MODELS.put(miraculous, createDefaultGeoModel(miraculous));
                MiraculousLookData data = getMiraculousLookData(stack);
                if (data != null && !stack.has(MineraculousDataComponents.POWERED)) {
                    if (!LOOK_MODELS.containsKey(data))
                        LOOK_MODELS.put(data, createLookGeoModel(miraculous, data));
                    return LOOK_MODELS.get(data);
                }
                return DEFAULT_MODELS.get(miraculous);
            }
        }
        return super.getGeoModel();
    }

    private GeoModel<MiraculousItem> createDefaultGeoModel(ResourceKey<Miraculous> miraculous) {
        return new DefaultedItemGeoModel<>(miraculous.location().withPrefix("miraculous/")) {
            private final ResourceLocation texture = miraculous.location().withPrefix("textures/item/miraculous/").withSuffix("/hidden.png");

            @Override
            public ResourceLocation getTextureResource(MiraculousItem animatable) {
                return texture;
            }
        };
    }

    private GeoModel<MiraculousItem> createLookGeoModel(ResourceKey<Miraculous> miraculous, MiraculousLookData data) {
        return new GeoModel<>() {
            private BakedGeoModel currentModel = null;

            @Override
            public ResourceLocation getModelResource(MiraculousItem animatable) {
                return DEFAULT_MODELS.get(miraculous).getModelResource(animatable);
            }

            @Override
            public ResourceLocation getTextureResource(MiraculousItem animatable) {
                return data.texture();
            }

            @Override
            public ResourceLocation getAnimationResource(MiraculousItem animatable) {
                return null;
            }

            @Override
            public BakedGeoModel getBakedModel(ResourceLocation location) {
                BakedGeoModel baked = data.model().orElseGet(() -> super.getBakedModel(location));
                if (currentModel != baked) {
                    currentModel = baked;
                    getAnimationProcessor().setActiveModel(baked);
                }
                return currentModel;
            }
        };
    }

    @Nullable
    private MiraculousLookData getMiraculousLookData(ItemStack stack) {
        ResourceKey<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
        ResolvableProfile profile = getCurrentItemStack().get(DataComponents.PROFILE);
        if (profile != null && Minecraft.getInstance().level != null) {
            Player player = Minecraft.getInstance().level.getPlayerByUUID(profile.id().orElse(profile.gameProfile().getId()));
            if (player != null) {
                String look = player.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous).miraculousLook();
                if (!look.isEmpty()) {
                    return player.getData(MineraculousAttachmentTypes.MIRACULOUS_MIRACULOUS_LOOKS).get(miraculous, look);
                }
            }
        }
        return null;
    }
}
