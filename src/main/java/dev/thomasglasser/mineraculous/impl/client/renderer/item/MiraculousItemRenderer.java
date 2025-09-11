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
    private static final Map<Holder<Miraculous>, GeoModel<MiraculousItem>> DEFAULT_MODELS = new Reference2ReferenceOpenHashMap<>();

    public MiraculousItemRenderer() {
        super(null);
        addRenderLayer(new ConditionalAutoGlowingGeoLayer<>(this));
    }

    public static void clearModels() {
        DEFAULT_MODELS.clear();
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

    @Override
    public ResourceLocation getTextureLocation(MiraculousItem animatable) {
        ItemStack stack = getCurrentItemStack();
        if (stack != null) {
            if (stack.has(MineraculousDataComponents.POWERED)) {
                ResourceLocation powered = super.getTextureLocation(animatable).withPath(path -> path.replace("hidden", "powered"));
                Integer remainingTicks = stack.get(MineraculousDataComponents.REMAINING_TICKS);
                if (remainingTicks != null) {
                    int seconds = remainingTicks / SharedConstants.TICKS_PER_SECOND;
                    int maxSeconds = MineraculousServerConfig.get().miraculousTimerDuration.get();
                    int threshold = Math.max(maxSeconds / 5, 1);
                    int frame = seconds / threshold + 1;
                    if (seconds % 2 == 0) {
                        // Blinks every other second
                        return super.getTextureLocation(animatable).withPath(path -> path.replace("hidden", "powered_" + (Math.max(frame - 1, 0))));
                    } else if (frame >= 5) {
                        // The first blink level should reference the normal powered model
                        return powered;
                    } else {
                        return super.getTextureLocation(animatable).withPath(path -> path.replace("hidden", "powered_" + frame));
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
