package dev.thomasglasser.mineraculous.mixin.minecraft.client.renderer.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.platform.NativeImage;
import dev.thomasglasser.mineraculous.world.effect.MineraculousMobEffects;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
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
                                if (image.getPixelRGBA(x, y) != 0x00000000)
                                    nonEmpty.add(new Vector2i(x, y));
                            }
                        }

                        int pixelsToCataclysm = (int) (nonEmpty.size() * (((livingEntity.getMaxHealth() - mineraculous$lastHealth) / livingEntity.getMaxHealth())));
                        CATACLYSM_RANDOM.setSeed(livingEntity.getUUID().getMostSignificantBits());
                        for (int i = 0; i < pixelsToCataclysm; i++) {
                            Vector2i pixel = nonEmpty.remove(CATACLYSM_RANDOM.nextInt(nonEmpty.size()));
                            int cataclysmColor = switch (CATACLYSM_RANDOM.nextInt(4)) {
                                case 1 -> 0xFF241D28;
                                case 2 -> 0xFF352E39;
                                case 3 -> 0xFF403944;
                                default -> 0xFF211A25;
                            };
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
