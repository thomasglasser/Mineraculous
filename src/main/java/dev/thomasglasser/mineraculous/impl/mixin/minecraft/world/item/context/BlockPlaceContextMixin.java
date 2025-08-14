package dev.thomasglasser.mineraculous.impl.mixin.minecraft.world.item.context;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockPlaceContext.class)
public abstract class BlockPlaceContextMixin extends UseOnContext {
    private BlockPlaceContextMixin(Player player, InteractionHand hand, BlockHitResult hitResult) {
        super(player, hand, hitResult);
    }

    @ModifyReturnValue(method = "canPlace", at = @At("RETURN"))
    private boolean disallowPlacingKamikotizedItems(boolean original) {
        return original && !getItemInHand().has(MineraculousDataComponents.KAMIKOTIZATION);
    }
}
