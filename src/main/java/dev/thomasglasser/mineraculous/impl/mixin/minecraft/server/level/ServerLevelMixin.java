package dev.thomasglasser.mineraculous.impl.mixin.minecraft.server.level;

import dev.thomasglasser.mineraculous.impl.network.ClientboundSyncArrowPickupStackPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Shadow
    public abstract MinecraftServer getServer();

    @Inject(method = "addEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;onAddedToLevel()V"))
    private void syncArrowStack(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof AbstractArrow arrow) {
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncArrowPickupStackPayload(arrow.getId(), arrow.getPickupItemStackOrigin()), this.getServer());
        }
    }
}
