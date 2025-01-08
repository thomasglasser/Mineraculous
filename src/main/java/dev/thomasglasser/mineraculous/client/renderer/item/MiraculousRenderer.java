package dev.thomasglasser.mineraculous.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.MineraculousItemDisplayContexts;
import dev.thomasglasser.mineraculous.world.item.MiraculousItem;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.texture.GeoAbstractTexture;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class MiraculousRenderer extends GeoItemRenderer<MiraculousItem> {
    private final Map<ResourceKey<Miraculous>, GeoModel<MiraculousItem>> defaultModels = new HashMap<>();

    public MiraculousRenderer() {
        super(null);
        addRenderLayer(new AutoGlowingGeoLayer<>(this) {
            @Override
            protected @Nullable RenderType getRenderType(MiraculousItem animatable, MultiBufferSource bufferSource) {
                if (getCurrentItemStack() != null) {
                    ResourceLocation glowMask = GeoAbstractTexture.appendToPath(getTextureResource(animatable), "_glowmask");
                    if (Minecraft.getInstance().getResourceManager().getResource(glowMask).isPresent()) {
                        return super.getRenderType(animatable, bufferSource);
                    }
                }
                return null;
            }
        });
    }

    @Override
    public void preRender(PoseStack poseStack, MiraculousItem animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        if (getCurrentItemStack() != null) {
            ResourceKey<Miraculous> miraculous = getCurrentItemStack().get(MineraculousDataComponents.MIRACULOUS);
            BakedModel miraculousModel = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(miraculous.location().getNamespace(), "item/miraculous/" + miraculous.location().getPath())));
            if (miraculousModel != Minecraft.getInstance().getModelManager().getMissingModel()) {
                miraculousModel.applyTransform(renderPerspective, poseStack, false);
            }
            if (renderPerspective == MineraculousItemDisplayContexts.CURIOS_EARRINGS.getValue())
                model.getBone("right").orElseThrow().setHidden(renderPerspective == MineraculousItemDisplayContexts.CURIOS_EARRINGS.getValue());
        }
    }

    @Override
    public ResourceLocation getTextureLocation(MiraculousItem animatable) {
        ItemStack stack = getCurrentItemStack();
        if (stack != null) {
            if (stack.has(MineraculousDataComponents.POWERED.get())) {
                int ticks = stack.getOrDefault(MineraculousDataComponents.REMAINING_TICKS.get(), 0);
                final int second = ticks / 20;
                final int minute = (second / 60) + 1;
                ResourceLocation powered = super.getTextureLocation(animatable).withPath(path -> path.replace("hidden", "powered"));
                if (ticks > 0 && ticks < MiraculousItem.FIVE_MINUTES) {
                    // Blinks every other second
                    if (second % 2 == 0)
                        return super.getTextureLocation(animatable).withPath(path -> path.replace("hidden", "powered_" + (minute - 1)));
                    // The first blink level should reference the normal powered model
                    else if (minute == 5) {
                        return powered;
                    } else
                        return super.getTextureLocation(animatable).withPath(path -> path.replace("hidden", "powered_" + minute));
                } else
                    return powered;
            } else {
                // TODO: Look dependent
            }
        }
        return super.getTextureLocation(animatable);
    }

    @Override
    public GeoModel<MiraculousItem> getGeoModel() {
        if (getCurrentItemStack() != null) {
            ResourceKey<Miraculous> miraculous = getCurrentItemStack().get(MineraculousDataComponents.MIRACULOUS);
            if (miraculous != null) {
//                if (getCurrentEntity() instanceof Player player) {
//                    String look = player.getData(MineraculousAttachmentTypes.MIRACULOUS).get(miraculous).look();
//                    if (!look.isEmpty()) {
//                        SuitLookData data = player.getData(MineraculousAttachmentTypes.MIRACULOUS_SUIT_LOOKS).get(miraculous, look);
//                        if (data != null) {
//                            if (!lookModels.containsKey(data))
//                                lookModels.put(data, createLookGeoModel(data));
//                            return lookModels.get(data);
//                        }
//                    }
//                }
                if (!defaultModels.containsKey(miraculous))
                    defaultModels.put(miraculous, createDefaultGeoModel(miraculous));
                return defaultModels.get(miraculous);
            }
        }
        return super.getGeoModel();
    }

    private GeoModel<MiraculousItem> createDefaultGeoModel(ResourceKey<Miraculous> miraculous) {
        return new DefaultedItemGeoModel<>(ResourceLocation.fromNamespaceAndPath(miraculous.location().getNamespace(), "miraculous/" + miraculous.location().getPath())) {
            private final ResourceLocation textureLoc = ResourceLocation.fromNamespaceAndPath(miraculous.location().getNamespace(), "textures/item/miraculous/" + miraculous.location().getPath() + "/hidden.png");

            @Override
            public ResourceLocation getTextureResource(MiraculousItem animatable) {
                return textureLoc;
            }
        };
    }

//    private GeoModel<MiraculousArmorItem> createLookGeoModel(SuitLookData data) {
//        return new GeoModel<>() {
//            private BakedGeoModel currentModel = null;
//
//            @Override
//            public ResourceLocation getModelResource(MiraculousArmorItem animatable) {
//                return null;
//            }
//
//            @Override
//            public ResourceLocation getTextureResource(MiraculousArmorItem animatable) {
//                return data.texture();
//            }
//
//            @Override
//            public ResourceLocation getAnimationResource(MiraculousArmorItem animatable) {
//                return null;
//            }
//
//            @Override
//            public BakedGeoModel getBakedModel(ResourceLocation location) {
//                BakedGeoModel baked = data.model();
//                if (currentModel != baked) {
//                    currentModel = baked;
//                    getAnimationProcessor().setActiveModel(baked);
//                }
//                return currentModel;
//            }
//        };
//    }
}
