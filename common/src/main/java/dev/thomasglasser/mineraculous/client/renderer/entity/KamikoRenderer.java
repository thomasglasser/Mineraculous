package dev.thomasglasser.mineraculous.client.renderer.entity;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.model.KamikoModel;
import dev.thomasglasser.mineraculous.client.model.geom.MineraculousModelLayers;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class KamikoRenderer extends MobRenderer<Kamiko, KamikoModel> {
    private static final ResourceLocation KAMIKO_LOCATION = new ResourceLocation(Mineraculous.MOD_ID, "textures/entity/kamiko.png");
    private static final ResourceLocation KAMIKO_POWERED_LOCATION = new ResourceLocation(Mineraculous.MOD_ID, "textures/entity/kamiko_powered.png");

    public KamikoRenderer(EntityRendererProvider.Context context) {
        super(context, new KamikoModel(context.bakeLayer(MineraculousModelLayers.KAMIKO)), 0.25F);
    }

    public ResourceLocation getTextureLocation(Kamiko entity) {
        return entity.isPowered()?KAMIKO_POWERED_LOCATION:KAMIKO_LOCATION;
    }
}
