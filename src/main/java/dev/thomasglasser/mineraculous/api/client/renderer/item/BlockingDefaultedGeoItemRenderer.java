package dev.thomasglasser.mineraculous.api.client.renderer.item;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.tommylib.api.client.renderer.item.DefaultedGeoItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;

/**
 * A {@link DefaultedGeoItemRenderer} with a separate model and texture when blocking,
 * based on the {@link MineraculousDataComponents#BLOCKING} component being present.
 * 
 * @param <T> The item the renderer is for
 */
public class BlockingDefaultedGeoItemRenderer<T extends Item & GeoAnimatable> extends DefaultedGeoItemRenderer<T> {
    private final GeoModel<T> blockingModel;
    private final ResourceLocation blockingTexture;

    public BlockingDefaultedGeoItemRenderer(ResourceLocation id) {
        super(id);
        this.blockingModel = new DefaultedItemGeoModel<>(id.withSuffix("_blocking"));
        this.blockingTexture = id.withPrefix("textures/item/geo/").withSuffix("_blocking.png");
    }

    public boolean isBlocking() {
        return getCurrentItemStack().has(MineraculousDataComponents.BLOCKING);
    }

    public GeoModel<T> getBlockingModel() {
        return blockingModel;
    }

    public ResourceLocation getBlockingTextureLocation() {
        return blockingTexture;
    }

    @Override
    public GeoModel<T> getGeoModel() {
        if (isBlocking())
            return getBlockingModel();
        return super.getGeoModel();
    }

    @Override
    public ResourceLocation getTextureLocation(T animatable) {
        if (isBlocking())
            return getBlockingTextureLocation();
        return super.getTextureLocation(animatable);
    }
}
