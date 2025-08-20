package dev.thomasglasser.mineraculous.impl.mixin.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.YoyoRopeRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @Inject(method = "renderLeash", at = @At("HEAD"), cancellable = true)
    private <T extends Entity, E extends Entity> void renderYoyoRope(T entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, E leashHolder, CallbackInfo ci) {
        if (entity.getData(MineraculousAttachmentTypes.YOYO_LEASH_OVERRIDE) && leashHolder instanceof Player player && player.getData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO).isPresent()) {
            YoyoRopeRenderer.render(entity, player, player.getData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO).get().maxRopeLength(), poseStack, bufferSource, partialTick);
            ci.cancel();
        }
    }
}
