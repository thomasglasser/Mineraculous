package dev.thomasglasser.mineraculous.impl.mixin.minecraft.client;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
    private void disableGameModeSwitcherWhileSpectating(int key, CallbackInfoReturnable<Boolean> cir) {
        Player player = ClientUtils.getLocalPlayer();
        if (key == 293 && player != null && player.getData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS).spectatingId().isPresent()) {
            cir.setReturnValue(true);
        }
    }
}
