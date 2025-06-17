package dev.thomasglasser.mineraculous.impl.mixin.neoforge.common.extensions;

import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IItemExtension.class)
public interface IItemExtensionMixin {
    @Inject(method = "onDestroyed", at = @At("HEAD"))
    default void checkKamikotizationStackOnDestroyed(ItemEntity itemEntity, DamageSource damageSource, CallbackInfo ci) {
        if (itemEntity.level() instanceof ServerLevel level) {
            Kamikotization.checkBroken(itemEntity.getItem(), level, damageSource.getEntity());
        }
    }
}
