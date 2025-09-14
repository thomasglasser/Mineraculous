package dev.thomasglasser.mineraculous.impl.mixin.minecraft.client.multiplayer;

import com.llamalad7.mixinextras.sugar.Local;
import dev.thomasglasser.mineraculous.api.world.entity.ClientRemovalListener;
import dev.thomasglasser.mineraculous.impl.network.ServerboundHandleEntityRemovedOnClientPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Inject(method = "removeEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;onClientRemoval()V"))
    private void sendRemovedPayload(int entityId, Entity.RemovalReason reason, CallbackInfo ci, @Local Entity entity) {
        if (entity instanceof ClientRemovalListener) {
            TommyLibServices.NETWORK.sendToServer(new ServerboundHandleEntityRemovedOnClientPayload(entityId));
        }
    }
}
