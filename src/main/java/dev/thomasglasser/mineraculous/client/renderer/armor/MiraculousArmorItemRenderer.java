package dev.thomasglasser.mineraculous.client.renderer.armor;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.item.armor.MiraculousArmorItem;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.texture.AutoGlowingTexture;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class MiraculousArmorItemRenderer extends GeoArmorRenderer<MiraculousArmorItem> {
    private final String miraculous;
    private final Map<String, GeoModel<MiraculousArmorItem>> playerModels = new HashMap<>();

    public MiraculousArmorItemRenderer(final String miraculous) {
        super(new DefaultedItemGeoModel<>(Mineraculous.modLoc("armor/miraculous/" + miraculous + "_miraculous_suit_default")) {
            private final ResourceLocation texture = Mineraculous.modLoc("textures/models/armor/miraculous/" + miraculous + "_miraculous_suit_default.png");

            @Override
            public ResourceLocation getTextureResource(MiraculousArmorItem animatable) {
                return texture;
            }
        });
        this.miraculous = miraculous;
        addRenderLayer(new AutoGlowingGeoLayer<>(this) {
            @Override
            protected RenderType getRenderType(MiraculousArmorItem animatable) {
                return RenderType.eyes(AutoGlowingTexture.getEmissiveResource(getTextureResource(animatable)));
            }
        });
    }

    @Override
    public GeoModel<MiraculousArmorItem> getGeoModel() {
        if (getCurrentEntity() instanceof Player player) {
            if (!playerModels.containsKey(player.getGameProfile().getName().toLowerCase())) {
                playerModels.put(player.getGameProfile().getName().toLowerCase(), new DefaultedItemGeoModel<>(Mineraculous.modLoc("armor/miraculous/" + miraculous + "_miraculous_suit_" + player.getGameProfile().getName().toLowerCase())) {
                    private final ResourceLocation textureLoc = Mineraculous.modLoc("textures/models/armor/miraculous/" + miraculous + "_miraculous_suit_" + player.getGameProfile().getName().toLowerCase() + ".png");

                    @Override
                    public ResourceLocation getTextureResource(MiraculousArmorItem animatable) {
                        if (Minecraft.getInstance().getTextureManager().getTexture(textureLoc) != MissingTextureAtlasSprite.getTexture())
                            return textureLoc;
                        return model.getTextureResource(animatable);
                    }

                    @Override
                    public ResourceLocation getModelResource(MiraculousArmorItem animatable) {
                        if (GeckoLibCache.getBakedModels().containsKey(super.getModelResource(animatable)))
                            return super.getModelResource(animatable);
                        return model.getModelResource(animatable);
                    }

                    @Override
                    public ResourceLocation getAnimationResource(MiraculousArmorItem animatable) {
                        if (GeckoLibCache.getBakedAnimations().containsKey(super.getAnimationResource(animatable)))
                            return super.getAnimationResource(animatable);
                        return model.getAnimationResource(animatable);
                    }
                });
            }
            return playerModels.get(player.getGameProfile().getName().toLowerCase());
        }
        return super.getGeoModel();
    }
}
