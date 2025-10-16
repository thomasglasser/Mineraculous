package dev.thomasglasser.mineraculous.impl.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.KwamiRenderer;
import dev.thomasglasser.mineraculous.impl.world.item.KwamiItem;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.Map;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class KwamiItemRenderer extends GeoItemRenderer<KwamiItem> {
    private final Map<Holder<Miraculous>, GeoModel<KwamiItem>> models = new Reference2ReferenceOpenHashMap<>();

    public KwamiItemRenderer() {
        super((GeoModel<KwamiItem>) null);
    }

    @Override
    public void defaultRender(PoseStack poseStack, KwamiItem animatable, MultiBufferSource bufferSource, @Nullable RenderType renderType, @Nullable VertexConsumer buffer, float yaw, float partialTick, int packedLight) {
        super.defaultRender(poseStack, animatable, bufferSource, renderType, buffer, yaw, partialTick, LightTexture.FULL_BRIGHT);
    }

    @Override
    public @Nullable RenderType getRenderType(KwamiItem animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return NeoForgeRenderTypes.getUnlitTranslucent(texture);
    }

    @Override
    public GeoModel<KwamiItem> getGeoModel() {
        Holder<Miraculous> miraculous = MiraculousItemRenderer.getMiraculousOrDefault(getCurrentItemStack());
        if (!models.containsKey(miraculous))
            models.put(miraculous, KwamiRenderer.createGeoModel(miraculous, item -> getCurrentItemStack().getOrDefault(MineraculousDataComponents.CHARGED, true)));
        return models.get(miraculous);
    }
}
