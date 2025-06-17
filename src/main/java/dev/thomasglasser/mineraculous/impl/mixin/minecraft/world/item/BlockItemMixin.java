package dev.thomasglasser.mineraculous.impl.mixin.minecraft.world.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @WrapOperation(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/context/BlockPlaceContext;canPlace()Z"))
    private boolean disallowPlacingKamikotizedItems(BlockPlaceContext instance, Operation<Boolean> original) {
        return !instance.getItemInHand().has(MineraculousDataComponents.KAMIKOTIZATION) && original.call(instance);
    }
}
