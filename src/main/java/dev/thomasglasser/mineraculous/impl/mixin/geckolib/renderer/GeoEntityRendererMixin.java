package dev.thomasglasser.mineraculous.impl.mixin.geckolib.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Mixin(GeoEntityRenderer.class)
public class GeoEntityRendererMixin {
    @Inject(method = "renderLeash", at = @At("HEAD"), cancellable = true)
    private <E extends Entity, M extends Mob> void disableLeashRenderingForYoyoLeash(M mob, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, E leashHolder, CallbackInfo ci) {
        if (mob.getData(MineraculousAttachmentTypes.YOYO_LEASH_OVERRIDE) && leashHolder instanceof Player player && player.getData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO).isPresent()) {
            ci.cancel();
        }
    }
}
