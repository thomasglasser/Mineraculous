package dev.thomasglasser.mineraculous.mixin.minecraft.client.renderer.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.platform.NativeImage;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @ModifyExpressionValue(method = "getRenderType", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getTextureLocation(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation getRenderType(ResourceLocation original, LivingEntity livingEntity, boolean bodyVisible, boolean translucent, boolean glowing) {
        if (MineraculousEntityEvents.isCataclysmed(livingEntity)) {
            int cataclysmColor = 0x201915FF;
            ResourceLocation result = ResourceLocation.fromNamespaceAndPath(original.getNamespace(), original.getPath() + "_cataclysmed");
            try (AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(original)) {
                NativeImage image;
                if (texture instanceof SimpleTexture simpleTexture) {
                    image = simpleTexture.getTextureImage(Minecraft.getInstance().getResourceManager()).getImage();
                } else if (texture instanceof DynamicTexture dynamicTexture) {
                    image = dynamicTexture.getPixels();
                } else {
                    return original;
                }
                if (image != null) {
                    List<Vector2i> nonEmpty = new ArrayList<>();

                    int width = image.getWidth();
                    int height = image.getHeight();
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            if (image.getPixelRGBA(x, y) != 0x00000000 && image.getPixelRGBA(x, y) != cataclysmColor)
                                nonEmpty.add(new Vector2i(x, y));
                        }
                    }

                    int pixelsToCataclysm = (int) (nonEmpty.size() * (((livingEntity.getMaxHealth() - livingEntity.getHealth()) / livingEntity.getMaxHealth())));
                    for (int i = 0; i < pixelsToCataclysm; i++) {
                        Vector2i pixel = nonEmpty.get(i);
                        image.setPixelRGBA(pixel.x, pixel.y, cataclysmColor);
                    }
                    Minecraft.getInstance().getTextureManager().register(result, new DynamicTexture(image));
                    return result;
                }
            } catch (Exception e) {
                return original;
            }
        }
        return original;
    }
}
