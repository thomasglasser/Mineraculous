package dev.thomasglasser.mineraculous.mixin.geckolib.renderer;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;

@Mixin(GeoRenderer.class)
public interface GeoRendererMixin {
    @ModifyVariable(method = "defaultRender", at = @At(value = "INVOKE", target = "Lsoftware/bernie/geckolib/renderer/GeoRenderer;preRender(Lcom/mojang/blaze3d/vertex/PoseStack;Lsoftware/bernie/geckolib/animatable/GeoAnimatable;Lsoftware/bernie/geckolib/cache/object/BakedGeoModel;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZFIII)V"), argsOnly = true)
    default VertexConsumer checkLuckyCharm(VertexConsumer original, @Local(argsOnly = true) MultiBufferSource bufferSource) {
        return switch (this) {
            case GeoItemRenderer<?> itemRenderer when itemRenderer.getCurrentItemStack() != null -> MineraculousClientUtils.checkLuckyCharm(original, bufferSource, itemRenderer.getCurrentItemStack(), false, true);
            case GeoArmorRenderer<?> armorRenderer when armorRenderer.getCurrentStack() != null -> MineraculousClientUtils.checkLuckyCharm(original, bufferSource, armorRenderer.getCurrentStack(), true, false);
            case GeoEntityRenderer<?> entityRenderer when entityRenderer.getAnimatable() instanceof AbstractArrow arrow -> MineraculousClientUtils.checkLuckyCharm(original, bufferSource, arrow.getPickupItemStackOrigin(), false, true);
            default -> original;
        };
    }
}
