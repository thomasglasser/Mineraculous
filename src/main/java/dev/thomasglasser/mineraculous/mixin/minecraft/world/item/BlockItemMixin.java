package dev.thomasglasser.mineraculous.mixin.minecraft.world.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @WrapOperation(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/context/BlockPlaceContext;canPlace()Z"))
    private boolean canPlace(BlockPlaceContext instance, Operation<Boolean> original) {
        if (instance.getItemInHand().has(MineraculousDataComponents.KAMIKOTIZATION))
            return false;
        return original.call(instance);
    }
}
