package dev.thomasglasser.mineraculous.impl.mixin.minecraft.world.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.impl.world.item.LadybugYoyoItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Unique
    private final Entity mineraculous$instance = (Entity) (Object) this;

    @ModifyReturnValue(method = "getName", at = @At("RETURN"))
    private Component formatMiraculousName(Component original) {
        return MineraculousEntityUtils.formatDisplayName(mineraculous$instance, original);
    }

    @Inject(method = "interact", at = @At("HEAD"))
    private void fixLeashYoyoInteraction(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (mineraculous$instance.isAlive() && this instanceof Leashable leashable) {
            if (leashable.getLeashHolder() == player) {
                if (!mineraculous$instance.level().isClientSide()) {
                    if (player.getItemInHand(hand).is(MineraculousItems.LADYBUG_YOYO)) {
                        LadybugYoyoItem.removeLeash(player);
                        cir.setReturnValue(InteractionResult.sidedSuccess(mineraculous$instance.level().isClientSide));
                    }
                }
            }

        }
    }
}
