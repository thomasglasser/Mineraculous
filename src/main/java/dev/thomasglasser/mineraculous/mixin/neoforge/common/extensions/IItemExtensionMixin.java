package dev.thomasglasser.mineraculous.mixin.neoforge.common.extensions;

import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import net.minecraft.core.component.DataComponents;
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
    default void onDestroyed(ItemEntity itemEntity, DamageSource damageSource, CallbackInfo ci) {
        if (!itemEntity.level().isClientSide()) {
            ItemStack stack = itemEntity.getItem();
            if (stack.has(MineraculousDataComponents.KAMIKOTIZATION.get()) && stack.has(DataComponents.PROFILE)) {
                ServerPlayer target = (ServerPlayer) itemEntity.level().getPlayerByUUID(stack.get(DataComponents.PROFILE).gameProfile().getId());
                if (target != null) {
                    KamikotizationData data = target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION);
                    data.kamikotizedStack().setCount(1);
                    MineraculousEntityEvents.handleKamikotizationTransformation(target, data, false, false, true, itemEntity.position().add(0, 1, 0));
                    data.kamikotizedStack().setCount(0);
                }
            }
        }
    }
}
