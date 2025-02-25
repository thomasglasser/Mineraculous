package dev.thomasglasser.mineraculous.client.renderer.armor;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.DynamicAutoGlowingTexture;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.armor.MiraculousArmorItem;
import dev.thomasglasser.mineraculous.world.level.storage.SuitLookData;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.texture.AutoGlowingTexture;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class MiraculousArmorItemRenderer extends GeoArmorRenderer<MiraculousArmorItem> {
    private final Map<ResourceKey<Miraculous>, GeoModel<MiraculousArmorItem>> defaultModels = new HashMap<>();
    private final Map<SuitLookData, GeoModel<MiraculousArmorItem>> lookModels = new HashMap<>();

    public MiraculousArmorItemRenderer() {
        super(null);
        addRenderLayer(new AutoGlowingGeoLayer<>(this) {
            @Override
            protected @Nullable RenderType getRenderType(MiraculousArmorItem animatable, @Nullable MultiBufferSource bufferSource) {
                ResourceLocation texture = getTextureLocation(animatable);
                ResourceLocation glowmaskTexture = AutoGlowingTexture.appendToPath(texture, "_glowmask");
                if (Minecraft.getInstance().getTextureManager().getTexture(glowmaskTexture, MissingTextureAtlasSprite.getTexture()) == MissingTextureAtlasSprite.getTexture()) {
                    if (Minecraft.getInstance().getResourceManager().getResource(glowmaskTexture).isPresent())
                        return super.getRenderType(animatable, bufferSource);
                    else if (getCurrentStack() != null && getCurrentEntity() instanceof LivingEntity livingEntity) {
                        String look = livingEntity.getData(MineraculousAttachmentTypes.MIRACULOUS).get(getCurrentStack().get(MineraculousDataComponents.MIRACULOUS)).suitLook();
                        SuitLookData data = livingEntity.getData(MineraculousAttachmentTypes.MIRACULOUS_SUIT_LOOKS).get(getCurrentStack().get(MineraculousDataComponents.MIRACULOUS), look);
                        if (data != null) {
                            if (data.glowmask().isPresent() && texture.equals(data.texture())) {
                                byte[] glowmask = data.glowmask().get();
                                try {
                                    DynamicAutoGlowingTexture.register(texture, glowmask);
                                } catch (IOException e) {
                                    Mineraculous.LOGGER.error("Failed to register glowmask texture for {}", texture, e);
                                }
                            }
                            if (!data.glowmaskFrames().isEmpty()) {
                                try {
                                    byte[] glowmaskFrame = data.glowmaskFrames().get(data.frames().indexOf(texture));
                                    DynamicAutoGlowingTexture.register(texture, glowmaskFrame);
                                } catch (IndexOutOfBoundsException e) {
                                    return null;
                                } catch (IOException e) {
                                    Mineraculous.LOGGER.error("Failed to register glowmask frame texture for {}", texture, e);
                                }
                            }
                        }
                    }
                    return null;
                }
                return super.getRenderType(animatable, bufferSource);
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
                    if (Minecraft.getInstance().getResourceManager().getResource(loc).isEmpty() && Minecraft.getInstance().getTextureManager().getTexture(loc, MissingTextureAtlasSprite.getTexture()) == MissingTextureAtlasSprite.getTexture())
                        return super.getTextureLocation(animatable);
                    return loc;
                } else {
                    Integer detransformationTicks = stack.get(MineraculousDataComponents.DETRANSFORMATION_FRAMES);
                    if (detransformationTicks != null && detransformationTicks > 0) {
                        ResourceLocation loc = super.getTextureLocation(animatable).withPath(path -> path.replace(".png", "_" + detransformationTicks + ".png"));
                        if (Minecraft.getInstance().getResourceManager().getResource(loc).isEmpty() && Minecraft.getInstance().getTextureManager().getTexture(loc, MissingTextureAtlasSprite.getTexture()) == MissingTextureAtlasSprite.getTexture())
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
                if (!defaultModels.containsKey(miraculous))
                    defaultModels.put(miraculous, createDefaultGeoModel(miraculous));
                if (getCurrentEntity() instanceof Player player) {
                    String look = player.getData(MineraculousAttachmentTypes.MIRACULOUS).get(miraculous).suitLook();
                    if (!look.isEmpty()) {
                        SuitLookData data = player.getData(MineraculousAttachmentTypes.MIRACULOUS_SUIT_LOOKS).get(miraculous, look);
                        if (data != null) {
                            if (!lookModels.containsKey(data))
                                lookModels.put(data, createLookGeoModel(miraculous, data));
                            return lookModels.get(data);
                        }
                    }
                }
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

            @Override
            public @Nullable Animation getAnimation(MiraculousArmorItem animatable, String name) {
                try {
                    return super.getAnimation(animatable, name);
                } catch (RuntimeException e) {
                    return null;
                }
            }
        };
    }

    private GeoModel<MiraculousArmorItem> createLookGeoModel(ResourceKey<Miraculous> miraculous, SuitLookData data) {
        return new GeoModel<>() {
            private BakedGeoModel currentModel = null;

            @Override
            public ResourceLocation getModelResource(MiraculousArmorItem animatable) {
                return defaultModels.get(miraculous).getModelResource(animatable);
            }

            @Override
            public ResourceLocation getTextureResource(MiraculousArmorItem animatable) {
                return data.texture();
            }

            @Override
            public ResourceLocation getAnimationResource(MiraculousArmorItem animatable) {
                return defaultModels.get(miraculous).getAnimationResource(animatable);
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

            @Override
            public @Nullable Animation getAnimation(MiraculousArmorItem animatable, String name) {
                try {
                    if (data.animations().isPresent()) {
                        return data.animations().get().getAnimation(name);
                    }
                    return super.getAnimation(animatable, name);
                } catch (RuntimeException e) {
                    return null;
                }
            }
        };
    }
}
