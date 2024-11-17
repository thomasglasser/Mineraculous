package dev.thomasglasser.mineraculous.mixin.minecraft.world.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Unique
    Entity mineraculous$INSTANCE = (Entity) (Object) this;

    @ModifyReturnValue(method = "getName", at = @At("RETURN"))
    private Component getName(Component original) {
        if (mineraculous$INSTANCE instanceof LivingEntity livingEntity) {
            return MineraculousEntityEvents.formatDisplayName(livingEntity, Entity.removeAction(original.copy().withStyle(style -> style.withHoverEvent(null))));
        }

        return original;
    }
}
