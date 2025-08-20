package dev.thomasglasser.mineraculous.impl.mixin.minecraft.world.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.thomasglasser.mineraculous.impl.world.entity.MineraculousInternalEntityUtils;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnEggItem.class)
public class SpawnEggItemMixin {
    @WrapOperation(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;spawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;ZZ)Lnet/minecraft/world/entity/Entity;"))
    private <T extends Entity> T checkAndTrackLuckyCharmEntityOn(EntityType<T> instance, ServerLevel serverLevel, @Nullable ItemStack stack, @Nullable Player player, BlockPos pos, MobSpawnType spawnType, boolean shouldOffsetY, boolean shouldOffsetYMore, Operation<T> original) {
        T spawned = original.call(instance, serverLevel, stack, player, pos, spawnType, shouldOffsetY, shouldOffsetYMore);
        if (stack != null) {
            MineraculousInternalEntityUtils.checkAndTrackLuckyCharmEntity(player, spawned, serverLevel, stack);
        }
        return spawned;
    }

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;spawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;ZZ)Lnet/minecraft/world/entity/Entity;"))
    private <T extends Entity> T checkAndTrackLuckyCharmEntity(EntityType<T> instance, ServerLevel serverLevel, @Nullable ItemStack stack, @Nullable Player player, BlockPos pos, MobSpawnType spawnType, boolean shouldOffsetY, boolean shouldOffsetYMore, Operation<T> original) {
        T spawned = original.call(instance, serverLevel, stack, player, pos, spawnType, shouldOffsetY, shouldOffsetYMore);
        if (stack != null) {
            MineraculousInternalEntityUtils.checkAndTrackLuckyCharmEntity(player, spawned, serverLevel, stack);
        }
        return spawned;
    }

    @Inject(method = "spawnOffspringFromSpawnEgg", at = @At("TAIL"))
    private void checkAndTrackLuckyCharmOffspring(Player player, Mob parent, EntityType<? extends Mob> entityType, ServerLevel serverLevel, Vec3 pos, ItemStack stack, CallbackInfoReturnable<Optional<Mob>> cir) {
        cir.getReturnValue().ifPresent(offspring -> MineraculousInternalEntityUtils.checkAndTrackLuckyCharmEntity(player, offspring, serverLevel, stack));
    }
}
