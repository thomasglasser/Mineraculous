package dev.thomasglasser.mineraculous.mixin.minecraft.world.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Unique
    private final Entity mineraculous$instance = (Entity) (Object) this;

    @ModifyReturnValue(method = "getName", at = @At("RETURN"))
    private Component formatMiraculousName(Component original) {
        return MineraculousEntityUtils.formatDisplayName(mineraculous$instance, original);
    }
}
