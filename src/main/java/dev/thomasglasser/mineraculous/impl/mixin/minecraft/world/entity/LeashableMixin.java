package dev.thomasglasser.mineraculous.impl.mixin.minecraft.world.entity;

import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.world.level.storage.LeashingLadybugYoyoData;
import java.util.Optional;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Leashable.class)
public interface LeashableMixin {
    @Expression("(double)? > 10.0")
    @ModifyExpressionValue(method = "tickLeash", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static <E extends Entity & Leashable> boolean overrideSnapping(boolean original, E entity) {
        if (entity.getData(MineraculousAttachmentTypes.YOYO_LEASH_OVERRIDE)) {
            return false;
        }
        return original;
    }

    @Expression("(double)? > 6.0")
    @ModifyExpressionValue(method = "tickLeash", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static <E extends Entity & Leashable> boolean overrideElasticPull(boolean original, E entity, @Local float f) {
        if (entity.getData(MineraculousAttachmentTypes.YOYO_LEASH_OVERRIDE)) {
            Entity leashHolder = entity.getLeashHolder();
            if (leashHolder != null) {
                Optional<LeashingLadybugYoyoData> optionalData = leashHolder.getData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO);
                if (optionalData.isPresent()) {
                    LeashingLadybugYoyoData data = optionalData.get();
                    return f > data.maxRopeLength();
                }
            }
        }
        return original;
    }

    @ModifyVariable(method = "dropLeash(Lnet/minecraft/world/entity/Entity;ZZ)V", at = @At("HEAD"), index = 2, argsOnly = true)
    private static <E extends Entity & Leashable> boolean neverDropYoyoLeash(boolean original, @Local(argsOnly = true) E entity) {
        if (entity.getData(MineraculousAttachmentTypes.YOYO_LEASH_OVERRIDE)) {
            return false;
        }
        return original;
    }

    @Inject(method = "dropLeash(Lnet/minecraft/world/entity/Entity;ZZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerChunkCache;broadcast(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/network/protocol/Packet;)V"))
    private static <E extends Entity & Leashable> void updateLeashedPlayer(E entity, boolean broadcastPacket, boolean dropItem, CallbackInfo ci) {
        if (entity instanceof ServerPlayer player) {
            player.connection.send(new ClientboundSetEntityLinkPacket(entity, null));
        }
    }

    @Inject(method = "setLeashedTo(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerChunkCache;broadcast(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/network/protocol/Packet;)V"))
    private static <E extends Entity & Leashable> void syncLeashDataToSelf(E entity, Entity leashHolder, boolean broadcastPacket, CallbackInfo ci) {
        if (entity instanceof ServerPlayer player) {
            player.connection.send(new ClientboundSetEntityLinkPacket(entity, leashHolder));
        }
    }
}
