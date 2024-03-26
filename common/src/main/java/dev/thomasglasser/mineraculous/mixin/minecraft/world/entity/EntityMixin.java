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
public class EntityMixin
{
	@Unique
	Entity INSTANCE = (Entity) (Object) this;
	@ModifyReturnValue(method = "getName", at = @At("RETURN"))
	private Component getName(Component original)
	{
		if (INSTANCE instanceof LivingEntity livingEntity)
		{
			return MineraculousEntityEvents.formatDisplayName(livingEntity, original);
		}

		return original;
	}
}
