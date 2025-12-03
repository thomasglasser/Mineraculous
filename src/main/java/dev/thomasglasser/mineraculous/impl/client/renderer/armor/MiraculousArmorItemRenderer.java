package dev.thomasglasser.mineraculous.impl.client.renderer.armor;

import dev.thomasglasser.mineraculous.api.client.renderer.layer.ConditionalAutoGlowingGeoLayer;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.item.armor.MiraculousArmorItem;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.client.renderer.item.MiraculousItemRenderer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class MiraculousArmorItemRenderer<T extends MiraculousArmorItem> extends GeoArmorRenderer<T> {
    private static final Map<Holder<Miraculous>, GeoModel<?>> DEFAULT_MODELS = new Reference2ReferenceOpenHashMap<>();
    private static final Map<ResourceLocation, Int2ObjectMap<ResourceLocation>> FRAME_TEXTURES = new Object2ReferenceOpenHashMap<>();

    public MiraculousArmorItemRenderer() {
        super((GeoModel<T>) null);
        addRenderLayer(new ConditionalAutoGlowingGeoLayer<>(this));
    }

    public static void clearAssets() {
        DEFAULT_MODELS.clear();
        FRAME_TEXTURES.clear();
    }

    @Override
    public ResourceLocation getTextureLocation(T animatable) {
        ResourceLocation base = super.getTextureLocation(animatable);
        ItemStack stack = getCurrentStack();
        Holder<Miraculous> miraculous = MiraculousItemRenderer.getMiraculousOrDefault(stack);
        Optional<Integer> transformationFrames = miraculous.value().transformationFrames();
        if (transformationFrames.isPresent()) {
            Integer transformationTicks = stack.get(MineraculousDataComponents.TRANSFORMATION_FRAMES);
            Integer detransformationTicks = stack.get(MineraculousDataComponents.DETRANSFORMATION_FRAMES);
            int frame = transformationTicks == null ? detransformationTicks == null ? -1 : detransformationTicks : transformationFrames.get() - transformationTicks;
            if (frame >= 0) {
                ResourceLocation texture = FRAME_TEXTURES.computeIfAbsent(base, loc -> new Int2ObjectOpenHashMap<>()).computeIfAbsent(frame, i -> base.withPath(path -> path.replace(".png", "_" + i + ".png")));
                if (MineraculousClientUtils.isValidTexture(texture))
                    return texture;
            }
        }
        return base;
    }

    @Override
    public GeoModel<T> getGeoModel() {
        return (GeoModel<T>) DEFAULT_MODELS.computeIfAbsent(MiraculousItemRenderer.getMiraculousOrDefault(getCurrentStack()), this::createDefaultGeoModel);
    }

    private GeoModel<T> createDefaultGeoModel(Holder<Miraculous> miraculous) {
        return new DefaultedItemGeoModel<>(miraculous.getKey().location().withPrefix("armor/miraculous/")) {
            private final ResourceLocation texture = miraculous.getKey().location().withPath(path -> "textures/entity/equipment/humanoid/miraculous/" + path + ".png");

            @Override
            public ResourceLocation getTextureResource(T animatable) {
                return texture;
            }

            @Override
            public @Nullable Animation getAnimation(T animatable, String name) {
                try {
                    return super.getAnimation(animatable, name);
                } catch (RuntimeException e) {
                    return null;
                }
            }
        };
    }
}
