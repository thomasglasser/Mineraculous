package dev.thomasglasser.mineraculous.mixin.minecraft.world.entity.player;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin {
    @Unique
    Player mineraculous$INSTANCE = (Player) (Object) this;

    @ModifyReturnValue(method = "getName", at = @At("RETURN"))
    private Component getName(Component original) {
        return MineraculousEntityEvents.formatDisplayName(mineraculous$INSTANCE, original);
    }

    @ModifyReturnValue(method = "decorateDisplayNameComponent", at = @At(value = "RETURN"))
    private MutableComponent decorateDisplayNameComponent(MutableComponent original) {
        return MineraculousEntityEvents.formatDisplayName(mineraculous$INSTANCE, original).copy();
    }
}
