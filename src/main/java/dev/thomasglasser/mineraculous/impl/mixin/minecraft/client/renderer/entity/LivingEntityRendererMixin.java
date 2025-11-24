package dev.thomasglasser.mineraculous.impl.mixin.minecraft.client.renderer.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.platform.NativeImage;
import dev.thomasglasser.mineraculous.api.world.effect.MineraculousMobEffects;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Unique
    private static final RandomSource CATACLYSM_RANDOM = RandomSource.create();
    @Unique
    private static final List<Vector2i> NON_EMPTY_PIXELS = new ReferenceArrayList<>();

    @Unique
    private int mineraculous$lastEntityId;
    @Unique
    private float mineraculous$lastHealth;

    @ModifyExpressionValue(method = "getRenderType", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getTextureLocation(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation applyCataclysmPixels(ResourceLocation original, LivingEntity livingEntity) {
        if (livingEntity.hasEffect(MineraculousMobEffects.CATACLYSM)) {
            ResourceLocation result = ResourceLocation.fromNamespaceAndPath(original.getNamespace(), original.getPath() + "_cataclysmed");
            if (mineraculous$lastEntityId != livingEntity.getId() || mineraculous$lastHealth != livingEntity.getHealth()) {
                mineraculous$lastEntityId = livingEntity.getId();
                mineraculous$lastHealth = livingEntity.getHealth();
                try (AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(original)) {
                    NativeImage image;
                    switch (texture) {
                        case HttpTexture httpTexture -> image = MineraculousClientUtils.getNativeImage(httpTexture);
                        case SimpleTexture simpleTexture -> image = simpleTexture.getTextureImage(Minecraft.getInstance().getResourceManager()).getImage();
                        case DynamicTexture dynamicTexture -> image = dynamicTexture.getPixels();
                        default -> {
                            return original;
                        }
                    }
                    if (image != null) {
                        NON_EMPTY_PIXELS.clear();

                        int width = image.getWidth();
                        int height = image.getHeight();
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                if (image.getPixelRGBA(x, y) != 0x00000000)
                                    NON_EMPTY_PIXELS.add(new Vector2i(x, y));
                            }
                        }

                        int pixelsToCataclysm = (int) (NON_EMPTY_PIXELS.size() * (((livingEntity.getMaxHealth() - mineraculous$lastHealth) / livingEntity.getMaxHealth())));
                        CATACLYSM_RANDOM.setSeed(livingEntity.getUUID().getMostSignificantBits());
                        for (int i = 0; i < pixelsToCataclysm; i++) {
                            Vector2i pixel = NON_EMPTY_PIXELS.remove(CATACLYSM_RANDOM.nextInt(NON_EMPTY_PIXELS.size()));
                            int cataclysmColor = MineraculousClientUtils.getCataclysmPixel(CATACLYSM_RANDOM);
                            image.setPixelRGBA(pixel.x, pixel.y, cataclysmColor);
                        }
                        Minecraft.getInstance().getTextureManager().register(result, new DynamicTexture(image));
                        return result;
                    }
                } catch (Exception e) {
                    return original;
                }
            } else {
                return result;
            }
        }
        return original;
    }
}
