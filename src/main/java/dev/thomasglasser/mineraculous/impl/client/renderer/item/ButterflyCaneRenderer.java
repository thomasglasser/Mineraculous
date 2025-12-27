package dev.thomasglasser.mineraculous.impl.client.renderer.item;

import dev.thomasglasser.mineraculous.api.client.look.util.renderer.MiraculousToolLookRenderer;
import dev.thomasglasser.mineraculous.api.client.model.LookGeoModel;
import dev.thomasglasser.mineraculous.api.client.renderer.layer.ConditionalAutoGlowingGeoLayer;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContexts;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ButterflyCaneRenderer<T extends Item & GeoAnimatable> extends GeoItemRenderer<T> implements MiraculousToolLookRenderer {
    private final GeoModel<T> model;

    public ButterflyCaneRenderer() {
        super((GeoModel<T>) null);
        addRenderLayer(new ConditionalAutoGlowingGeoLayer<>(this));

        this.model = new LookGeoModel<>(this);
    }

    @Override
    public GeoModel<T> getGeoModel() {
        return this.model;
    }

    @Override
    public Holder<LookContext> getContext() {
        ItemStack stack = getCurrentItemStack();
        if (stack.has(MineraculousDataComponents.BLOCKING))
            return LookContexts.BLOCKING_MIRACULOUS_TOOL;
        return switch (stack.get(MineraculousDataComponents.BUTTERFLY_CANE_MODE)) {
            case PHONE -> LookContexts.PHONE_MIRACULOUS_TOOL;
            case SPYGLASS -> LookContexts.SPYGLASS_MIRACULOUS_TOOL;
            case null, default -> LookContexts.MIRACULOUS_TOOL;
        };
    }
}
