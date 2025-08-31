package dev.thomasglasser.mineraculous.impl.client.renderer.armor;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.world.item.armor.MiraculousArmorItem;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.cache.texture.AutoGlowingTexture;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class MiraculousArmorItemRenderer extends GeoArmorRenderer<MiraculousArmorItem> {
    private static final Map<Holder<Miraculous>, GeoModel<MiraculousArmorItem>> DEFAULT_MODELS = new Reference2ReferenceOpenHashMap<>();
    private static final Object2BooleanMap<ResourceLocation> PRESENT_GLOWMASKS = new Object2BooleanOpenHashMap<>();

    public MiraculousArmorItemRenderer() {
        super(null);
        addRenderLayer(new AutoGlowingGeoLayer<>(this) {
            @Override
            protected @Nullable RenderType getRenderType(MiraculousArmorItem animatable, @Nullable MultiBufferSource bufferSource) {
                ResourceLocation glowmaskTexture = AutoGlowingTexture.appendToPath(getTextureLocation(animatable), "_glowmask");
                if (PRESENT_GLOWMASKS.computeIfAbsent(glowmaskTexture, (ResourceLocation texture) -> Minecraft.getInstance().getResourceManager().getResource(texture).isPresent() || Minecraft.getInstance().getTextureManager().getTexture(texture, MissingTextureAtlasSprite.getTexture()) != MissingTextureAtlasSprite.getTexture())) {
                    return super.getRenderType(animatable, bufferSource);
                } else {
                    return null;
                }
            }
        });
    }

    public static void clearModels() {
        DEFAULT_MODELS.clear();
        PRESENT_GLOWMASKS.clear();
    }

    @Override
    public ResourceLocation getTextureLocation(MiraculousArmorItem animatable) {
        ItemStack stack = getCurrentStack();
        if (stack != null) {
            Holder<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
            Entity entity = getCurrentEntity();
            if (miraculous != null && entity != null) {
                Optional<Integer> transformationFrames = miraculous.value().transformationFrames();
                if (transformationFrames.isPresent()) {
                    Integer transformationTicks = stack.get(MineraculousDataComponents.TRANSFORMATION_FRAMES);
                    Integer detransformationTicks = stack.get(MineraculousDataComponents.DETRANSFORMATION_FRAMES);
                    int frame = transformationTicks == null ? detransformationTicks == null ? -1 : detransformationTicks : transformationFrames.get() - transformationTicks;
                    if (frame >= 0) {
                        ResourceLocation texture = super.getTextureLocation(animatable).withPath(path -> path.replace(".png", "_" + frame + ".png"));
                        if (Minecraft.getInstance().getResourceManager().getResource(texture).isPresent() || Minecraft.getInstance().getTextureManager().getTexture(texture, MissingTextureAtlasSprite.getTexture()) != MissingTextureAtlasSprite.getTexture())
                            return texture;
                    }
                }
            }
        }
        return super.getTextureLocation(animatable);
    }

    @Override
    public GeoModel<MiraculousArmorItem> getGeoModel() {
        if (getCurrentStack() != null) {
            Holder<Miraculous> miraculous = getCurrentStack().get(MineraculousDataComponents.MIRACULOUS);
            if (miraculous != null) {
                if (!DEFAULT_MODELS.containsKey(miraculous))
                    DEFAULT_MODELS.put(miraculous, createDefaultGeoModel(miraculous));
                return DEFAULT_MODELS.get(miraculous);
            }
        }
        return super.getGeoModel();
    }

    private GeoModel<MiraculousArmorItem> createDefaultGeoModel(Holder<Miraculous> miraculous) {
        return new DefaultedItemGeoModel<>(ResourceLocation.fromNamespaceAndPath(miraculous.getKey().location().getNamespace(), "armor/miraculous/" + miraculous.getKey().location().getPath())) {
            private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(miraculous.getKey().location().getNamespace(), "textures/entity/equipment/humanoid/miraculous/" + miraculous.getKey().location().getPath() + ".png");

            @Override
            public ResourceLocation getTextureResource(MiraculousArmorItem animatable) {
                return texture;
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
}
