package dev.thomasglasser.mineraculous.impl.mixin.minecraft.server.level;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Unique
    private final ServerPlayer mineraculous$instance = (ServerPlayer) (Object) this;

    @Inject(method = "broadcastToPlayer", at = @At(value = "HEAD"), cancellable = true)
    private void broadcastToPlayer(ServerPlayer player, CallbackInfoReturnable<Boolean> cir) {
        if (mineraculous$shouldForceSyncSpectator(mineraculous$instance) || mineraculous$shouldForceSyncSpectator(player))
            cir.setReturnValue(true);
    }

    @Unique
    private static boolean mineraculous$shouldForceSyncSpectator(ServerPlayer player) {
        return player.isSpectator() && player.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).spectatingId().isPresent();
    }
}
