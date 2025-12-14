package dev.thomasglasser.mineraculous.impl.mixin.geckolib.renderer;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.impl.client.KwamiBufferSource;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;

@Mixin(GeoRenderer.class)
public interface GeoRendererMixin {
    @Shadow
    boolean bufferNeedsRefresh(VertexConsumer buffer);

    @ModifyVariable(method = "defaultRender", at = @At(value = "INVOKE", target = "Lsoftware/bernie/geckolib/renderer/GeoRenderer;preRender(Lcom/mojang/blaze3d/vertex/PoseStack;Lsoftware/bernie/geckolib/animatable/GeoAnimatable;Lsoftware/bernie/geckolib/cache/object/BakedGeoModel;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZFIII)V"), argsOnly = true)
    default VertexConsumer checkLuckyCharm(VertexConsumer original, @Local(argsOnly = true) MultiBufferSource bufferSource) {
        return switch (this) {
            case GeoItemRenderer<?> itemRenderer -> MineraculousClientUtils.checkItemShaders(original, bufferSource, itemRenderer.getCurrentItemStack(), false, true);
            case GeoArmorRenderer<?> armorRenderer -> MineraculousClientUtils.checkItemShaders(original, bufferSource, armorRenderer.getCurrentStack(), true, false);
            case GeoEntityRenderer<?> entityRenderer when entityRenderer.getAnimatable() instanceof AbstractArrow arrow -> MineraculousClientUtils.checkItemShaders(original, bufferSource, arrow.getPickupItemStackOrigin(), false, true);
            default -> original;
        };
    }

    @ModifyReturnValue(method = "bufferNeedsRefresh", at = @At("RETURN"))
    private boolean isKwamiGlowing(boolean original, VertexConsumer buffer) {
        if (buffer instanceof KwamiBufferSource.KwamiOutlineGenerator kwamiGlows) {
            return bufferNeedsRefresh(kwamiGlows.delegate());
        }
        if (original) {
            return true;
        }
        return false;
    }

    @ModifyReturnValue(method = "checkAndRefreshBuffer", at = @At("RETURN"))
    default VertexConsumer kwamiRefreshOutline(
            VertexConsumer original,
            boolean isReRender,
            VertexConsumer buffer,
            MultiBufferSource bufferSource,
            RenderType renderType) {
        if (isReRender)
            return original;

        if (original instanceof KwamiBufferSource.KwamiOutlineGenerator kwami
                && bufferNeedsRefresh(kwami.delegate())) {
            return new KwamiBufferSource.KwamiOutlineGenerator(
                    bufferSource.getBuffer(renderType),
                    kwami.color());
        }

        return original;
    }
}
