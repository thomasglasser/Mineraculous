package dev.thomasglasser.mineraculous.impl.mixin.minecraft.client.renderer.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.PlayerLikeRenderer;
import dev.thomasglasser.mineraculous.impl.world.entity.PlayerLike;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @ModifyReturnValue(method = "getRenderer", at = @At("TAIL"))
    private <T extends Entity> EntityRenderer<? super T> getRenderer(EntityRenderer<? super T> original, T entity) {
        if (original == null && entity instanceof LivingEntity && entity instanceof PlayerLike playerLike && playerLike.getVisualSource() instanceof AbstractClientPlayer player)
            return (EntityRenderer<? super T>) PlayerLikeRenderer.get(player.getSkin().model());
        return original;
    }
}
