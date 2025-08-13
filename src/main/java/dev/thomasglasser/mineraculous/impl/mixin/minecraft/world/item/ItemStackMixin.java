package dev.thomasglasser.mineraculous.impl.mixin.minecraft.world.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemUtils;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
    private final ItemStack mineraculous$instance = (ItemStack) (Object) this;

    @Inject(method = "addToTooltip", at = @At("HEAD"), cancellable = true)
    private <T extends TooltipProvider> void hideEnchantmentsWhenComponentPresent(DataComponentType<T> component, Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag, CallbackInfo ci) {
        if (component == DataComponents.ENCHANTMENTS && has(MineraculousDataComponents.HIDE_ENCHANTMENTS))
            ci.cancel();
    }

    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private void checkKamikotizationStack(int p_220158_, ServerLevel level, LivingEntity breaker, Consumer<Item> p_348596_, CallbackInfo ci) {
        Kamikotization.checkBroken(mineraculous$instance, level, breaker);
    }

    @WrapOperation(method = "isSameItemSameComponents", at = @At(value = "INVOKE", target = "Ljava/util/Objects;equals(Ljava/lang/Object;Ljava/lang/Object;)Z"))
    private static boolean checkSameBesidesCarrier(Object a, Object b, Operation<Boolean> original) {
        if (original.call(a, b))
            return true;

        if (!(a instanceof PatchedDataComponentMap self) || !(b instanceof PatchedDataComponentMap other))
            return false;

        return MineraculousItemUtils.isSameComponentsBesides(self, other, MineraculousDataComponents.CARRIER);
    }
}
