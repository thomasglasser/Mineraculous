package dev.thomasglasser.mineraculous.mixin.minecraft.world.item;

import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements DataComponentHolder {
    @Unique
    private final ItemStack mineraculous$INSTANCE = (ItemStack) (Object) this;

    @Inject(method = "addToTooltip", at = @At("HEAD"), cancellable = true)
    private <T extends TooltipProvider> void addToTooltip(DataComponentType<T> component, Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag, CallbackInfo ci) {
        if (component == DataComponents.ENCHANTMENTS && has(MineraculousDataComponents.HIDE_ENCHANTMENTS.get()))
            ci.cancel();
    }

    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private void hurtAndBreak(int p_220158_, ServerLevel level, LivingEntity breaker, Consumer<Item> p_348596_, CallbackInfo ci) {
        if (mineraculous$INSTANCE.has(MineraculousDataComponents.KAMIKOTIZATION.get()) && mineraculous$INSTANCE.has(DataComponents.PROFILE)) {
            ServerPlayer target = (ServerPlayer) level.getPlayerByUUID(mineraculous$INSTANCE.get(DataComponents.PROFILE).gameProfile().getId());
            if (target != null) {
                KamikotizationData data = target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).orElseThrow();
                if (data.stackCount() <= 1)
                    MineraculousEntityEvents.handleKamikotizationTransformation(target, data, false, false, breaker.position().add(0, 1, 0));
                else {
                    data.decrementStackCount().save(target, true);
                }
            }
        }
    }
}
