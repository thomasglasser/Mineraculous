package dev.thomasglasser.mineraculous.mixin.neoforge.common.extensions;

import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
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
            MineraculousEntityEvents.checkKamikotizationStack(itemEntity.getItem(), level, damageSource.getEntity());
        }
    }
}
