package dev.thomasglasser.mineraculous.impl.client.renderer.item;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.KwamiRenderer;
import dev.thomasglasser.mineraculous.impl.world.item.KwamiItem;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class KwamiItemRenderer extends GeoItemRenderer<KwamiItem> {
    private final Map<Holder<Miraculous>, GeoModel<KwamiItem>> models = new Reference2ReferenceOpenHashMap<>();

    public KwamiItemRenderer() {
        super((GeoModel<KwamiItem>) null);
    }

    @Override
    public GeoModel<KwamiItem> getGeoModel() {
        Holder<Miraculous> miraculous = MiraculousItemRenderer.getMiraculousOrDefault(getCurrentItemStack());
        if (!models.containsKey(miraculous))
            models.put(miraculous, KwamiRenderer.createGeoModel(miraculous, item -> getCurrentItemStack().getOrDefault(MineraculousDataComponents.CHARGED, true)));
        return models.get(miraculous);
    }
}
