package dev.thomasglasser.mineraculous.impl.client.renderer.armor;

import dev.thomasglasser.mineraculous.api.client.renderer.layer.ConditionalAutoGlowingGeoLayer;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.world.item.armor.MiraculousArmorItem;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class MiraculousArmorItemRenderer extends GeoArmorRenderer<MiraculousArmorItem> {
    private static final Map<Holder<Miraculous>, GeoModel<MiraculousArmorItem>> DEFAULT_MODELS = new Reference2ReferenceOpenHashMap<>();

    public MiraculousArmorItemRenderer() {
        super(null);
        addRenderLayer(new ConditionalAutoGlowingGeoLayer<>(this));
    }

    public static void clearModels() {
        DEFAULT_MODELS.clear();
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
        Holder<Miraculous> miraculous = getCurrentStack().get(MineraculousDataComponents.MIRACULOUS);
        Level level = ClientUtils.getLevel();
        if (miraculous == null && level != null) {
            miraculous = level.registryAccess().registryOrThrow(MineraculousRegistries.MIRACULOUS).getAny().orElse(null);
        }
        if (miraculous != null) {
            if (!DEFAULT_MODELS.containsKey(miraculous))
                DEFAULT_MODELS.put(miraculous, createDefaultGeoModel(miraculous));
            return DEFAULT_MODELS.get(miraculous);
        }
        throw new IllegalStateException("Tried to render a Miraculous Armor Item without any registered miraculous");
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
