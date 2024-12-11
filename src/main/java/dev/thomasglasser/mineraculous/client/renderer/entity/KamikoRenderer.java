package dev.thomasglasser.mineraculous.client.renderer.entity;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.specialty.DynamicGeoEntityRenderer;

public class KamikoRenderer<T extends Kamiko> extends DynamicGeoEntityRenderer<T> {
    private static final ResourceLocation KAMIKO_LOCATION = Mineraculous.modLoc("textures/entity/kamiko.png");
    private static final ResourceLocation KAMIKO_POWERED_LOCATION = Mineraculous.modLoc("textures/entity/kamiko_powered.png");

    public KamikoRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(Mineraculous.modLoc("kamiko")));
    }

    @Override
    public ResourceLocation getTextureLocation(T kamiko) {
        return kamiko.isPowered() ? KAMIKO_POWERED_LOCATION : KAMIKO_LOCATION;
    }
}
