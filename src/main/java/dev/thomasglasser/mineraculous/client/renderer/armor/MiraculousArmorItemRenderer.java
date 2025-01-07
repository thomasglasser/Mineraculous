package dev.thomasglasser.mineraculous.client.renderer.armor;

import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.armor.MiraculousArmorItem;
import dev.thomasglasser.mineraculous.world.level.storage.LookData;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.texture.GeoAbstractTexture;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class MiraculousArmorItemRenderer extends GeoArmorRenderer<MiraculousArmorItem> {
    private final Map<ResourceKey<Miraculous>, GeoModel<MiraculousArmorItem>> defaultModels = new HashMap<>();
    private final Map<LookData, GeoModel<MiraculousArmorItem>> lookModels = new HashMap<>();

    public MiraculousArmorItemRenderer() {
        super(null);
        addRenderLayer(new AutoGlowingGeoLayer<>(this) {
            @Override
            protected @Nullable RenderType getRenderType(MiraculousArmorItem animatable, MultiBufferSource bufferSource) {
                if (getCurrentStack() != null) {
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
    public ResourceLocation getTextureLocation(MiraculousArmorItem animatable) {
        ItemStack stack = getCurrentStack();
        if (stack != null) {
            ResourceKey<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
            if (miraculous != null) {
                Integer transformationTicks = stack.get(MineraculousDataComponents.TRANSFORMATION_FRAMES);
                if (transformationTicks != null && transformationTicks > 0) {
                    ResourceLocation loc = super.getTextureLocation(animatable).withPath(path -> path.replace(".png", "_" + (10 - transformationTicks) + ".png"));
                    if (Minecraft.getInstance().getTextureManager().getTexture(loc, MissingTextureAtlasSprite.getTexture()) == MissingTextureAtlasSprite.getTexture())
                        return super.getTextureLocation(animatable);
                    return loc;
                } else {
                    Integer detransformationTicks = stack.get(MineraculousDataComponents.DETRANSFORMATION_FRAMES);
                    if (detransformationTicks != null && detransformationTicks > 0) {
                        ResourceLocation loc = super.getTextureLocation(animatable).withPath(path -> path.replace(".png", "_" + detransformationTicks + ".png"));
                        if (Minecraft.getInstance().getTextureManager().getTexture(loc, MissingTextureAtlasSprite.getTexture()) == MissingTextureAtlasSprite.getTexture())
                            return super.getTextureLocation(animatable);
                        return loc;
                    }
                }
            }
        }
        return super.getTextureLocation(animatable);
    }

    @Override
    public GeoModel<MiraculousArmorItem> getGeoModel() {
        if (getCurrentStack() != null) {
            ResourceKey<Miraculous> miraculous = getCurrentStack().get(MineraculousDataComponents.MIRACULOUS);
            if (miraculous != null) {
                if (getCurrentEntity() instanceof Player player) {
                    String look = player.getData(MineraculousAttachmentTypes.MIRACULOUS).get(miraculous).look();
                    if (!look.isEmpty()) {
                        LookData data = player.getData(MineraculousAttachmentTypes.MIRACULOUS_LOOKS).get(miraculous, look);
                        if (data != null) {
                            if (!lookModels.containsKey(data))
                                lookModels.put(data, createLookGeoModel(data));
                            return lookModels.get(data);
                        }
                    }
                }
                if (!defaultModels.containsKey(miraculous))
                    defaultModels.put(miraculous, createDefaultGeoModel(miraculous));
                return defaultModels.get(miraculous);
            }
        }
        return super.getGeoModel();
    }

    private GeoModel<MiraculousArmorItem> createDefaultGeoModel(ResourceKey<Miraculous> miraculous) {
        return new DefaultedItemGeoModel<>(ResourceLocation.fromNamespaceAndPath(miraculous.location().getNamespace(), "armor/miraculous/" + miraculous.location().getPath())) {
            private final ResourceLocation textureLoc = ResourceLocation.fromNamespaceAndPath(miraculous.location().getNamespace(), "textures/entity/equipment/humanoid/miraculous/" + miraculous.location().getPath() + ".png");

            @Override
            public ResourceLocation getTextureResource(MiraculousArmorItem animatable) {
                return textureLoc;
            }
        };
    }

    private GeoModel<MiraculousArmorItem> createLookGeoModel(LookData data) {
        return new GeoModel<>() {
            private BakedGeoModel currentModel = null;

            @Override
            public ResourceLocation getModelResource(MiraculousArmorItem animatable) {
                return null;
            }

            @Override
            public ResourceLocation getTextureResource(MiraculousArmorItem animatable) {
                return data.texture();
            }

            @Override
            public ResourceLocation getAnimationResource(MiraculousArmorItem animatable) {
                return null;
            }

            @Override
            public BakedGeoModel getBakedModel(ResourceLocation location) {
                BakedGeoModel baked = data.model();
                if (currentModel != baked) {
                    currentModel = baked;
                    getAnimationProcessor().setActiveModel(baked);
                }
                return currentModel;
            }
        };
    }
}
