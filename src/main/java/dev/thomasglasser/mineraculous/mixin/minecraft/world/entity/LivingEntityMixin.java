package dev.thomasglasser.mineraculous.mixin.minecraft.world.entity;

import com.llamalad7.mixinextras.sugar.Local;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.network.ServerboundHurtEntityPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    public abstract long getLootTableSeed();

    @Unique
    private final LivingEntity mineraculous$INSTANCE = (LivingEntity) (Object) this;

    protected LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "dropFromLootTable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;JLjava/util/function/Consumer;)V"), cancellable = true)
    private void dropFromLootTable(DamageSource damageSource, boolean hitByPlayer, CallbackInfo ci, @Local LootTable lootTable, @Local LootParams lootParams) {
        if (MineraculousEntityEvents.isCataclysmed(mineraculous$INSTANCE)) {
            lootTable.getRandomItems(lootParams, getLootTableSeed(), stack -> mineraculous$INSTANCE.spawnAtLocation(MineraculousEntityEvents.convertToCataclysmDust(stack)));
            ci.cancel();
        }
    }

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("HEAD"), cancellable = true)
    private void swing(InteractionHand hand, boolean swingHand, CallbackInfo ci) {
        if (mineraculous$INSTANCE instanceof Player player && player.getData(MineraculousAttachmentTypes.MIRACULOUS).isTransformed()) {
            Entity camera = player.isLocalPlayer() ? MineraculousClientUtils.getCameraEntity() : ((ServerPlayer) player).getCamera();
            if (camera instanceof Player target && player != target && target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent() && target.getHealth() > 4) {
                if (player.isLocalPlayer())
                    TommyLibServices.NETWORK.sendToServer(new ServerboundHurtEntityPayload(target.getId(), DamageTypes.PLAYER_ATTACK, 15));
            }
            if (camera != null && camera != mineraculous$INSTANCE)
                ci.cancel();
        }
    }
}
