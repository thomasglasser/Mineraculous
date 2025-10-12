package dev.thomasglasser.mineraculous.impl.client.renderer.armor;

import dev.thomasglasser.mineraculous.api.client.renderer.layer.ConditionalAutoGlowingGeoLayer;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.impl.world.item.armor.KamikotizationArmorItem;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class KamikotizationArmorItemRenderer extends GeoArmorRenderer<KamikotizationArmorItem> {
    private static final Map<Holder<Kamikotization>, GeoModel<KamikotizationArmorItem>> DEFAULT_MODELS = new Reference2ReferenceOpenHashMap<>();

    public KamikotizationArmorItemRenderer() {
        super(null);
        addRenderLayer(new ConditionalAutoGlowingGeoLayer<>(this));
    }

    public static Holder<Kamikotization> getKamikotizationOrDefault(ItemStack stack) {
        Holder<Kamikotization> kamikotization = stack.get(MineraculousDataComponents.KAMIKOTIZATION);
        Level level = ClientUtils.getLevel();
        if (kamikotization == null && level != null) {
            kamikotization = level.registryAccess().registryOrThrow(MineraculousRegistries.KAMIKOTIZATION).getAny().orElse(null);
        }
        if (kamikotization == null) {
            throw new IllegalStateException("Tried to render a Kamikotization related item without any registered kamikotizations");
        }
        return kamikotization;
    }

    public static void clearModels() {
        DEFAULT_MODELS.clear();
    }

    @Override
    public GeoModel<KamikotizationArmorItem> getGeoModel() {
        return DEFAULT_MODELS.computeIfAbsent(getKamikotizationOrDefault(getCurrentStack()), this::createDefaultGeoModel);
    }

    private GeoModel<KamikotizationArmorItem> createDefaultGeoModel(Holder<Kamikotization> kamikotization) {
        return new DefaultedItemGeoModel<>(kamikotization.getKey().location().withPrefix("armor/kamikotization/")) {
            private final ResourceLocation texture = kamikotization.getKey().location().withPath(path -> "textures/entity/equipment/humanoid/kamikotization/" + path + ".png");

            @Override
            public ResourceLocation getTextureResource(KamikotizationArmorItem animatable) {
                return texture;
            }

            @Override
            public @Nullable Animation getAnimation(KamikotizationArmorItem animatable, String name) {
                try {
                    return super.getAnimation(animatable, name);
                } catch (RuntimeException e) {
                    return null;
                }
            }
        };
    }
}
