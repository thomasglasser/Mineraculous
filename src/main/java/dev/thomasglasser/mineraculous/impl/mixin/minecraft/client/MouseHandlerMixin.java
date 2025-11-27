package dev.thomasglasser.mineraculous.impl.mixin.minecraft.client;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void cancelTurnWhenSpectating(double movementTime, CallbackInfo ci) {
        Player player = ClientUtils.getLocalPlayer();
        if (player != null && player.getData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS).spectatingId().isPresent()) {
            ci.cancel();
        }
    }
}
